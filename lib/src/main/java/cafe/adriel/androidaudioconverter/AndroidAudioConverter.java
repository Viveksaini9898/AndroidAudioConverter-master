package cafe.adriel.androidaudioconverter;

import android.content.Context;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class AndroidAudioConverter {

    private static boolean loaded;
    String durationLeft,durationRight;
    private Context context;
    private File audioFile,outputFile;
    private AudioFormat format;
    private IConvertCallback callback;

    private AndroidAudioConverter(Context context){
        this.context = context;
    }
    public AndroidAudioConverter setDuration(String durationLeft,String durationRight){
        this.durationLeft = durationLeft;
        this.durationRight=durationRight;
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

    public static AndroidAudioConverter with(Context context) {
        return new AndroidAudioConverter(context);
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
        final File convertedFile = getConvertedFile(outputFile, format);
        convertedFile.getPath();
        final String[] cmd = new String[]{"-i" ,audioFile.getPath(), "-ss",durationLeft, "-to" ,durationRight ,"-c" ,"copy",convertedFile.getPath()};
        try {
            FFmpeg.getInstance(context).execute(cmd, new FFmpegExecuteResponseHandler() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onProgress(String message) {

                        }

                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess(convertedFile);
                        }

                        @Override
                        public void onFailure(String message) {
                            callback.onFailure(new IOException(message));
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
        } catch (Exception e){
            callback.onFailure(e);
        }
    }

    private static File getConvertedFile(File originalFile, AudioFormat format){
        String[] f = originalFile.getPath().split("\\.");
        String filePath = originalFile.getPath().replace(f[f.length - 1], format.getFormat());
        return new File(filePath);
    }
}