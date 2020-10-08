package cafe.adriel.androidaudioconverter.sample;

import android.app.Activity;
import android.app.Dialog;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cafe.adriel.androidaudioconverter.model.AudioFormat;
public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {
    AudioFormat format;
    public ConvertFile c;
    public EditText filename;
    public TextView tvexist,convert,cancel;
    Spinner formatSpinner,bitrate;
    String bitrateSelected;
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
        String nameHint=c.selectedFile.getName();
        int length=nameHint.length();
        filename.setText("Audio_Converter_"+nameHint.substring(0,length-4));
        formatSpinner=findViewById(R.id.format);
        tvexist=findViewById(R.id.txexist);
       bitrate =findViewById(R.id.bitrate);
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(c.selectedFile.getPath());// the adresss location of the sound on sdcard.
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mf = mex.getTrackFormat(0);
        int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE)/1000;
        ArrayList<String> spinnerArray= new ArrayList();
        spinnerArray.add("copy("+bitRate+"kb/s)");
        spinnerArray.add("32kb/s CBR");
        spinnerArray.add("64kb/s CBR");
        spinnerArray.add("128kb/s CBR");
        spinnerArray.add("192kb/s CBR");
        spinnerArray.add("256kb/s CBR");
        spinnerArray.add("320kb/s CBR");
        ArrayAdapter<String> bitrateSpinner= new ArrayAdapter(c, android.R.layout.simple_spinner_item,spinnerArray);
        bitrateSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bitrate.setAdapter(bitrateSpinner);
        setListeners();
        convert =  findViewById(R.id.convert);
        cancel =  findViewById(R.id.cancel);
        convert.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    private void setListeners() {
        bitrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                  if(i==0)
                                                  {
                                                      bitrateSelected="32kb/s";
                                                  }
                                                  else  if(i==1)
                                                  {
                                                      bitrateSelected="64kb/s";
                                                  }
                                                  else  if(i==2)
                                                  {
                                                      bitrateSelected="128kb/s";
                                                  }
                                                  else  if(i==3)
                                                  {
                                                      bitrateSelected="192kb/s";
                                                  }
                                                  else  if(i==4)
                                                  {
                                                      bitrateSelected="256kb/s";
                                                  }else  if(i==5)
                                                  {
                                                      bitrateSelected="320kb/s";
                                                  }else  if(i==6)
                                                  {
                                                      bitrateSelected="190kb/s";
                                                  }else  if(i==7)
                                                  {
                                                      bitrateSelected="245kb/s";
                                                  }
                                              }
                                              @Override
                                              public void onNothingSelected(AdapterView<?> adapterView) {

                                              }
                                          }

        );
        filename.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                tvexist.setVisibility(View.GONE);
            }
        });
        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.convert:
                File wavFile=new File(Environment.getExternalStorageDirectory(),filename.getText().toString()+".mp3");
                if(wavFile.exists())
                {
                   tvexist.setVisibility(View.VISIBLE);
                }
                else {
                    c.convertAudio(format, wavFile);
                }
                break;
            case R.id.cancel:
                dismiss();
                break;
            default:
                break;
        }
    }


}
