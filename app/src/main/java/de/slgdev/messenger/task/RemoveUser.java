package de.slgdev.messenger.task;

import android.os.AsyncTask;

import de.slgdev.leoapp.Start;
import de.slgdev.leoapp.service.SocketService;
import de.slgdev.leoapp.utility.User;
import de.slgdev.leoapp.utility.Utils;
import de.slgdev.messenger.activity.ChatEditActivity;
import de.slgdev.messenger.utility.Assoziation;

public class RemoveUser extends AsyncTask<User, Void, Void> {
    private final int              cid;
    private       SocketService    service;
    private       ChatEditActivity chatEditActivity;

    public RemoveUser(ChatEditActivity chatEditActivity, int cid) {
        this.chatEditActivity = chatEditActivity;
        this.cid = cid;
        this.service = Utils.getController().getSocketService();

        if (service == null) {
            Start.startReceiveService();
            this.service = Utils.getController().getSocketService();
        }
    }

    @Override
    protected void onPreExecute() {
        chatEditActivity.notifyTaskStarted(this);
    }

    @Override
    protected Void doInBackground(User... params) {
        for (User u : params) {
            service.sendRemove(new Assoziation(cid, u.uid));
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        chatEditActivity.notifyTaskDone(this);
    }
}
