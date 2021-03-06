package de.slgdev.essensbons.task;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import de.slgdev.essensbons.utility.EncryptionManager;
import de.slgdev.essensbons.utility.EssensbonUtils;
import de.slgdev.essensbons.utility.Order;
import de.slgdev.leoapp.sqlite.SQLiteConnectorEssensbons;
import de.slgdev.leoapp.task.general.TaskStatusListener;
import de.slgdev.leoapp.task.general.VoidCallbackTask;
import de.slgdev.leoapp.utility.GraphicUtils;
import de.slgdev.leoapp.utility.Utils;

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

        if (onAppStart) {
            if (EssensbonUtils.isAutoSyncEnabled()) {
                if (EssensbonUtils.fastConnectionAvailable()) {
                    saveNewestEntries();
                }
            }
        } else if (EssensbonUtils.fastConnectionAvailable()) {
            saveNewestEntries();
        }

        Order act = getRecentEntry();
        if (act == null)
            return null;

        menu = act.getMenu();
        description = act.getDescr();
        int id = act.getId();

        Utils.logDebug("ID: "+act.getId());

        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.GERMAN);
        Date       date       = new Date();
        String     dateS      = dateFormat.format(date);
        dateS = dateS.substring(0, 4) + dateS.substring(5);

        int cur = Integer.parseInt(dateS.substring(0, 2) + dateS.substring(4)) + id;
        int mod = cur % 97;
        int fin = 98 - mod;

        String formattedChecksum = String.format(Locale.GERMANY, "%02d", fin);

        String code = id + "-" + "M" + menu + "-" + dateS + "-" + formattedChecksum;

        return createNewQR(code);
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        for (TaskStatusListener l : getListeners())
            l.taskFinished(result, menu, description);

        dbh.close();
    }

    @Nullable
    private Order getRecentEntry() {

        DateFormat dateFormat    = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        Date       date          = new Date();
        String     selection     = SQLiteConnectorEssensbons.ORDER_DATE + " = ?";
        String[]   selectionArgs = {dateFormat.format(date)};

        Cursor cursor = dbh.query(
                SQLiteConnectorEssensbons.TABLE_ORDERS,
                new String[]{
                        SQLiteConnectorEssensbons.ORDER_ID,
                        SQLiteConnectorEssensbons.ORDER_MENU,
                        SQLiteConnectorEssensbons.ORDER_DESCR
                },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.getCount() == 0)
            return null;

        cursor.moveToNext();

        int    id   = cursor.getInt(0);
        short  menu = cursor.getShort(1);
        String desc = String.valueOf(cursor.getString(2));

        Order o = new Order(id, date, menu, desc);
        cursor.close();
        return o;
    }

    private void saveNewestEntries() {

        try {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL(
                                    Utils.URL_LUNCH_LEO + "qr_database.php?" +
                                            "id=" + EssensbonUtils.getCustomerId() + "&" +
                                            "auth=2SnDS7GBdHf5sd"
                            )
                                    .openConnection()
                                    .getInputStream()
                    )
            );


            Utils.logError(Utils.URL_LUNCH_LEO + "qr_database.php?" +
                    "id=" + EssensbonUtils.getCustomerId() + "&" +
                    "auth=2SnDS7GBdHf5sd");

            StringBuilder builder = new StringBuilder();
            String        line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("<"))
                    builder.append(line);
            }

            reader.close();

            String result = builder.toString();
            String decrypted = EncryptionManager.decrypt(result)
                    .replace("\r", " ")
                    .replace("\t", " ");

            String[]   data       = decrypted.split("_next_");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.GERMANY);

            Date highest = null;

            try {
                highest = dateFormat.parse("1900-01-01");
            } catch (ParseException e) {
                Utils.logError(e);
            }

            int amount = 0;

            for (String s : data) {

                if (!s.contains("_separator_"))
                    continue;

                ContentValues values = new ContentValues();
                amount++;

                try {
                    Date d = dateFormat.parse(s.split("_separator_")[1]);
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

                dbh.insertWithOnConflict(SQLiteConnectorEssensbons.TABLE_ORDERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            }

            String dateString = dateFormat.format(highest);

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
                    + ") VALUES ('" + dateFormat.format(new Date()) + "', " + amount + ", " + c.getInt(0) + ")");

            c.close();
        } catch (IOException e) {
            Utils.logError(e);
        }
    }

    private Bitmap createNewQR(String s) {
        MultiFormatWriter mFW = new MultiFormatWriter();
        Bitmap            bM  = null;

        int px = (int) (GraphicUtils.getDisplayWidth()*0.9f);
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2); /* default = 4 */

        try {
            BitMatrix bitM = mFW.encode(s, BarcodeFormat.QR_CODE, px, px, hints);
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
