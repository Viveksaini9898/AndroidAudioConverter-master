package cafe.adriel.androidaudioconverter.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class CustomDialogForSetAs extends Dialog implements android.view.View.OnClickListener {

    TextView cancel,set;
    RadioGroup radioGroup;
    ConvertActivity convertActivity;
    RadioButton alarm,ringtone,notification;

    public CustomDialogForSetAs(ConvertActivity a) {
        super(a);
        this.convertActivity=a;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_for_setas);
        cancel=findViewById(R.id.cancel);
        set=findViewById(R.id.set);
        radioGroup=findViewById(R.id.radioGroup);
        ringtone=findViewById(R.id.ringtone);
        alarm=findViewById(R.id.alarm);
        notification=findViewById(R.id.notification);

        set.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set:
                if(ringtone.isChecked()) {
                    convertActivity.setTone(1);
                    Toasty.success(getContext(), "Ringtone Set Successfully", Toast.LENGTH_LONG, true).show();
                }
                else if (alarm.isChecked()){
                    convertActivity.setTone(4);
                    Toasty.success(getContext(), "Alarm Tone Set Successfully", Toast.LENGTH_LONG, true).show();
                }
                else if(notification.isChecked()){
                    convertActivity.setTone(2);
                    Toasty.success(getContext(), "Notification Tone Set Successfully", Toast.LENGTH_LONG, true).show();
                }
                break;
            case R.id.cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
