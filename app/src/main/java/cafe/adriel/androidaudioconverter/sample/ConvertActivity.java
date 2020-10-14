package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.sample.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.sample.callback.IConvertCallback;

public class ConvertActivity extends AppCompatActivity implements Runnable {

    TextView convertfilename, textview,txBitrate,txSize,progressPercent;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    ProgressBar progressIndicator;
    boolean wasPlaying = false;
    ImageView play_pause_btn, share, openwith, setas;
    String duration;
    File outputFile,inputFile;
    AudioFormat format;
    String durationLeft,durationRight;
    int difference;
    float volume;
    int position, currentPosition;
    String bitrate;
    Button backgroundConvert;
    View settings,playerDisplay,progressDisplay;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convertlayout);
        convertfilename = findViewById(R.id.convertfilename);
        seekBar = findViewById(R.id.seekbar);
        play_pause_btn = findViewById(R.id.button);
        progressPercent=findViewById(R.id.progressPercent);
        textview = findViewById(R.id.textView);
        share = findViewById(R.id.share);
        progressIndicator=findViewById(R.id.progressIndicator);
        settings=findViewById(R.id.settings);
        backgroundConvert=findViewById(R.id.background);
        playerDisplay=findViewById(R.id.playerDisplay);
        progressDisplay=findViewById(R.id.progressDisplay);
        openwith = findViewById(R.id.openwith);
        setas = findViewById(R.id.setas);
        txSize=findViewById(R.id.size);
        txBitrate=findViewById(R.id.bitrate);
        Bundle extras = getIntent().getExtras();
        bitrate=extras.get("bitrate").toString();
        inputFile=(File)extras.get("inputfile");
        outputFile =(File)extras.get("outputfile");
        format=(AudioFormat) extras.get("format");
        volume=(float) extras.get("volume");
        durationLeft=extras.getString("durationLeft");
        difference=(int)extras.get("difference");
        durationRight=extras.getString("durationRight");
        convertfilename.setText(outputFile.getName());
        txBitrate.setText(" | "+bitrate+"b/s");
        progressIndicator.setMax(difference);
        convertAudio(format,outputFile,bitrate,volume);
        setListeners();
    }
    private void setListeners() {
        backgroundConvert.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {
                                                     Intent intent = new Intent(ConvertActivity.this,SelectorActivity.class);
                                                        intent.putExtra("added",true);
                                                     startActivity(intent);
                                                 }
                                             }
        );
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                Uri screenshotUri = Uri.parse(outputFile.toString());
                sendIntent.setType("audio/*");
                sendIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                Intent.createChooser(sendIntent, "Share via");
                startActivity(sendIntent);
            }
        });
        openwith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(outputFile.toString());
                Log.d("uri",uri.toString());
                Intent it = new Intent();
                it.setAction(android.content.Intent.ACTION_VIEW);
                it.setDataAndType(uri, "audio/*");
                ConvertActivity.this.startActivity(it);
            }
        });
        setas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSystemWritePermission()){
                    CustomDialogForSetAs alertDialog = new CustomDialogForSetAs(ConvertActivity.this);
                    alertDialog.show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Please, allow system settings ", Toast.LENGTH_LONG).show();
            }
        });
        play_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                textview.setText(Time_Conversion_in_minsec(progress) + "/" + duration);
                if (progress == seekBar.getMax()) {
                    seekBar.setProgress(0);
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    public void playSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            wasPlaying = true;
            play_pause_btn.setImageResource(android.R.drawable.ic_media_play);
        } else if (!wasPlaying) {
            play_pause_btn.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayer.setVolume(0.5f, 0.5f);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
            new Thread(this).start();
        }
        wasPlaying = false;
    }
    public void convertAudio(AudioFormat format, File wavFile,  String bitrateSelected,  float volume) {
        String pathDebug=wavFile.getPath();
        Log.d("filepath",pathDebug);
        Toast.makeText(getApplicationContext(), inputFile.getPath(), Toast.LENGTH_LONG).show();
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                refreshGallery(convertedFile.getPath(),ConvertActivity.this);
                Toast.makeText(ConvertActivity.this, "SUCCESS: " + convertedFile.getPath(), Toast.LENGTH_LONG).show();
               outputFile=convertedFile;
                long size_long= outputFile.length();
                progressDisplay.setVisibility(View.GONE);
                txSize.setVisibility(View.VISIBLE);
                playerDisplay.setVisibility(View.VISIBLE);
                settings.setVisibility(View.VISIBLE);
                txSize.setText(format(size_long,1));
                //absolute_path=Environment.getExternalStorageDirectory()+extras.get("converted filename").toString();
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                seekBar.setMax(mediaPlayer.getDuration());
                duration = Time_Conversion_in_minsec(mediaPlayer.getDuration());
                textview.setText(String.format("%02d:%02d", 00, 00) + "/" + duration);
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(ConvertActivity.this,error.toString(), Toast.LENGTH_LONG).show();
                Log.d("checkandresolve", error.toString());
            }
        };
        Log.d("Filename", inputFile.getPath());
        if (inputFile != null) {
            AndroidAudioConverter.with(ConvertActivity.this,progressIndicator,progressPercent)
                    .setInputFile(inputFile)
                    .setOutputFile(wavFile)
                    .setdurations(difference)
                    .setFormat(format)
                    .setCallback(callback)
                    .setDuration(durationLeft, durationRight)
                    .setBitrate(bitrateSelected)
                    .setVolume(String.valueOf(volume))
                    .convert();
        } else {
            Toast.makeText(ConvertActivity.this, "File not passed", Toast.LENGTH_LONG).show();
        }
    }
    void refreshGallery(String path, Context context) {
        File file = new File(path);
        try {
            Intent mediaScanIntent =new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        if(mediaPlayer!=null)
        mediaPlayer.pause();
        super.onPause();
    }

    public void run() {
        currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            seekBar.setProgress(currentPosition);
        }
        play_pause_btn.setImageResource(android.R.drawable.ic_media_play);
    }
    private boolean checkSystemWritePermission() {
        boolean retVal=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
            if (retVal == false) {
                if (!Settings.System.canWrite(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    //Toast.makeText(getApplicationContext(), "Please, allow system settings ", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
            }
        }
        else {

                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS},1);
        }
        return retVal;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.menu2, menu);
        MenuItem item = menu.getItem(0);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent i = new Intent(ConvertActivity.this, SelectorActivity.class);
                startActivity(i);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null)
        mediaPlayer.pause();
    }

    private String Time_Conversion_in_minsec(int milisec){
        int x = (int) Math.ceil(milisec / 1000f);
        int min = (x % 3600) / 60;
        int sec = x % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public void setTone(int i) {
        ContentValues values = new ContentValues(i);
        values.put(MediaStore.MediaColumns.DATA, outputFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, outputFile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.SIZE, outputFile.length());
        if(i==1)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        else if(i==2)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        else if(i==4)
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outputFile.getAbsolutePath());
        getContentResolver().delete(
                uri,
                MediaStore.MediaColumns.DATA + "=\""
                        + outputFile.getAbsolutePath() + "\"", null);
        Uri newUri = this.getContentResolver().insert(uri, values);
/*
        RingtoneManager ringtoneManager=new RingtoneManager(this);
*/
        try {
            /*Uri rUri = RingtoneManager.getValidRingtoneUri(this);
            if (rUri != null)
                ringtoneManager.setStopPreviousRingtone(true);*/
            RingtoneManager.setActualDefaultRingtoneUri(this,i,newUri);
        } catch (Throwable t) {
            Log.e("exception", "ringtone not set");
        }
        this.getContentResolver().insert(uri, values);
    }
    public static String format(double bytes, int digits) {
        String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }
}


