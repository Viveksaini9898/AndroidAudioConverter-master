package cafe.adriel.androidaudioconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class AndroidAudioConverter {

    private static boolean loaded;
    String durationLeft,durationRight;
    private Context context;
    public static File audioFile,outputFile,file;
    private AudioFormat format;
    private IConvertCallback callback;
    private String bitrate;
    FFmpeg fFmpeg;
    String volume;
    private int difference;
    ProgressBar progressBar;
    TextView progressPercent;
    private AndroidAudioConverter(Context context,ProgressBar progressBar,TextView progressPercent){
        this.context = context;
        this.progressPercent=progressPercent;
        this.progressBar=progressBar;
    }
    public AndroidAudioConverter setDuration(String durationLeft,String durationRight){
        this.durationLeft = durationLeft;
        this.durationRight=durationRight;
        return this;
    }
    public AndroidAudioConverter setVolume(String volume){
        this.volume=volume;
        return this;
    }
    public AndroidAudioConverter setBitrate(String bitrate){
        this.bitrate=bitrate;
        return this;
    }
    public static boolean isLoaded(){
        return loaded;
    }

    public static void load(Context context, final ILoadCallback callback){
        try {
            FFmpeg.getInstance(context).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess() {
                            loaded = true;
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure() {
                            loaded = false;
                            callback.onFailure(new Exception("Failed to loaded FFmpeg lib"));
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
        } catch (Exception e){
            loaded = false;
            callback.onFailure(e);
        }
    }

    public static AndroidAudioConverter with(Context context, ProgressBar progressBar, TextView progressPercent) {
        return new AndroidAudioConverter(context,progressBar,progressPercent);
    }

    public AndroidAudioConverter setInputFile(File originalFile) {
        this.audioFile = originalFile;
        return this;
    }
    public AndroidAudioConverter setOutputFile(File convertedFile)
    {
        this.outputFile=convertedFile;
       return  this;
    }
    public AndroidAudioConverter setFormat(AudioFormat format) {
        this.format = format;
        return this;
    }

    public AndroidAudioConverter setCallback(IConvertCallback callback) {
        this.callback = callback;
        return this;
    }

    public void convert() {
        if(!isLoaded()){
            callback.onFailure(new Exception("FFmpeg not loaded"));
            return;
        }
        if(audioFile == null || !audioFile.exists()){
            callback.onFailure(new IOException("File not exists"));
            return;
        }
        if(!audioFile.canRead()){
            callback.onFailure(new IOException("Can't read the file. Missing permission?"));
            return;
        }
         file=new File(Environment.getExternalStorageDirectory(),"/deletedfiles"+System.currentTimeMillis()+".mp3");
          file=getConvertedFile(file,format);
         outputFile=getConvertedFile(outputFile,format);
        FFmpeg.getInstance(context).killRunningProcesses();
        final String[] cmd_to_trim = new String[]{"-i" ,audioFile.getPath() ,"-ss",durationLeft, "-to" ,durationRight,"-c" ,"copy","-f","wav",file.getPath()};
        try {
             fFmpeg=FFmpeg.getInstance(context);
            fFmpeg.execute(cmd_to_trim, new FFmpegExecuteResponseHandler() {
                        @Override
                        public void onStart() {
                            Toast.makeText(context,"started first",Toast.LENGTH_LONG).show();
                            Log.d("startedfirst","started first");
                        }
                        @Override
                        public void onProgress(String message) {
                            Log.d("progressfirst",message);
                        }
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(context,String.valueOf(file.length()),Toast.LENGTH_LONG).show();
                            Log.d("succeededfirst","finished");
                            setRate();
                           //callback.onSuccess(outputFile);
                        }
                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(context, "Converting failed", Toast.LENGTH_SHORT).show();
                            Log.d("failedfirst",message);
                            if(file.exists())
                            file.delete();
                            callback.onFailure(new IOException(message));
                        }
                        @Override
                        public void onFinish() {

                        }
                    });
        }
        catch(FFmpegCommandAlreadyRunningException e)
        {
            killAllreadyRunningProcess(fFmpeg);
        }
        catch (Exception e){
            if(file!=null)
            file.delete();
            Log.d("occuredproblem",e.toString());
            callback.onFailure(e);
        }
    }
    private void setRate() {
        try {
            FFmpeg.getInstance(context).killRunningProcesses();
            killAllreadyRunningProcess(fFmpeg);
            fFmpeg = FFmpeg.getInstance(context);
            final String[] cmd_for_bitrate;
            if(bitrate.equals("0"))
                cmd_for_bitrate = new String[]{"-i",file.getPath() ,"-af","volume="+volume,outputFile.getPath()};
                else
               cmd_for_bitrate = new String[]{"-i",file.getPath() , "-ab", bitrate,"-filter:a","volume="+volume,outputFile.getPath()};
            Toast.makeText(context,"previous succeded",Toast.LENGTH_SHORT).show();
               FFmpeg.getInstance(context).execute(cmd_for_bitrate, new FFmpegExecuteResponseHandler() {
                @Override
                public void onStart() {
                    progressBar.setProgress(0);
                    progressPercent.setText("0%");
                    Toast.makeText(context,"started compressing",Toast.LENGTH_LONG).show();
                }
                @Override
                public void onProgress(String message) {
                    int start = message.indexOf("time=");
                    int end = message.indexOf(" bitrate");
                    if (start != -1 && end != -1) {
                        String duration = message.substring(start + 5, end);
                        if (duration != "") {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                progressBar.setProgress((int)sdf.parse("1970-01-01 " + duration).getTime());
                                int progress=(int)progressBar.getProgress()*100/difference;
                                progressPercent.setText(progress+"%");
                            }catch (ParseException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                @Override
                public void onSuccess(String message) {
                    Log.d("successmessage",message);
                    file.delete();
                    callback.onSuccess(outputFile);
                }
                @Override
                public void onFailure(String message) {
                    file.delete();
                    Log.d("failedsecond",message);
                    callback.onFailure(new IOException(message));
                }
                @Override
                public void onFinish() {

                }
            });
        }
        catch(FFmpegCommandAlreadyRunningException e)
        {
            killAllreadyRunningProcess(fFmpeg);
        }
        catch (Exception e){
            callback.onFailure(e);
        }
    }
    private static File getConvertedFile(File originalFile, AudioFormat format){
        String[] f = originalFile.getAbsolutePath().split("\\.");
        String filePath =originalFile.getAbsolutePath().replace(f[f.length - 1],format.getFormat());
        return new File(filePath);
    }
    private void killAllreadyRunningProcess(FFmpeg fFmpeg){
            fFmpeg.killRunningProcesses();
    }
    public AndroidAudioConverter setdurations(long duration) {
        this.difference =(int) duration;
        return this;
    }
}