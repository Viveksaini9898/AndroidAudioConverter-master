package cafe.adriel.androidaudioconverter.sample;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
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
    public ConvertActivity afterConvert;
    public Button convert,cancel;
    public EditText filename;
    public TextView tv_exist,tvVolume;
    Spinner formatSpinner,bitrate;
    String bitrateSelected;
    SeekBar volumeSeekbar;
    float targetVolume=1;
    int fileBitrate;
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
        volumeSeekbar=findViewById(R.id.volume);
        tvVolume=findViewById(R.id.measure_of_volume);
        String nameHint=c.selectedFile.getName();
        int length=nameHint.length();
        filename.setText("Audio_Converter_"+nameHint.substring(0,length-4));
        filename. setSelectAllOnFocus(true);
        formatSpinner=findViewById(R.id.format);
        tv_exist=findViewById(R.id.tx_exist);
       bitrate =findViewById(R.id.bitrate);
       volumeSeekbar.setMax(300);
       volumeSeekbar.setProgress(100);
       MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(c.selectedFile.getPath());// the adresss location of the sound on sdcard.
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mf = mex.getTrackFormat(0);
        fileBitrate = mf.getInteger(MediaFormat.KEY_BIT_RATE)/1000;
        ArrayList<String> spinnerArray= new ArrayList();
        spinnerArray.add("copy("+fileBitrate+"kb/s)");
        spinnerArray.add("32kb/s CBR");
        spinnerArray.add("64kb/s CBR");
        spinnerArray.add("128kb/s CBR");
        spinnerArray.add("192kb/s CBR");
        spinnerArray.add("256kb/s CBR");
        spinnerArray.add("320kb/s CBR");
        spinnerArray.add("190kb/s VBR");
        spinnerArray.add("245kb/s VBR");
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
        filename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvVolume.setText(seekBar.getProgress()+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvVolume.setText(seekBar.getProgress()+"%");
                targetVolume=seekBar.getProgress()/100;
            }
    });
        bitrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                  if(i==0)
                                                  {
                                                      //bitrateSelected=fileBitrate+"k";
                                                      bitrateSelected="0";
                                                  }
                                                  else  if(i==1)
                                                  {
                                                      bitrateSelected="32k";
                                                  }
                                                  else if(i==2)
                                                  {
                                                      bitrateSelected="64k";
                                                  }
                                                  else  if(i==3)
                                                  {
                                                      bitrateSelected="128k";
                                                  }
                                                  else  if(i==4)
                                                  {
                                                      bitrateSelected="192k";
                                                  }
                                                  else  if(i==5)
                                                  {
                                                      bitrateSelected="256k";
                                                  }else  if(i==6)
                                                  {
                                                      bitrateSelected="320k";
                                                  }else  if(i==7)
                                                  {
                                                      bitrateSelected="190k";
                                                  }else  if(i==8)
                                                  {
                                                      bitrateSelected="245k";
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
                tv_exist.setVisibility(View.GONE);
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
                                                            format=AudioFormat.AAC;
                                                        }
                                                        else  if(i==2)
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
                String path=Environment.getExternalStorageDirectory()+File.separator+"Android Audio Converter"+File.separator+"Audio cutter"+File.separator;
                File file=new File(path);
                if(!file.exists())
                    file.mkdirs();
                File wavFile=new File(path,filename.getText().toString()+".mp3");
                if(wavFile.exists())
                {
                   tv_exist.setVisibility(View.VISIBLE);
                }
                else {
                    Intent intent=new Intent(c,ConvertActivity.class);
                    intent.putExtra("inputfile",c.selectedFile);
                    intent.putExtra("durationLeft",c.duration_left);
                    intent.putExtra("durationRight",c.durationRight);
                    intent.putExtra("format",format);
                    intent.putExtra("bitrate",bitrateSelected);
                    intent.putExtra("outputfile",wavFile);
                    intent.putExtra("difference",c.difference);
                    intent.putExtra("volume",targetVolume);
                    c.startActivity(intent);

                   // c.convertAudio(format, wavFile,bitrateSelected,targetVolume);
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
