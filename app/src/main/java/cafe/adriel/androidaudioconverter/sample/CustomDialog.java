package cafe.adriel.androidaudioconverter.sample;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import cafe.adriel.androidaudioconverter.model.AudioFormat;
public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {
    AudioFormat format;
    public ConvertFile convertFile;
    public Dialog dialog;
    public TextView convert,cancel;
    public EditText filename;
    Spinner spinner;
    public CustomDialog(ConvertFile a) {
        super(a);
        this.convertFile = a;
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);
        filename=findViewById(R.id.etFilename);
        filename.setText("Audio_Converter_"+convertFile.selectedFile.getName());
        spinner=findViewById(R.id.sFormat);
        Spinner spinner =findViewById(R.id.sFormat);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(convertFile,
                R.array.formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                  if(i==0)
                                                  {
                                                     format=AudioFormat.MP3;
                                                  }
                                                  else  if(i==1)
                                                  {
                                                      format=AudioFormat.WAV;
                                                  }
                                                  else  if(i==2)
                                                  {
                                                       format=AudioFormat.AAC;
                                                  }
                                                  else  if(i==3)
                                                  {
                                                      format=AudioFormat.M4A;
                                                  }
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> adapterView) {

                                              }
                                          }

        );
        convert = (TextView) findViewById(R.id.tvConvert);
        cancel = (TextView) findViewById(R.id.tvCancel);
        convert.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvConvert:
                convertFile.convertAudio(filename.getText().toString(),format);
                break;
            case R.id.tvCancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }


}
