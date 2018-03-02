package de.slgdev.leoapp.task;

import android.support.v4.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import de.slgdev.leoapp.task.general.VoidCallbackTask;
import de.slgdev.leoapp.utility.ResponseCode;
import de.slgdev.leoapp.utility.Utils;
import de.slgdev.leoapp.utility.VerificationListener;
import de.slgdev.leoapp.utility.datastructure.List;

public class SyncUserTask extends VoidCallbackTask<ResponseCode> {

    private List<VerificationListener> listeners;
    private Fragment                   fragment;

    public SyncUserTask(Fragment fragment) {
        listeners = new List<>();
        this.fragment = fragment;
    }

    public SyncUserTask() {
        listeners = new List<>();
        this.fragment = null;
    }

    @Override
    protected ResponseCode doInBackground(Void... params) {
        if (!Utils.checkNetwork()) {
            return ResponseCode.NO_CONNECTION;
        }

        try {
            StringBuilder builder = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL(
                                    Utils.BASE_URL_PHP + "user/" +
                                            "updateUser.php?" +
                                            "name=" + Utils.getUserDefaultName()
                            )
                                    .openConnection()
                                    .getInputStream()
                    )
            );

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            String result = builder.toString();
            if (result.startsWith("-")) {
                throw new IOException(result);
            }

            int uid = Integer.parseInt(result.substring(0, result.indexOf(';')));
            result = result.substring(result.indexOf(';') + 1);

            int permission = Integer.parseInt(result.substring(0, result.indexOf(';')));
            result = result.substring(result.indexOf(';') + 1);

            String uname = result;

            Utils.getController().getPreferences()
                    .edit()
                    .putInt("pref_key_general_id", uid)
                    .putInt("pref_key_general_permission", permission)
                    .putString("pref_key_general_name", uname)
                    .apply();

            return ResponseCode.SUCCESS;
        } catch (IOException e) {
            Utils.logError(e);
        }
        return ResponseCode.SERVER_FAILED;
    }

    public SyncUserTask registerListener(VerificationListener listener) {
        listeners.append(listener);
        return this;
    }

    @Override
    protected void onPostExecute(ResponseCode code) {
        if (fragment == null)
            return;

        for (VerificationListener l : listeners) {
            l.onSynchronisationProcessed(code, fragment);
        }
    }
}