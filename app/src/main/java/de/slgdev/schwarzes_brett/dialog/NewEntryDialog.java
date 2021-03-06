package de.slgdev.schwarzes_brett.dialog;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.slgdev.leoapp.R;
import de.slgdev.leoapp.task.general.TaskStatusListener;
import de.slgdev.leoapp.utility.Utils;
import de.slgdev.schwarzes_brett.task.SendEntryTask;

/**
 * Nachrichtendialog
 * <p>
 * Dieser Dialog stellt eine Möglichkeit dar, Schwarzes-Brett Nachrichten innerhalb der App zu verfassen.
 *
 * @version 2017.2410
 * @since 0.5.5
 */
public class NewEntryDialog extends AlertDialog implements TaskStatusListener {

    private DatePickerDialog datePickerDialog;

    /**
     * Konstruktor.
     *
     * @param context Kontextobjekt
     */
    public NewEntryDialog(@NonNull Context context) {
        super(context);
    }

    /**
     * Dialog Inputs werden initialisiert.
     *
     * @param b Metadaten
     */
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.dialog_create_entry);
        initDatePicker();
        initSpinner();
        initTextViews();
        initButtons();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void initTextViews() {
        final Button   submit = findViewById(R.id.buttonSave);
        final TextView t1     = findViewById(R.id.title_edittext);
        final TextView t2     = findViewById(R.id.eingabeDatum);
        final TextView t3     = findViewById(R.id.content);

        TextWatcher listener = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submit.setEnabled(t1.getText().length() > 0 && t2.getText().length() > 0 && t3.getText().length() > 0);
            }
        };

        t1.addTextChangedListener(listener);
        t2.addTextChangedListener(listener);
        t3.addTextChangedListener(listener);
    }

    private void initButtons() {
        findViewById(R.id.buttonDel).setOnClickListener(v -> dismiss());

        findViewById(R.id.buttonSave).setOnClickListener(v -> {
            final TextView title = findViewById(R.id.title_edittext);

            final TextView content = findViewById(R.id.content);
            final TextView date    = findViewById(R.id.eingabeDatum);

            final String     OLD_FORMAT = "dd-MM-yyyy";
            final String     NEW_FORMAT = "yyyy-MM-dd";
            String           newDate;
            SimpleDateFormat sdf        = new SimpleDateFormat(OLD_FORMAT, Locale.GERMANY);
            String           dateString = date.getText().toString();
            Date             d          = null;
            try {
                d = sdf.parse(dateString);
            } catch (ParseException e) {
                Utils.logError(e);
            }
            sdf.applyPattern(NEW_FORMAT);
            newDate = sdf.format(d);
            Spinner spinner = findViewById(R.id.spinner2);
            Utils.logError(spinner.getSelectedItem().toString());
            new SendEntryTask().addListener(this).execute(title.getText().toString(), content.getText().toString(), newDate, spinner.getSelectedItem().toString());
        });
    }

    private void initDatePicker() {
        ImageButton dateButton = findViewById(R.id.imageButton);
        TextView    dateText   = findViewById(R.id.eingabeDatum);
        setDateTimeField(dateText);
        View.OnClickListener listener = v -> datePickerDialog.show();
        dateButton.setOnClickListener(listener);
        dateText.setOnClickListener(listener);
    }

    private void setDateTimeField(final TextView t) {
        final Calendar         newCalendar   = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);
        datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            t.setText(dateFormatter.format(newDate.getTime()));
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void initSpinner() {

        Spinner s = findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Utils.getContext(),
                R.array.level, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        s.setAdapter(adapter);
    }

    @Override
    public void taskFinished(Object... b) {
        if ((Boolean) b[0]) {
            dismiss();
            Toast.makeText(Utils.getContext(), R.string.sent, Toast.LENGTH_SHORT).show();
        } else {
            final Snackbar snack = Snackbar.make(findViewById(R.id.dialog_entry), Utils.getString(R.string.snackbar_no_connection_info), Snackbar.LENGTH_LONG);
            snack.setActionTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            snack.setAction(getContext().getString(R.string.confirm), v -> snack.dismiss());
            snack.show();
        }
    }

}
