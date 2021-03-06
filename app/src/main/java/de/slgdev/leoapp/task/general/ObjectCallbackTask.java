package de.slgdev.leoapp.task.general;

import android.os.AsyncTask;

import de.slgdev.leoapp.utility.datastructure.List;

/**
 * StringCallbackTask.
 *
 * Klasse für AsyncTasks, die ihren Status an Listener mitteilen müssen und einen
 * Eingangsdatentyp haben.
 *
 * @param <OutputType> Datentyp des Ergebnisobjekts
 *
 * @author Gianni
 * @since 0.7.2
 * @version 2018.1601
 */
public abstract class ObjectCallbackTask<OutputType> extends AsyncTask<Object, Void, OutputType> {

    private final List<TaskStatusListener> listeners;

    {
        listeners = new List<>();
    }

    public final ObjectCallbackTask addListener(TaskStatusListener listener) {
        listeners.append(listener);
        return this;
    }

    protected final List<TaskStatusListener> getListeners() {
        return listeners;
    }

    /**
     * Teilt Listenern das Ende des Tasks mit. Überschreibende Methodens sollten diese Funktionalität
     * implementieren.
     *
     * @param result Ergebnis des Tasks
     */
    @Override
    protected void onPostExecute(OutputType result) {
        for (TaskStatusListener l : listeners)
            l.taskFinished();
    }

    public void execute() {
        super.execute();
    }

}
