package de.slgdev.leoapp.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import de.slgdev.leoapp.sync.NewsSynchronizer;
import de.slgdev.leoapp.sync.SurveySynchronizer;
import de.slgdev.leoapp.sync.Synchronizer;

/**
 * ReceiveSyncAdapter.
 *
 * Erlaubt ein akkusparendes Synchronisieren von Serverdaten.
 *
 * @author Gianni
 * @since 0.6.8
 * @version 2017.0712
 */
@SuppressWarnings("unused")
class ReceiveSyncAdapter extends AbstractThreadedSyncAdapter {

    private Synchronizer[] synchronizers;
    private static final String TAG = ReceiveSyncAdapter.class.getSimpleName();

    {
        synchronizers = new Synchronizer[]{new NewsSynchronizer(), new SurveySynchronizer()};
    }

    ReceiveSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    ReceiveSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        for(Synchronizer s : synchronizers)
            if(s.run())
                s.postUpdate();
    }
}
