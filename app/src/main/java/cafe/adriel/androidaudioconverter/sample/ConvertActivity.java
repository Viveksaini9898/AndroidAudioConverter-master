package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.provider.Telephony.Mms.Addr.CONTACT_ID;

public class ConvertActivity extends AppCompatActivity implements Runnable {

    TextView convertfilename, textview;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    boolean wasPlaying = false;
    ImageView play_pause_btn, share, openwith, setas;
    String duration,songname;
    File convertedFilepath;
    int position, currentPosition;
    String absolute_path;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convertlayout);
        convertfilename = findViewById(R.id.convertfilename);
        seekBar = findViewById(R.id.seekbar);
        play_pause_btn = findViewById(R.id.button);
        textview = findViewById(R.id.textView);
        share = findViewById(R.id.share);
        openwith = findViewById(R.id.openwith);
        setas = findViewById(R.id.setas);
        Bundle extras = getIntent().getExtras();
        songname=extras.get("converted filename").toString();
        convertfilename.setText(songname);
        convertedFilepath = new File(Environment.getExternalStorageDirectory(), extras.get("converted filename").toString());
         absolute_path=Environment.getExternalStorageDirectory()+extras.get("converted filename").toString();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(String.valueOf(convertedFilepath));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        duration = Time_Conversion_in_minsec(mediaPlayer.getDuration());
        textview.setText(String.format("%02d:%02d", 00, 00) + "/" + duration);
        seekBar.setMax(mediaPlayer.getDuration());
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                Uri screenshotUri = Uri.parse(convertedFilepath.toString());
                sendIntent.setType("audio/*");
                sendIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                Intent.createChooser(sendIntent, "Share via");
                startActivity(sendIntent);
            }
        });
        openwith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(convertedFilepath.toString());
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

    @Override
    protected void onPause() {
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
                Intent i = new Intent(ConvertActivity.this, MainActivity.class);
                startActivity(i);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    protected void onDestroy() {
        super.onDestroy();
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
        values.put(MediaStore.MediaColumns.DATA, convertedFilepath.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, songname);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.SIZE,convertedFilepath.length());
        if(i==1)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        else if(i==2)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        else if(i==4)
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(convertedFilepath.getAbsolutePath());
        getContentResolver().delete(
                uri,
                MediaStore.MediaColumns.DATA + "=\""
                        + convertedFilepath.getAbsolutePath() + "\"", null);
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

}


