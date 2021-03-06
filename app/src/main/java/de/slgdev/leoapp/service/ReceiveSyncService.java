package de.slgdev.leoapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import de.slgdev.leoapp.utility.Utils;

/**
 * ReceiveSyncService.
 *
 * Startet den SyncAdapter, der Updates bzgl. Umfragen und Neuigkeiten am SchwarzenBrett empfängt.
 *
 * @author Gianni
 * @since 0.6.9
 * @version 2017.0712
 */

public class ReceiveSyncService extends Service {

    private static ReceiveSyncAdapter syncAdapter;
    private static final Object syncLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        if(Utils.getContext() == null)
            Utils.getController().setContext(getApplicationContext());
        synchronized (syncLock) {
            if (syncAdapter == null) {
                syncAdapter = new ReceiveSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
