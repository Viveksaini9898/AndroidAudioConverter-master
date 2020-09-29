package cafe.adriel.androidaudioconverter.sample;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongList extends BaseAdapter implements Filterable
{
    private AppCompatActivity context;
    ArrayList<ItemsModel> itemsModelList;
    ArrayList<ImageView> playImage;
    private ArrayList<ItemsModel> itemsModelListFiltered;
    MediaPlayer mediaPlayer;
    int lastPosition=-1;
    private boolean sameposition=false;
    public  SongList(AppCompatActivity context,ArrayList<ItemsModel> itemModel) {
        super();
        this.context = context;
        this.itemsModelList = itemModel;
        this.itemsModelListFiltered = itemModel;
    }

    @Override
    public int getCount() {
        if(itemsModelListFiltered==null)
            return 0;
        return itemsModelListFiltered.size();
    }
    @Override
    public Object getItem(int position) {
        return itemsModelListFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null)
            row = inflater.inflate(R.layout.listview_item, null);
        TextView name =  row.findViewById(R.id.textView);
        ImageView song_image =  row.findViewById(R.id.imageView);
        name.setText(itemsModelListFiltered.get(position).getName());
        song_image.setImageBitmap(itemsModelListFiltered.get(position).getImages());
        TextView duration=row.findViewById(R.id.tv_duration);
        String durationMinSec=TimeConversionInMinsec(Integer.parseInt(itemsModelListFiltered.get(position).getDuration()));
        duration.setText(durationMinSec+" ");
        String sizeText=format(itemsModelListFiltered.get(position).getSize(),1);
        TextView size=row.findViewById(R.id.tv_size);
        size.setText("| "+sizeText);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer!=null&&mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=null;
                }
                Log.e("main activity", "item clicked");
                Intent intent = new Intent(context, ConvertFile.class);
                intent.putExtra("item",itemsModelListFiltered.get(position).getUri());
                intent.putExtra("duration", itemsModelListFiltered.get(position).getDuration());
                context.startActivity(intent);
            }
        });
        return row;
    }

    private String getRealPathFromUri(Uri uri) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    filterResults.count = itemsModelList.size();
                    filterResults.values = itemsModelList;
                }else{
                    List<ItemsModel> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase().replaceAll(" ","");

                    for(ItemsModel itemsModel:itemsModelList){
                        if(itemsModel.getName().toLowerCase().replaceAll(" ","").contains(searchStr))
                        {
                            resultsModel.add(itemsModel);
                            filterResults.count = resultsModel.size();
                            filterResults.values = resultsModel;
                        }
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results!=null)
                itemsModelListFiltered = (ArrayList<ItemsModel>)results.values;
                else
                    itemsModelListFiltered=new ArrayList<>();
                notifyDataSetChanged();
            }
        };
        return filter;
    }
    private String TimeConversionInMinsec(int millisec){
     int x =(int) Math.ceil(millisec / 1000f);
    int min = x % 3600 / 60;
    int sec = x % 60;
    return String.format("%02d:%02d", min, sec);
}
    public static String format(double bytes, int digits) {
        String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }
}
