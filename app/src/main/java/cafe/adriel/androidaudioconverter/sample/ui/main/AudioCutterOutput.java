package cafe.adriel.androidaudioconverter.sample.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cafe.adriel.androidaudioconverter.sample.ItemsModel;
import cafe.adriel.androidaudioconverter.sample.MainActivity;
import cafe.adriel.androidaudioconverter.sample.R;
import cafe.adriel.androidaudioconverter.sample.SelectorActivity;
import cafe.adriel.androidaudioconverter.sample.SongListAdapter;
import cafe.adriel.androidaudioconverter.sample.asynctask.DataFetcherAsyncTask;
import cafe.adriel.androidaudioconverter.sample.listener.DataFetcherListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class AudioCutterOutput extends Fragment  implements DataFetcherListener {
    RecyclerView recyclerView;
    String selection;
    String[] selectionArgs;
    View view;
    Context context;
    private AudioCutterOutputAdapter songListAdapter;
    public AudioCutterOutput(Context context)
    {
        selection= MediaStore.Video.Media.DATA +" like?";
        selectionArgs=new String[]{"%Android Audio Converter/Audio cutter%"};
        this.context=context;
    }
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.audio_cutter_output, container, false);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED||
                ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            getActivity(),
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            1);
        }
        else
        {
            new DataFetcherAsyncTask(context, this,selection,selectionArgs).execute();
        }
        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i]) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    new DataFetcherAsyncTask(context, this,selection,selectionArgs).execute();
                    return;
                }
            }
            Toast.makeText(context,"permission not granted",Toast.LENGTH_LONG).show();
            Activity thisActivity = getActivity();
            if (thisActivity != null) {
                 Intent intent=new Intent(thisActivity,SelectorActivity.class);
                 startActivity(intent);
            }
        }
    }
    @Override
    public void onDataFetched(List<ItemsModel> itemsModelList) {
        if (songListAdapter == null && view!=null){
            recyclerView=view.findViewById(R.id.trimmed_audios);
            recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));
            songListAdapter=new AudioCutterOutputAdapter(getActivity(),itemsModelList);
            recyclerView.setAdapter(songListAdapter);
        }else if(view!=null){
            songListAdapter.updateDataAndNotify(itemsModelList);
        }
    }
    @Override
    public void onDataError() {

    }
}