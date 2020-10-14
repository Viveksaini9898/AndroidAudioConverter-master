package cafe.adriel.androidaudioconverter.sample.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cafe.adriel.androidaudioconverter.sample.ItemsModel;
import cafe.adriel.androidaudioconverter.sample.R;
import cafe.adriel.androidaudioconverter.sample.asynctask.DataFetcherAsyncTask;
import cafe.adriel.androidaudioconverter.sample.listener.DataFetcherListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class AudioMergerOutput extends Fragment implements DataFetcherListener {
    RecyclerView recyclerView;
    String selection;
    String[] selectionArgs;
    View view;
    public AudioMergerOutput()
    {

    }
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.audio_merger_output, container, false);
        //new DataFetcherAsyncTask(getContext(), this,selection,selectionArgs).execute();
        return view;
    }

    @Override
    public void onDataFetched(List<ItemsModel> itemsModelList) {

    }

    @Override
    public void onDataError() {

    }
}