package cafe.adriel.androidaudioconverter.sample.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import cafe.adriel.androidaudioconverter.sample.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class VideoToAudio extends Fragment {
    public VideoToAudio()
    {

    }
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
       return inflater.inflate(R.layout.video_to_audio_output, container, false);
    }
}