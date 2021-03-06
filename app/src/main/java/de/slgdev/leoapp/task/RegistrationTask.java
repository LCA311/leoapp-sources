package de.slgdev.leoapp.task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import de.slgdev.leoapp.task.general.TaskStatusListener;
import de.slgdev.leoapp.task.general.VoidCallbackTask;
import de.slgdev.leoapp.utility.NetworkUtils;
import de.slgdev.leoapp.utility.RequestMethod;
import de.slgdev.leoapp.utility.ResponseCode;
import de.slgdev.leoapp.utility.Utils;

public class RegistrationTask extends VoidCallbackTask<ResponseCode> {

    @Override
    protected ResponseCode doInBackground(Void... voids) {

        if (!NetworkUtils.isNetworkAvailable()) {
            return ResponseCode.NO_CONNECTION;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("name", Utils.getUserDefaultName());
            requestBody.put("device", Utils.getDeviceIdentifier());
            requestBody.put("checksum", Utils.getDeviceChecksum());

            Utils.logError(Utils.getUserDefaultName());
            Utils.logError(Utils.getDeviceIdentifier());
            Utils.logError(Utils.getDeviceChecksum());

            HttpURLConnection connection = NetworkUtils.openURLConnection(
                    Utils.BASE_URL_PHP + "user/add/index.php",
                    RequestMethod.POST,
                    "application/json",
                    requestBody.toString()
            );

            int responseCode = connection.getResponseCode();

            if (responseCode == 401) {
                Utils.logError("401 error");
                return ResponseCode.AUTH_FAILED;
            }

            if (responseCode != 200) {
                Utils.logError("non 200 error: "+responseCode);
                Utils.logError(NetworkUtils.getJSONResponse(connection));
                return ResponseCode.SERVER_FAILED;
            }

            JSONObject response = NetworkUtils.getJSONResponse(connection);
            int userid = response.getJSONObject("data").getInt("user_id");

            Utils.getController().getPreferences()
                    .edit()
                    .putInt("pref_key_general_id", userid)
                    .apply();

            return ResponseCode.SUCCESS;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return ResponseCode.NOT_SENT;
        }
    }

    @Override
    protected void onPostExecute(ResponseCode result) {
        for (TaskStatusListener l : getListeners()) {
            l.taskFinished(result);
        }
    }
}
