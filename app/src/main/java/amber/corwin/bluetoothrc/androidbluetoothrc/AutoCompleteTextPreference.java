package amber.corwin.bluetoothrc.androidbluetoothrc;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * Created by corwin on 16/10/2017.
 */

public class AutoCompleteTextPreference extends ListPreference {

    private AutoCompleteTextView mEditText = null;

    @TargetApi(21)
    public AutoCompleteTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mEditText = new AutoCompleteTextView(context, attrs);
        mEditText.setThreshold(0);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, new String[]{"first", "second"});
        mEditText.setAdapter(adapter);

        //setDialogLayoutResource(R.layout.pref_combo);
    }

    public AutoCompleteTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0); //R.style.DialogPreference);
    }

    public AutoCompleteTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.comboPrefStyle);
    }

    public AutoCompleteTextPreference(Context context) {
        this(context, null);
    }

        /*public AutoCompleteTextPreference(Context context) {
        super(context);
    }*/

        /*
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setSingleChoiceItems(new CharSequence[] { "first", "second" }, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }*/

    @Override
    protected void onBindDialogView(View view) {
        AutoCompleteTextView editText = mEditText;
        //editText.setText(getText());

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            //onAddEditTextToDialogView(view, editText);
            ((ViewGroup) view).addView(mEditText);
        }
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && getValue().equals("Other:")) {
            setValue(mEditText.getText().toString());
        }
    }

}
