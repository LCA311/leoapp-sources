package de.slg.leoapp.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import de.slg.leoapp.R;

/**
 * EditTextDialog.
 * <p>
 * Einfacher Dialog mit Texteingabefeld
 *
 * @author Gianni, Moritz
 * @since 0.5.7
 * @version 2017.1211
 */
public class EditTextDialog extends AlertDialog {

    private EditText editText;
    private String   hint;

    private View.OnClickListener action;

    /**
     * Konstruktur. Initialisiert den EditText-Dialog mit einem OnClickListener für den OK Button und einem Hint für das Textfeld.
     *
     * @param context Kontextobjekt (Aktive Activity)
     * @param hint Hint für Textfeld
     * @param action OnClickListener für den OK-Button
     */
    public EditTextDialog(@NonNull Context context, @NonNull String hint, @NonNull View.OnClickListener action) {
        super(context);
        this.hint = hint;
        this.action = action;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_text);

        editText = (EditText) findViewById(R.id.editText);
        editText.setHint(hint);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.ok).setOnClickListener(action);
    }

    /**
     * Liefert den Inhalt des EditText Feldes als String.
     *
     * @return User-Eingabe
     */
    public String getTextInput() {
        return editText.getText().toString();
    }

    /**
     * Setzt den InputType des EditText Feldes auf einen per Parameter übergebenen Wert.
     *
     * @param type Neuer InputType
     */
    public void setInputType(int type) {
        editText.setInputType(type);
    }

}