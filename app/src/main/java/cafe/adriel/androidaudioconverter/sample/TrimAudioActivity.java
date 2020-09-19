package cafe.adriel.androidaudioconverter.sample;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import cafe.adriel.androidaudioconverter.sample.soundfile.SoundFile;

public class TrimAudioActivity extends AppCompatActivity implements MarkerView.MarkerListener {

	private Toolbar toolbar;
	private TextView textViewStart, textViewEnd,textViewTotalDuration,leftText,differencetext,rightText;
	private Button buttonSave;
	private ImageView imageViewPlayPause;
	private MediaPlayer mediaPlayer;
	private AudioWaveformView audioWaveformView;
	private File file;
	private Uri uri;
    private boolean mStartVisible;
    private boolean mEndVisible;
    long duration;
    private SoundFile mSoundFile;
    private RangeSeekBar rangeSeekbar;
	private static long startFromSec,endAtSec,prevStartSec=-1;

    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private long mRecordingLastUpdateTime;
    private boolean mRecordingKeepGoing;
    private double mRecordingTime;
    private boolean mFinishActivity;
    private TextView mTimerTextView;
    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private File mFile;
    private String mFilename;
    private String mArtist;
    private String mTitle;
    private int mNewFileKind;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;

    private TextView mStartText;
    private Toolbar  mToolbar;
    private TextView mEndText;
    private TextView mInfo;
    private String mInfoContent;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private boolean mKeyDown;
    private String mCaption = "";
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private Handler mHandler;
    private boolean mIsPlaying;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset = 0;
    private int mMarkerRightInset = 0;
    private int mMarkerTopOffset = 0;
    private Context mContext;
    private int mMarkerBottomOffset = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		Intent intent=getIntent();

		mediaPlayer=null;
        mContext = getApplicationContext();
		final String songName=intent.getStringExtra(getResources().getString(R.string.music_file_name));
		uri=intent.getParcelableExtra(getResources().getString(R.string.selected_song_uri));
		final long duration=intent.getLongExtra(getResources().getString(R.string.duration),0);

        leftText = (TextView) findViewById(R.id.tvLeftText);
        rightText = (TextView) findViewById(R.id.tvRightText);
        differencetext = (TextView) findViewById(R.id.tvDifference);
        imageViewPlayPause=findViewById(R.id.play);
        buttonSave=findViewById(R.id.Save);
        mEndMarker=findViewById(R.id.endmarker);
        mStartMarker=findViewById(R.id.startmarker);

        mPlayButton = (ImageButton) findViewById(R.id.play);
        mPlayButton.setOnClickListener(mPlayListener);
        mRewindButton = (ImageButton) findViewById(R.id.rew);
        mRewindButton.setOnClickListener(mRewindListener);
        mFfwdButton = (ImageButton) findViewById(R.id.ffwd);
        mFfwdButton.setOnClickListener(mFfwdListener);

       /* TextView markStartButton = (TextView) findViewById(R.id.tvLeftText);
        markStartButton.setOnClickListener(mMarkStartListener);

        TextView markEndButton = (TextView) findViewById(R.id.tvRightText);
        markEndButton.setOnClickListener(mMarkEndListener);*/
        enableDisableButtons();
        mHandler = new Handler();
        mHandler.postDelayed(mTimerRunnable, 100);
        //toolbar=(Toolbar)findViewById(R.id.toolbar_select_section);
		/*imageViewPlayPause=(ImageView)findViewById(R.id.imageViewPlayPauseSelectSection);
		textViewStart =(TextView) findViewById(R.id.textViewStart);
		textViewEnd =(TextView) findViewById(R.id.textViewEnd);
		textViewTotalDuration=(TextView)findViewById(R.id.textViewDurationSelectSection);
		buttonSave=(Button)findViewById(R.id.buttonSelectSection) ;*/
	//	rangeSeekbar=findViewById(R.id.rangeseekbar);
		audioWaveformView=(AudioWaveformView)findViewById(R.id.waveform);
		//setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(songName);
		rightText.setText(getDuration(duration));
		mediaPlayer=new MediaPlayer();
		file=new File(uri.toString());
		final long noOfBytes=file.length();
		/*rangeSeekbar.setSelectedMinValue(0);
		rangeSeekbar.setSelectedMinValue(duration);*/
		//final Thread thread=null;
		imageViewPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mediaPlayer.isPlaying()){
					mediaPlayer.seekTo((int)startFromSec);
					mediaPlayer.start();
					imageViewPlayPause.setImageResource(R.drawable.pause);
					Thread thread=new Thread(new Runnable() {
						@Override
						public void run() {
							while (mediaPlayer.getCurrentPosition()<endAtSec);
							mediaPlayer.pause();
							mediaPlayer.seekTo((int) duration-1);
							mediaPlayer.start();
						}
					});
					thread.start();
				}else{
					mediaPlayer.pause();
					//thread.interrupt();
					imageViewPlayPause.setImageResource(R.drawable.play);
				}
			}
		});
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.O)
			@Override
			public void onClick(View v) {
				long bytesToSkip= getByteNoFromSecNo(startFromSec,duration,noOfBytes);
				long lastByteNo=getByteNoFromSecNo(endAtSec,duration,noOfBytes);
				long bytesToRead=lastByteNo-bytesToSkip;
				try {
					String pathNewFile=createNewSong(songName);
					BufferedInputStream bufferedInputStream=new BufferedInputStream(new FileInputStream(file));
					bufferedInputStream.skip(bytesToSkip);
					File newFile=new File(pathNewFile);
					BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(new FileOutputStream(newFile));
					while (bytesToRead-->0){
						bufferedOutputStream.write(bufferedInputStream.read());
					}
					bufferedInputStream.close();
					bufferedOutputStream.close();
					Intent intent1=new Intent(TrimAudioActivity.this,PlayMusicActivity.class);
					intent1.putExtra(getResources().getString(R.string.music_file_name),Paths.get(pathNewFile).getFileName().toString());
					intent1.putExtra(getResources().getString(R.string.selected_song_uri),getNewFilesUri(songName));
					startActivity(intent1);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});



/*		rangeSeekbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                leftText.setText(getDuration((long)minValue));
                rightText.setText(getDuration((long)maxValue));
                startFromSec=(long)minValue;
                endAtSec=(long)maxValue;
                if(prevStartSec!=startFromSec){
                    mediaPlayer.seekTo((int)startFromSec);
                    prevStartSec=startFromSec;
                }
                else
                    mediaPlayer.seekTo((int)endAtSec-5000);

                mediaPlayer.start();
            }
		});*/


        audioWaveformView.setListener(this);

        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;


        if (mSoundFile != null && !audioWaveformView.hasSoundFile()) {
            audioWaveformView.setSoundFile(mSoundFile);
            audioWaveformView.recomputeHeights(mDensity);
            mMaxPos = audioWaveformView.maxPos();
        }
        mStartMarker = (MarkerView) findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView) findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;

        updateDisplay();

        
	}

    public void waveformDraw() {
        mWidth = audioWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = getCurrentTime();
    }

    public void waveformTouchMove(float x) {
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = audioWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                    mediaPlayer.seekTo(seekMsec);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    public void markerTouchStart(MarkerView marker, float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerEnter(MarkerView marker) {
    }

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    @Override
    public void markerDraw() {

    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mediaPlayer.getCurrentPosition();
            int frames = audioWaveformView.millisecsToPixels(now);
            audioWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        audioWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        audioWaveformView.invalidate();

        mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(
                startX - AudioSelectActivity.getDimensionInPixel(mContext, 42),
                mMarkerTopOffset,
                0,
                0);

        mStartMarker.setLayoutParams(params);
        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(endX + AudioSelectActivity.getDimensionInPixel(mContext, 42),
                audioWaveformView.getMeasuredHeight() - mEndMarker.getHeight(), 0,
                0);

        mEndMarker.setLayoutParams(params);
    }

    private String formatTime(int pixels) {
        if (audioWaveformView != null && audioWaveformView.isInitialized()) {
            return formatDecimal(audioWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }
    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }
    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    private synchronized void handlePause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        audioWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    private void enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(R.drawable.pause);
            mPlayButton.setContentDescription(getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(R.drawable.play);
            mPlayButton.setContentDescription(getResources().getText(R.string.play));
        }
    }

    private Runnable mTimerRunnable = new Runnable() {
        public void run() {
            // Updating an EditText is slow on Android.  Make sure
            // we only do the update if the text has actually changed.
            if (mStartPos != mLastDisplayedStartPos &&
                    !leftText.hasFocus()) {
                leftText.setText(formatTime(mStartPos));
                mLastDisplayedStartPos = mStartPos;
            }

            if (mEndPos != mLastDisplayedEndPos &&
                    !rightText.hasFocus()) {
                rightText.setText(formatTime(mEndPos));
                mLastDisplayedEndPos = mEndPos;
            }

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };

    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mediaPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
            mPlayStartMsec = audioWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = audioWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = audioWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = audioWaveformView.pixelsToMillisecs(mEndPos);
            }
           /* mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    handlePause();
                }
            });*/
            mIsPlaying = true;

            mediaPlayer.seekTo(mPlayStartMsec);
            mediaPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
          //  showFinalAlert(e, R.string.play_error);
            return;
        }
    }

    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        public void onClick(View sender) {
            onPlay(mStartPos);
            sender.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    };

    private View.OnClickListener mRewindListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = mediaPlayer.getCurrentPosition() - 5000;
                if (newPos < mPlayStartMsec)
                    newPos = mPlayStartMsec;
                mediaPlayer.seekTo(newPos);
            } else {
                mStartMarker.requestFocus();
                mStartMarker.setImageResource(R.drawable.start_dragger_selected);
                mEndMarker.setImageResource(R.drawable.end_dragger);
                markerFocus(mStartMarker);
            }
        }
    };

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = 5000 + mediaPlayer.getCurrentPosition();
                if (newPos > mPlayEndMsec)
                    newPos = mPlayEndMsec;
                mediaPlayer.seekTo(newPos);
            } else {
                mEndMarker.requestFocus();
                mEndMarker.setImageResource(R.drawable.end_dragger_selected);
                mStartMarker.setImageResource(R.drawable.start_dragger);
                markerFocus(mEndMarker);
            }
        }
    };

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }


   /* private View.OnClickListener mMarkStartListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mStartPos = audioWaveformView.millisecsToPixels(
                        mediaPlayer.getCurrentPosition());
                updateDisplay();
            }
        }
    };*/

    private View.OnClickListener mMarkEndListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mEndPos = audioWaveformView.millisecsToPixels(
                        mediaPlayer.getCurrentPosition());
                updateDisplay();
                handlePause();
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (mStartText.hasFocus()) {
                try {
                    mStartPos = audioWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mStartText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
            if (mEndText.hasFocus()) {
                try {
                    mEndPos = audioWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mEndText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
        }
    };






    private void loadGui() {
    }


    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }


    @Override
	protected void onStart() {
		super.onStart();
		mediaPlayer=new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				imageViewPlayPause.setImageResource(R.drawable.play);
				mediaPlayer.seekTo((int)startFromSec);
			}
		});
		try {
			mediaPlayer.setDataSource(TrimAudioActivity.this,uri);
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!AudioWaveformView.isWaveDrawn){
			new MyThread().start();
		}
	}

	Uri getNewFilesUri(String songName){
		Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, MediaStore.Audio.Media.DISPLAY_NAME+"=?",new String[]{songName},null);
		cursor.moveToFirst();
		Toast.makeText(this, cursor.getCount()+"", Toast.LENGTH_SHORT).show();
		return Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
	}
	long getByteNoFromSecNo(long startSec, long totalDuration, long noOfBytes){
		return startSec*noOfBytes/totalDuration;
	}

	String createNewSong(String songName) throws IOException {
		String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyMp3Cutter/MyMp3Cutter"+songName;
		File file =new File(path);
		file.createNewFile();
		return path;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();

		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
	}

	@Override
	protected void onPause() {
		super.onPause();
		AudioWaveformView.isWaveDrawn=false;
	}

	public class MyThread extends Thread{
		@Override
		public void run() {
			super.run();
			BufferedInputStream bufferedInputStream= null;
			try {
				bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			int[] bytes=new int[(int)file.length()];
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
	public String getDuration(long msec){
		if(msec==0)
			return "00:00";
		long sec=msec/1000;
		long min=sec/60;
		sec=sec%60;
		String minstr=min+"";
		String secstr=sec+"";
		if(min<10)
			minstr="0"+min;
		if(sec<10)
			secstr="0"+sec;
		return  minstr+":"+secstr;
	}
}
