package cafe.adriel.androidaudioconverter.sample;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class SelectSectionActivity extends AppCompatActivity {

	private Toolbar toolbar;
	private TextView textViewStart, textViewEnd,textViewTotalDuration,leftText,differencetext,rightText;
	private Button buttonSave;
	private ImageView imageViewPlayPause;
	private MediaPlayer mediaPlayer;
	private AudioWaveformView audioWaveformView;
	private File file;
	private Uri uri;
	private MarkerView endmarker,startmarker;
    private boolean mStartVisible;
    private boolean mEndVisible;
    long duration;
	private RangeSeekBar rangeSeekbar;
	private static long startFromSec,endAtSec,prevStartSec=-1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		Intent intent=getIntent();
		final String songName=intent.getStringExtra(getResources().getString(R.string.music_file_name));
		uri=intent.getParcelableExtra(getResources().getString(R.string.selected_song_uri));
		final long duration=intent.getLongExtra(getResources().getString(R.string.duration),0);

        leftText = (TextView) findViewById(R.id.leftText);
        rightText = (TextView) findViewById(R.id.rightText);
        differencetext = (TextView) findViewById(R.id.difference);
        imageViewPlayPause=findViewById(R.id.play);
        buttonSave=findViewById(R.id.Save);
        endmarker=findViewById(R.id.endmarker);
        startmarker=findViewById(R.id.startmarker);
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
		final Thread thread=null;
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
					thread.interrupt();
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
					Intent intent1=new Intent(SelectSectionActivity.this,PlayMusicActivity.class);
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



       /* mStartMarker = (MarkerView) findViewById(R.id.startmarker);
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
        mEndVisible = true;*/

        //  updateDisplay();

        
	}

	@Override
	protected void onStart() {
		super.onStart();
		mediaPlayer=new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				imageViewPlayPause.setImageResource(R.drawable.icon_play);
				mediaPlayer.seekTo((int)startFromSec);
			}
		});
		try {
			mediaPlayer.setDataSource(SelectSectionActivity.this,uri);
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
