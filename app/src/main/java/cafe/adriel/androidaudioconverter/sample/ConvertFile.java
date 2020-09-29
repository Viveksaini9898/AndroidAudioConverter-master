package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.ICUUncheckedIOException;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import es.dmoral.toasty.Toasty;

public class ConvertFile extends AppCompatActivity implements MarkerView.MarkerListener {
    int min, max, difference;
    TextView left, right, differenceText,minusLeft,plusLeft,minusRight,plusRight;
    long duration;
    ImageView playImage,playFromLeft;
    String duration_left, durationRight;
    File selectedFile;
    private int mStartPos;
    private int mEndPos;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private Handler mHandler;
    MediaPlayer mediaPlayer;
    SeekBar sbChangeSeekPosition;
    private boolean mTouchDragging;
    cafe.adriel.androidaudioconverter.sample.MarkerView mStartMarker,mEndMarker,lastUpdatedMarker;
    AudioWaveformView audioWaveformView;
    private int width=0,minOffset=20;
    private float mTouchStart;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private int rightX,rightY,leftX,musicPointerX;
    private int currentSongPosition;
    View leftView,rightView;
    private boolean wasPlaying=false;
    private boolean trapped=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert);
        minusLeft=findViewById(R.id.minusLeft);
        plusLeft=findViewById(R.id.plusLeft);
        minusRight=findViewById(R.id.minusRight);
        plusRight=findViewById(R.id.plusRight);
        left =  findViewById(R.id.leftText);
        right =  findViewById(R.id.rightText);
        playImage= findViewById(R.id.play);
        playFromLeft= findViewById(R.id.play_from_left);
        sbChangeSeekPosition=findViewById(R.id.sbChangeSeekPosition);
        differenceText =findViewById(R.id.difference);
        duration=Integer.parseInt(getIntent().getStringExtra("duration"));
        audioWaveformView=findViewById(R.id.waveform);
        leftView=findViewById(R.id.viewLeft);
        rightView=findViewById(R.id.viewRight);
        mStartMarker=findViewById(R.id.leftThumb);
        mEndMarker=findViewById(R.id.rightThumb);
        mStartMarker.setListener(this);
        mEndMarker.setListener(this);
        mHandler=new Handler();
        Bundle extras = getIntent().getExtras();
        Uri uri = (Uri) extras.get("item");
        selectedFile = new File(getRealPathFromURI(uri));
        durationRight = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        max= (int) duration;
        sbChangeSeekPosition.setMax(max);
        try {
            wasPlaying=false;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(selectedFile.getPath());
            mediaPlayer.prepare();
            mediaPlayer.setLooping(false);
        }
        catch(IOException e)
        {
        }
        left.setText("00:00:00");
        duration_left="00:00:00";
        right.setText(durationRight);
        setListeners();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        }

        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public void setListeners()
    {
        minusLeft.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if(min>0) {
                                                 min -= 1000;
                                                 int minPercent = (int) (min * 100 / duration);
                                                 mStartPos = trap(648* minPercent / 100);
                                                 changeMinText();
                                                 lastUpdatedMarker = mStartMarker;
                                                 updateDisplay();
                                             }
                                         }
                                     }
        );
        plusLeft.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if(min<max) {
                                                 min += 1000;
                                                 int minPercent = (int) (min * 100 / duration);
                                                 mStartPos = trap(648 * minPercent / 100);
                                                 changeMinText();
                                                 lastUpdatedMarker = mStartMarker;
                                                 updateDisplay();
                                             }
                                             int progressPercent= (int) (sbChangeSeekPosition.getProgress()*100/duration);
                                             int startPercent= (int) (mStartPos*100/648);
                                             if(progressPercent<startPercent)
                                             {
                                                 int progress_to_set= (int) (startPercent*duration/100);
                                                 sbChangeSeekPosition.setProgress(progress_to_set);
                                                 mediaPlayer.seekTo(progress_to_set);
                                             }
                                         }
                                     }
        );
        minusRight.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if(max>min) {
                                                 max -= 1000;
                                                 int maxPercent = (int) (max * 100 / duration);
                                                 mStartPos =trap( 647* maxPercent / 100);
                                                 changeMaxText();
                                                 lastUpdatedMarker = mEndMarker;
                                                 updateDisplay();
                                             }
                                         }
                                     }
        );
        plusRight.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(max<duration) {
                                                max += 1000;
                                                int maxPercent = (int) (max * 100 / duration);
                                                mStartPos = trap(647* maxPercent / 100);
                                                lastUpdatedMarker = mEndMarker;
                                                updateDisplay();
                                                if(trapped)
                                                {
                                                    max--;
                                                    trapped=false;
                                                }
                                              changeMaxText();
                                            }
                                        }
                                    }
        );
        playImage.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if(mediaPlayer==null) {
                                                 try {
                                                     mediaPlayer = new MediaPlayer();
                                                     mediaPlayer.setDataSource(selectedFile.getPath());
                                                     mediaPlayer.prepare();
                                                     mediaPlayer.setLooping(false);
                                                 } catch (IOException e) {
                                                     Toasty.normal(getApplicationContext(),"something went wrong with this file");
                                                 }
                                             }
                                             if(mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
                                                 mediaPlayer.start();
                                                 playImage.setImageResource(R.drawable.pause);
                                                 star();
                                             }
                                             else
                                             {
                                                 mediaPlayer.pause();
                                                 playImage.setImageResource(R.drawable.play);
                                             }
                                         }
                                     });
        playFromLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer==null) {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(selectedFile.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.setLooping(false);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "something went wrong with this file", Toast.LENGTH_SHORT).show();
                    }
                }
                int percent = mStartMarker.getLeft();
                int minPercent = percent * 100 /648;
                 currentSongPosition = (int) (minPercent * duration / 100);
                sbChangeSeekPosition.setProgress(currentSongPosition);
                mediaPlayer.seekTo(currentSongPosition);
                    mediaPlayer.start();
                    upDateSeekBar();
                    playImage.setImageResource(R.drawable.pause);
            }
        });
        leftView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //sbChangeSeekPosition.setClickable(false);
                int x= (int) motionEvent.getX();
                //int y= (int) motionEvent.getY();

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                    mEndPos=mStartMarker.getTop();
                    params.setMargins(x, mEndPos, 0, 0);
                    mStartMarker.setLayoutParams(params);
                    RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams) leftView.getLayoutParams();
                    int lagging2=(mStartMarker.getRight()-mStartMarker.getLeft())/2;
                    int leftMargin2=mStartMarker.getLeft()-lagging2;
                    if(leftMargin2>700)
                        leftX=trap(leftMargin2);
                    else
                        leftX=leftMargin2;
                    param2.width=x;
                    param2.height= audioWaveformView.getHeight();
                    //sbChangeSeekPosition.setClickable(true);
                return false;
            }
        });
        /*audioWaveformView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int x= (int) motionEvent.getX();
                int minX=mStartMarker.getLeft()+(mStartMarker.getRight()-mStartMarker.getLeft())/2;
                if(x<minX)
                {
                    int pos_to_assign= (int) ((x/640*100)*duration/100);
                    sbChangeSeekPosition.setProgress(currentSongPosition);
                    lastUpdatedMarker=mStartMarker;
                    mStartPos=x;
                    updateDisplay();
                }
                return false;
            }
        });*/
        /*rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
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
        });*/
        /*leftLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                sbChangeSeekPosition.setProgress(seekBar.getProgress());
            }
        });*/
        sbChangeSeekPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    int percent = mEndMarker.getLeft();
                   int minPercent = percent * 100 / 645;
                    int currentProgress = seekBar.getProgress();
                    int progressPercent = (int) (currentProgress * 100 / duration);
                    if(progressPercent>=minPercent)
                    {
                        seekBar.setClickable(false);
                        seekBar.setAlpha(0f);
                    }
                 percent = mStartMarker.getLeft();
                 minPercent = percent * 100 / 645;
                 currentProgress = seekBar.getProgress();
                 progressPercent = (int) (currentProgress * 100 / duration);
                if(progressPercent<minPercent)
                {
                    seekBar.setClickable(false);
                    seekBar.setAlpha(0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int percent= mStartMarker.getLeft();
                int minPercent=percent*100/648;
                int currentProgress=seekBar.getProgress();
                int progressPercent= (int) (currentProgress*100/duration);
                if(progressPercent<minPercent) {
                    lastUpdatedMarker=mStartMarker;
                    mStartPos=trap(progressPercent*648/100);
                     currentProgress=mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentProgress);
                    progressPercent= (int) (currentProgress*100/duration);
                     minPercent=mStartPos*100/648;
                    if (progressPercent<minPercent) {
                        currentProgress= (int) (minPercent*duration/100);
                        seekBar.setProgress(currentProgress);
                        mediaPlayer.seekTo(currentProgress);
                        currentSongPosition=currentProgress;
                    }
                    min= (int) (minPercent*duration/100);
                    if(trapped)
                    {
                        min=0;
                        trapped=false;
                    }
                    changeMinText();
                    updateDisplay();
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                 percent=mEndMarker.getLeft();
                 minPercent=percent*100/648;
                 currentProgress=seekBar.getProgress();
                 progressPercent= (int) (currentProgress*100/duration);
                if(progressPercent>=minPercent) {
                    sbChangeSeekPosition.setProgress(mediaPlayer.getCurrentPosition());
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    lastUpdatedMarker=mEndMarker;
                    mStartPos=trap(progressPercent*648/100);
                    max= (int) (progressPercent*duration/100);
                    changeMaxText();
                    mediaPlayer.seekTo(seekBar.getProgress());
                    updateDisplay();
                }
                checkClickable();
                if(mediaPlayer!=null)
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    private void checkClickable() {
        if(!sbChangeSeekPosition.isClickable())
        {
            sbChangeSeekPosition.setClickable(true);
            sbChangeSeekPosition.setAlpha(1f);
        }
    }

    private void changeMaxText() {
        durationRight = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(max),
                TimeUnit.MILLISECONDS.toMinutes(max) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(max)),
                TimeUnit.MILLISECONDS.toSeconds(max) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(max)));
        right.setText(durationRight);
        right.setText(durationRight);
        difference = max - min;
        String differenceString = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(difference),
                TimeUnit.MILLISECONDS.toMinutes(difference) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(difference)),
                TimeUnit.MILLISECONDS.toSeconds(difference) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(difference)));
        differenceText.setText(differenceString);
    }
    private void changeMinText() {
        duration_left = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(min),
                TimeUnit.MILLISECONDS.toMinutes(min) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(min)),
                TimeUnit.MILLISECONDS.toSeconds(min) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(min)));
        left.setText(duration_left);
        difference = max - min;
        String differenceString = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(difference),
                TimeUnit.MILLISECONDS.toMinutes(difference) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(difference)),
                TimeUnit.MILLISECONDS.toSeconds(difference) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(difference)));
        differenceText.setText(differenceString);
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
        MenuItem item=menu.getItem(0);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                CustomDialogClass alertDialog = new CustomDialogClass(ConvertFile.this);
                alertDialog.show();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    public void convertAudio(AudioFormat format,File wavFile) {
            String pathDebug=wavFile.getPath();
            Log.d("filepath",pathDebug);
            Toast.makeText(getApplicationContext(), selectedFile.getPath(), Toast.LENGTH_LONG).show();
            IConvertCallback callback = new IConvertCallback() {
                @Override
                public void onSuccess(File convertedFile) {
                    Toast.makeText(ConvertFile.this, "SUCCESS: " + convertedFile.getPath(), Toast.LENGTH_LONG).show();
                    MediaScannerConnection.scanFile(
                            getApplicationContext(),
                            new String[]{ convertedFile.getPath() },
                           null,
                            new MediaScannerConnection.MediaScannerConnectionClient()
                            {
                                public void onMediaScannerConnected()
                                {
                                }
                                public void onScanCompleted(String path, Uri uri)
                                {
                                }
                            });
                    Intent intent=new Intent(ConvertFile.this,ConvertActivity.class);
                    intent.putExtra("converted filename",convertedFile.getName());
                    intent.putExtra("converted file",convertedFile);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(ConvertFile.this,"Note : you have not selected any portion", Toast.LENGTH_LONG).show();
                    Log.d("Error", error.getMessage());
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
    protected void onResume() {
        super.onResume();
        if(!AudioWaveformView.isWaveDrawn){
            new MyThread().start();
        }
        if(wasPlaying)
        mediaPlayer.start();
        Log.d("waveform width", String.valueOf(width));
    }
    protected void onPause() {
        super.onPause();
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            wasPlaying=true;
        }
        else
        {
            wasPlaying=false;
        }
        AudioWaveformView.isWaveDrawn=false;
    }
    @Override
    public void markerTouchStart(MarkerView marker, float pos) {
       /*float delta = pos - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }
        updateDisplay();*/
    }
    @Override
    public void markerTouchMove(MarkerView marker, float posX) {
        if (marker == mStartMarker) {
            if(mediaPlayer.isPlaying()) {
                sbChangeSeekPosition.setClickable(false);
                sbChangeSeekPosition.setAlpha(1f);
            }
            lastUpdatedMarker=mStartMarker;
            mStartPos=(int)posX;
            int middleEndMarker=mEndMarker.getLeft();
            if(mStartPos>=middleEndMarker)
                mStartPos = middleEndMarker;
            mStartPos = trap(mStartPos);
            updateDisplay();
            int percentStart=mStartPos*100/648;
            min= (int) (percentStart*duration/100);
            if(trapped)
            {
                min=0;
                trapped=false;
            }
            changeMinText();
            }
        else {
            lastUpdatedMarker=mEndMarker;
            mStartPos=(int)posX;
            int middleStartMarker=mStartMarker.getLeft();
            if(mStartPos<=middleStartMarker)
                mStartPos = middleStartMarker;
            mStartPos = trap(mStartPos);
            updateDisplay();
            int currentEndPos=mEndMarker.getLeft();
            int percentEnd=currentEndPos*100/648;
             max= (int) (percentEnd*duration/100);
            changeMaxText();
        }
    }
    private int trap(int pos) {
        if(pos<=8)
        {
            trapped=true;
            return 8;
        }
        width=audioWaveformView.getWidth()+8;
        if (pos > width) {
            trapped=true;
            return width;
        }
        return pos;
    }
    private synchronized void updateDisplay() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        if(lastUpdatedMarker==mStartMarker) {
            mEndPos=mStartMarker.getTop();
            params.setMargins(mStartPos, mEndPos, 0, 0);
            mStartMarker.setLayoutParams(params);
            RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams) leftView.getLayoutParams();
            int lagging2=(mStartMarker.getRight()-mStartMarker.getLeft())/2;
            int leftMargin2=mStartMarker.getLeft()+lagging2;
            if(leftMargin2>700)
                leftX=trap(leftMargin2);
            else
                leftX=leftMargin2;
            param2.width=mStartPos;
            param2.height= audioWaveformView.getHeight();
        }
        if(lastUpdatedMarker==mEndMarker)
        {
            mEndPos=mEndMarker.getTop();
            params.setMargins(mStartPos,mEndPos,0,0);
            mEndMarker.setLayoutParams(params);
            RelativeLayout.LayoutParams param2=(RelativeLayout.LayoutParams) rightView.getLayoutParams();
            int lagging2=mEndMarker.getWidth()/2;
            int rightMargin2=648-mStartPos;
                rightY=trapView(rightMargin2);
            param2.width=rightY;
            param2.height=audioWaveformView.getHeight();
            rightView.setLayoutParams(param2);
        }
    }

    private int trapView(int width) {
        if(width>640)
            return 640;
        else
            return width;
    }

    @Override
    public void markerTouchEnd(MarkerView marker) {
       int percent=mStartMarker.getLeft();
        int minPercent=percent*100/648;
        int currentProgress=sbChangeSeekPosition.getProgress();
        int progressPercent= (int) (currentProgress*100/duration);
        if(progressPercent<minPercent) {
            percent = mStartMarker.getLeft();
            minPercent = percent * 100 / 648;
            currentProgress = (int) (minPercent * duration / 100);
            sbChangeSeekPosition.setProgress(currentProgress);
            mediaPlayer.seekTo(sbChangeSeekPosition.getProgress());
        }
             percent=mEndMarker.getLeft();
             minPercent=percent*100/648;
             currentProgress=sbChangeSeekPosition.getProgress();
             progressPercent= (int) (currentProgress*100/duration);
            if(progressPercent>=minPercent) {
                percent = mStartMarker.getLeft();
                minPercent = percent * 100 / 648;
                    currentProgress = (int) (minPercent * duration / 100);
                sbChangeSeekPosition.setProgress(currentProgress);
                mediaPlayer.seekTo(sbChangeSeekPosition.getProgress());
            }
        checkClickable();
    }
    @Override
    public void markerFocus(MarkerView marker) {

    }

    @Override
    public void markerLeft(MarkerView marker, int velocity) {
       /* if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
        }

        if (marker == mEndMarker) {
            RelativeLayout.LayoutParams param=(RelativeLayout.LayoutParams) rightLine.getLayoutParams();
            rightY=param.rightMargin;
            rightY+=velocity;
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }
        }*/
    }

    @Override
    public void markerRight(MarkerView marker, int velocity) {
      /*  if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > width)
                mStartPos = width;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > width)
                mEndPos = width;
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > width)
                mEndPos = width;
        }*/
    }

    @Override
    public void markerEnter(MarkerView marker) {

    }
    private void star(){
        upDateSeekBar();
    }
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            upDateSeekBar();
        }
    };
    private void upDateSeekBar() {
        checkClickable();
         int percent=mEndMarker.getLeft();
         int minPercent=percent*100/648;
         int currentProgress=sbChangeSeekPosition.getProgress();
        int progressPercent= (int) (mediaPlayer.getCurrentPosition()*100/duration);
      if(progressPercent>=minPercent) {
          percent = mStartMarker.getLeft();
          minPercent = percent * 100 / 648;
          currentProgress = (int) (minPercent * duration / 100);
          mediaPlayer.seekTo(currentProgress);
          sbChangeSeekPosition.setProgress(currentProgress);
          mediaPlayer.pause();
          playImage.setImageResource(R.drawable.play);
          currentSongPosition = sbChangeSeekPosition.getProgress();
      }
        else {
          sbChangeSeekPosition.setProgress(mediaPlayer.getCurrentPosition());
            currentSongPosition=sbChangeSeekPosition.getProgress();
        }
        mHandler.postDelayed(runnable, 0);
    }
    @Override
    public void markerKeyUp() {
        checkClickable();
    }

    @Override
    public void markerDraw() {

    }
    public class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
            BufferedInputStream bufferedInputStream= null;
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(selectedFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int[] bytes=new int[(int)selectedFile.length()];
            for(int i=0;i<bytes.length;i++){
                try {
                    bytes[i]=bufferedInputStream.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while(!AudioWaveformView.isWaveDrawn)
                audioWaveformView.drawWaveform(bytes);
        }
    }
    @Override
    public void onDestroy() {
        if (runnable != null&&mHandler!=null)
            mHandler.removeCallbacks(runnable );
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();
            mediaPlayer.release();
        super.onDestroy();
    }
    /*private synchronized void updateDisplay()
    {
        final Runnable mTimerRunnable = new Runnable() {
            public void run() {
                // Updating an EditText is slow on Android.  Make sure
                // we only do the update if the text has actually changed.
                if (mStartPos != mLastDisplayedStartPos) {
                    mLastDisplayedStartPos = mStartPos;
                }

                if (mEndPos != mLastDisplayedEndPos){
                    mLastDisplayedEndPos = mEndPos;
                }

                mHandler.postDelayed(mTimerRunnable, 100);
            }
        };
    }*/
}
