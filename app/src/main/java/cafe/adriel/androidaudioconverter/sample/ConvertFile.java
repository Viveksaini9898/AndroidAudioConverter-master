package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class ConvertFile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        }

        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public String getRealPathFromURI (Uri contentUri) {
        String path = null;
        String[] proj = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }
    public void convertAudio(View v){
        /**
         *  Update with a valid audio file!
         *  Supported formats: {@link AndroidAudioConverter.AudioFormat}
         */
        File wavFile = new File(Environment.getExternalStorageDirectory(), "recorded_audio.wav");
        Bundle extras=getIntent().getExtras();
        Uri uri=(Uri) extras.get("item");
        File selectedFile=new File(getRealPathFromURI(uri));
        Toast.makeText(ConvertFile.this,selectedFile.getPath(),Toast.LENGTH_SHORT).show();
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                Toast.makeText(ConvertFile.this, "SUCCESS: " + convertedFile.getPath(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Exception error) {
                Toast.makeText(ConvertFile.this, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        Toast.makeText(this, "Converting audio file...", Toast.LENGTH_SHORT).show();
        if(selectedFile!=null) {
            AndroidAudioConverter.with(this)
                    .setFile(selectedFile)
                    .setFormat(AudioFormat.MP3)
                    .setCallback(callback)
                    .convert();
        }
        else
        {
            Toast.makeText(ConvertFile.this,"File not passed", Toast.LENGTH_LONG).show();
        }
    }

}