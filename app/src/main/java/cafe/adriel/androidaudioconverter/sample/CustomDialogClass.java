package cafe.adriel.androidaudioconverter.sample;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {
    AudioFormat format;
    public ConvertFile c;
    public Dialog d;
    public Button convert,cancel;
    public EditText filename;
    Spinner spinner;
    public CustomDialogClass(ConvertFile a) {
        super(a);

        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialoglayout);
        filename=findViewById(R.id.filename);
        filename.setText("Audio_Converter_"+c.selectedFile.getName());
        spinner=findViewById(R.id.format);
        Spinner spinner =findViewById(R.id.format);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c,
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
        convert = (Button) findViewById(R.id.convert);
        cancel = (Button) findViewById(R.id.cancel);
        convert.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.convert:
                c.convertAudio(filename.getText().toString(),format);
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
