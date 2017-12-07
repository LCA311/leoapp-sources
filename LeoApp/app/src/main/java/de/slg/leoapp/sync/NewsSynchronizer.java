package de.slg.leoapp.sync;

import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.slg.leoapp.notification.NotificationHandler;
import de.slg.leoapp.sqlite.SQLiteConnectorNews;
import de.slg.leoapp.utility.Utils;

/**
 * NewsSynchronizer.
 *
 * Klasse zum Verwalten des News-Synchronisationstasks.
 *
 * @author Gianni
 * @since 0.6.8
 * @version 2017.0712
 */

public class NewsSynchronizer implements Synchronizer {

    @Override
    public boolean run() {

        if(!Utils.checkNetwork())
            return false;

        try {
            URLConnection connection = new URL(Utils.DOMAIN_DEV + "schwarzesBrett/meldungen.php")
                    .openConnection();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String        line;
            while ((line = reader.readLine()) != null)
                builder.append(line)
                        .append(System.getProperty("line.separator"));
            reader.close();

            SQLiteConnectorNews db = new SQLiteConnectorNews(Utils.getContext());
            SQLiteDatabase dbh    = db.getWritableDatabase();

            dbh.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + SQLiteConnectorNews.TABLE_EINTRAEGE + "'");
            dbh.delete(SQLiteConnectorNews.TABLE_EINTRAEGE, null, null);
            String[] result = builder.toString().split("_next_");
            for (String s : result) {
                String[] res = s.split(";");
                if (res.length == 8) {
                    dbh.insert(SQLiteConnectorNews.TABLE_EINTRAEGE, null, db.getEntryContentValues(
                            res[0],
                            res[1],
                            res[2],
                            Long.parseLong(res[3] + "000"),
                            Long.parseLong(res[4] + "000"),
                            Integer.parseInt(res[5]),
                            Integer.parseInt(res[6]),
                            res[7]
                    ));
                }
            }
            dbh.close();
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void postUpdate() {
        new NotificationHandler.NewsNotification().send();
    }

}
