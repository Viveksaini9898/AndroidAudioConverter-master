package cafe.adriel.androidaudioconverter.sample;

import android.app.Application;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.BuildConfig;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import es.dmoral.toasty.Toasty;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
                if (BuildConfig.DEBUG) {
                    Toasty.error(getApplicationContext(), " AndroidAudioConverter Failed").show();
                    error.toString();
                }
            }
        });
    }
}