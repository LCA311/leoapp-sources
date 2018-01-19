package de.slg.essensbons.task;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.slg.essensbons.utility.EncryptionManager;
import de.slg.essensbons.utility.EssensbonUtils;
import de.slg.essensbons.utility.Order;
import de.slg.leoapp.sqlite.SQLiteConnectorEssensbons;
import de.slg.leoapp.task.general.TaskStatusListener;
import de.slg.leoapp.task.general.VoidCallbackTask;
import de.slg.leoapp.utility.GraphicUtils;
import de.slg.leoapp.utility.Utils;

public class QRWriteTask extends VoidCallbackTask<Bitmap> {

    private short   menu;
    private boolean onAppStart;
    private String  description;

    private SQLiteDatabase dbh;

    public QRWriteTask(boolean startedOnAppStart) {
        onAppStart = startedOnAppStart;

        SQLiteConnectorEssensbons db = new SQLiteConnectorEssensbons(Utils.getContext());
        dbh = db.getWritableDatabase();
    }

    @Override
    @SuppressLint("DefaultLocale")
    protected Bitmap doInBackground(Void... params) {

        if (!EssensbonUtils.isLoggedIn())
            return null;

        if (hasActiveInternetConnection()) {
            if (onAppStart) {
                if (EssensbonUtils.isAutoSyncEnabled())
                    saveNewestEntries();
            } else
                saveNewestEntries();
        }

        Order act = getRecentEntry();
        if (act == null)
            return null;

        menu = act.getMenu();
        description = act.getDescr();
        short id = act.getId();

        DateFormat dateFormat = new SimpleDateFormat("ddMMyyy", Locale.GERMAN);
        Date       date       = new Date();
        String     dateS      = dateFormat.format(date);
        dateS = dateS.substring(0, 4) + dateS.substring(5);

        int cur = Integer.parseInt(dateS)+id;
        int mod = cur%97;
        int fin = 98-mod;

        String formattedChecksum = String.format("%02d", fin);

        String code = id+"-"+"M"+menu+"-"+dateS+"-"+formattedChecksum;

        return createNewQR(code);
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        for (TaskStatusListener l : getListeners())
            l.taskFinished(result, menu, description);

        dbh.close();

    }

    private boolean hasActiveInternetConnection() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.lunch.leo-ac.de").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return urlc.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    private Order getRecentEntry() {
        String[] projection = {
                SQLiteConnectorEssensbons.ORDER_DATE,
                SQLiteConnectorEssensbons.ORDER_MENU,
                SQLiteConnectorEssensbons.ORDER_DESCR,
        };
        DateFormat dateFormat    = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        Date       date          = new Date();
        String     selection     = SQLiteConnectorEssensbons.ORDER_DATE + " = ?";
        String[]   selectionArgs = {dateFormat.format(date)};

        Cursor cursor = dbh.query(
                SQLiteConnectorEssensbons.TABLE_ORDERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.getCount() == 0)
            return null;

        cursor.moveToNext();

        String desc = String.valueOf(cursor.getString(cursor.getColumnIndex(SQLiteConnectorEssensbons.ORDER_DESCR)));
        short menu = cursor.getShort(cursor.getColumnIndex(SQLiteConnectorEssensbons.ORDER_MENU));
        short id = cursor.getShort(cursor.getColumnIndex(SQLiteConnectorEssensbons.ORDER_ID));

        Order  o = new Order(id, date, menu, desc);
        cursor.close();
        return o;
    }

    private void saveNewestEntries() {

        BufferedReader in    = null;
        StringBuilder result = new StringBuilder();

        try {
            URL interfaceDB = new URL(Utils.URL_LUNCH_LEO + "qr_database.php?id=" + EssensbonUtils.getCustomerId()
                    + "&auth=2SnDS7GBdHf5sd");

            Utils.logDebug(interfaceDB.toString());
            in = null;
            in = new BufferedReader(new InputStreamReader(interfaceDB.openStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                if (!inputLine.contains("<"))
                    result.append(inputLine);
            }
            in.close();

        } catch (IOException e) {
            Utils.logError(e);
            return;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    Utils.logError(e);
                }
        }

        EncryptionManager encryptionManager = new EncryptionManager();
        try {
            result = new StringBuilder(new String(encryptionManager.decrypt(result.toString())));
        } catch (Exception e) {
            Utils.logError(e);
        }
        String[]   data = result.toString().split("_next_");
        DateFormat df   = new SimpleDateFormat("yyyy-mm-dd", Locale.GERMANY);

        Date highest = null;

        try {
            highest = df.parse("1900-01-01");
        } catch (ParseException e) {
            Utils.logError(e);
        }

        int amount = 0;

        for (String s : data) {
            if (!s.contains("_seperator_"))
                continue;

            ContentValues values = new ContentValues();
            amount++;

            try {
                Date d = df.parse(s.split("_seperator_")[0]);
                if (d.after(highest))
                    highest = d;
            } catch (ParseException e) {
                Utils.logError(e);
            }

            String[] parts = s.split("_separator_");

            values.put(SQLiteConnectorEssensbons.ORDER_ID, parts[0]);
            values.put(SQLiteConnectorEssensbons.ORDER_DATE, parts[1]);
            values.put(SQLiteConnectorEssensbons.ORDER_MENU, parts[2]);
            values.put(SQLiteConnectorEssensbons.ORDER_DESCR, parts[3]);

            try {
                dbh.insert(SQLiteConnectorEssensbons.TABLE_ORDERS, null, values);
            } catch (Exception e) {
                Utils.logError(e);
            }
        }

        String dateString = df.format(highest);

        if (dateString == null)
            return;


        Cursor c = dbh.rawQuery("SELECT "
                + SQLiteConnectorEssensbons.ORDER_ID
                + " FROM "
                + SQLiteConnectorEssensbons.TABLE_ORDERS
                + " WHERE " + SQLiteConnectorEssensbons.ORDER_DATE + " = "
                + dateString, null);

        c.moveToFirst();

        if (c.getCount() == 0)
            return;

        dbh.execSQL("INSERT INTO " + SQLiteConnectorEssensbons.TABLE_STATISTICS
                + " (" + SQLiteConnectorEssensbons.STATISTICS_SYNCDATE + ", "
                + SQLiteConnectorEssensbons.STATISTICS_AMOUNT + ", "
                + SQLiteConnectorEssensbons.STATISTICS_LASTORDER
                + ") VALUES ('" + df.format(new Date()) + "', " + amount + ", " + c.getInt(0) + ")");

        c.close();
    }

    private Bitmap createNewQR(String s) {
        MultiFormatWriter mFW = new MultiFormatWriter();
        Bitmap bM = null;

        int px = (int) GraphicUtils.dpToPx(250);

        try {
            BitMatrix bitM = mFW.encode(s, BarcodeFormat.QR_CODE, px, px);
            bM = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < px; i++) {
                for (int j = 0; j < px; j++) {
                    int c = (bitM.get(i, j)) ? Color.BLACK : Color.WHITE;
                    bM.setPixel(i, j, c);
                }
            }
        } catch (WriterException e) {
            Utils.logError(e);
        }
        return bM;
    }
}