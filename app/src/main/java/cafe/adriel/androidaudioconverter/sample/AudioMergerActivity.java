package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import cafe.adriel.androidaudioconverter.sample.asynctask.DataFetcherAsyncTask;
import cafe.adriel.androidaudioconverter.sample.listener.DataFetcherListener;

public class AudioMergerActivity extends AppCompatActivity implements DataFetcherListener {
    RecyclerView recyclerView;
    private AudioMergerAdapter songListAdapter;
    private Button next;
    private TextView numOfFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_merger);
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(AudioMergerActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED||
                ContextCompat.checkSelfPermission(AudioMergerActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            AudioMergerActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                           Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            1);
        }
        else
        {
            new DataFetcherAsyncTask(getApplicationContext(), this,null,null).execute();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i]) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    new DataFetcherAsyncTask(getApplicationContext(), this,null,null).execute();
                    return;
                }
            }
            Toast.makeText(this,"permission not granted",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.menu_source,menu);
        MenuItem item=menu.getItem(0);
        final SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                    songListAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                songListAdapter.getFilter().filter(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
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
    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        String permission=Manifest.permission.READ_EXTERNAL_STORAGE;
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                AudioMergerActivity.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            AudioMergerActivity.this,
                            new String[] { permission },
                            1);
        }
        else {
        }
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        }
        catch (FileNotFoundException e){
            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.aw_ic_default_album);
            return icon;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onDataFetched(List<ItemsModel> itemsModelList) {
        if (songListAdapter == null){
            recyclerView=findViewById(R.id.list_of_audios);
            next=findViewById(R.id.btn_next);
            numOfFiles=findViewById(R.id.tv_files);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            songListAdapter=new AudioMergerAdapter(this,itemsModelList,next,numOfFiles);
            recyclerView.setAdapter(songListAdapter);
        }else{
            songListAdapter.updateDataAndNotify(itemsModelList);
        }


    }

    @Override
    public void onDataError() {

    }

    class Audio {
        private final Uri uri;
        private final String name;
        private final int duration;
        private final int size;

        public Audio(Uri uri, String name, int duration, int size) {
            this.uri = uri;
            this.name = name;
            this.duration = duration;
            this.size = size;
        }
    }

    private class LoadThread extends Thread{
        @Override
        public void run() {
            super.run();
        }
    }
}