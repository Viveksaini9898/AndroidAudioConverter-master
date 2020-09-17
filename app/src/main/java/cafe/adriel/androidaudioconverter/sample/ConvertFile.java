package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.semantive.waveformandroid.waveform.view.WaveformView;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class ConvertFile extends AppCompatActivity {
    int min, max, difference;
    TextView left, right, differenceText;
    RangeSeekBar rangeSeekBar;
    long duration;
    String duration_left, durationRight;
    File selectedFile;
    private WaveformView waveformView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor);
        left = (TextView) findViewById(R.id.leftText);
        right = (TextView) findViewById(R.id.rightText);
        differenceText = (TextView) findViewById(R.id.difference);
        duration = Integer.parseInt(getIntent().getStringExtra("duration"));
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeseekbar);
        waveformView=findViewById(R.id.waveform);
        rangeSeekBar.setRangeValues(0, duration);
        Bundle extras = getIntent().getExtras();
        Uri uri = (Uri) extras.get("item");
        selectedFile = new File(getRealPathFromURI(uri));
        durationRight = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        left.setText("00:00:00");
        right.setText(durationRight);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                min = (int) bar.getSelectedMinValue();
                max = (int) bar.getSelectedMaxValue();
                difference = max - min;
                String differenceString = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(difference),
                        TimeUnit.MILLISECONDS.toMinutes(difference) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(difference)),
                        TimeUnit.MILLISECONDS.toSeconds(difference) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(difference)));
                duration_left = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(min),
                        TimeUnit.MILLISECONDS.toMinutes(min) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(min)),
                        TimeUnit.MILLISECONDS.toSeconds(min) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(min)));
                durationRight = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(max),
                        TimeUnit.MILLISECONDS.toMinutes(max) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(max)),
                        TimeUnit.MILLISECONDS.toSeconds(max) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(max)));
                left.setText(duration_left);
                right.setText(durationRight);
                differenceText.setText(differenceString);
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        }

        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                CustomDialog alertDialog = new CustomDialog(ConvertFile.this);
                alertDialog.show();
                return true;
            }
        });
        return super.onOptionsItemSelected(item);
    }

    public void convertAudio(String output_file_name,AudioFormat format) {
        File wavFile = new File(Environment.getExternalStorageDirectory(), output_file_name);
        if(wavFile.exists())
        {
            Toast.makeText(getApplicationContext(),"file name already exists",Toast.LENGTH_LONG).show();
        }
        else {
            Log.d("filepath", selectedFile.getPath());
            Toast.makeText(getApplicationContext(), selectedFile.getPath(), Toast.LENGTH_LONG).show();
            IConvertCallback callback = new IConvertCallback() {
                @Override
                public void onSuccess(File convertedFile) {
                    Toast.makeText(ConvertFile.this, "SUCCESS: " + convertedFile.getPath(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(ConvertFile.this, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("Error", error.toString());
                }
            };
            Toast.makeText(ConvertFile.this, "Converting audio file...", Toast.LENGTH_SHORT).show();
            Log.d("Filename", selectedFile.getPath());

            if (selectedFile != null) {
                AndroidAudioConverter.with(ConvertFile.this)
                        .setInputFile(selectedFile)
                        .setOutputFile(wavFile)
                        .setFormat(format)
                        .setCallback(callback)
                        .setDuration(duration_left, durationRight)
                        .convert();
            } else {
                Toast.makeText(ConvertFile.this, "File not passed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
