package cafe.adriel.androidaudioconverter.sample.asynctask;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cafe.adriel.androidaudioconverter.sample.ItemsModel;
import cafe.adriel.androidaudioconverter.sample.listener.DataFetcherListener;

public class DataFetcherAsyncTask extends AsyncTask<Void, Void, List<ItemsModel>> {
    private Context mContext;
    private List<ItemsModel> itemList;
    private DataFetcherListener mDataFetcherListener;
    private String selection;
    private String[] selectionArgs;
    public DataFetcherAsyncTask(Context context, DataFetcherListener dataFetcherListener,String selection,String[] selectionArgs){
        mContext = context;
        mDataFetcherListener = dataFetcherListener;
        this.selection=selection;
        this.selectionArgs=selectionArgs;
    }

    String[] projection = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
    };

   // String selection = MediaStore.Audio.Media.DURATION + "";
    /*  String[] selectionArgs = new String[]{
              String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))};
      String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
  */


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<ItemsModel> doInBackground(Void... voids) {

        try (
                Cursor cursor = mContext.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                )) {
            // Cache column indices.
            itemList = new ArrayList<>();
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int albumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
                Uri imageUri;
                imageUri=ContentUris.withAppendedId(albumArtUri, cursor.getLong(albumId));
               /* audioList.add(contentUri);
                names.add(name);
                song_duration.add(String.valueOf(duration));*/
                ItemsModel itemsModel=new ItemsModel(name,imageUri,contentUri,String.valueOf(duration),size);
                itemList.add(itemsModel);
                Log.d("size", String.valueOf(itemList.size()));

            }
        }
        return itemList;
    }

    @Override
    protected void onPostExecute(List<ItemsModel> itemsModels) {
        super.onPostExecute(itemsModels);
        if (mDataFetcherListener!=null){
            if (itemsModels!=null) {
                mDataFetcherListener.onDataFetched(itemsModels);
            }else {
                mDataFetcherListener.onDataError();
            }
        }
    }
}
