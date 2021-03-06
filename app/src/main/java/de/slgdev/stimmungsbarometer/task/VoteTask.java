package de.slgdev.stimmungsbarometer.task;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.slgdev.leoapp.utility.Utils;
import de.slgdev.stimmungsbarometer.utility.Vote;

public class VoteTask extends AsyncTask<Vote, Void, Void> {
    @Override
    protected Void doInBackground(Vote... votes) {
        if (votes[0] != null) {
            try {
                Vote w = votes[0];
                URLConnection connection = new URL(de.slgdev.leoapp.utility.Utils.BASE_URL_PHP + "stimmungsbarometer/vote.php?vid=" + w.voteid + "&uid=" + w.userid)
                        .openConnection();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection
                                                .getInputStream(), "UTF-8"));
                while (reader.readLine() != null)
                    ;
                reader.close();
            } catch (IOException e) {
                Utils.logError(e);
                Utils.logError(e);
            }
        }
        return null;
    }
}
