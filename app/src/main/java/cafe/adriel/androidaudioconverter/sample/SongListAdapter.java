package cafe.adriel.androidaudioconverter.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements Filterable {
    private Activity context;
    private ArrayList<ItemsModel> itemsModelListFiltered;
    private ArrayList<ItemsModel> itemsModelList;
    RequestOptions requestOptions;
    // RecyclerView recyclerView;
    public SongListAdapter(Activity context, List<ItemsModel> itemsModelList) {
        super();
        this.context = context;
        this.itemsModelListFiltered = (ArrayList<ItemsModel>) itemsModelList;
        this.itemsModelList=(ArrayList<ItemsModel>) itemsModelList;
        requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.aw_ic_default_album);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.listview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        final ItemsModel myListData = itemsModelListFiltered.get(position);
        holder.name.setText(myListData.getName());
        holder.duration.setText(myListData.getDurationInUIFormat());
        String size_to_assign = Util.formatFileSize(myListData.getSize(),1);
        holder.size.setText(" | "+size_to_assign);
        Glide.with(context)
                .load(myListData.getAlbumArtUri())
                .apply(requestOptions)
                .into(holder.imageView);
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("main activity", "item clicked");
                Intent intent = new Intent(context,ConvertFile.class);
                intent.putExtra("item",itemsModelListFiltered.get(position).getUri());
                intent.putExtra("duration", itemsModelListFiltered.get(position).getDuration());
                context.startActivity(intent);
            }
        });
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ItemsModel> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                   itemsModelListFiltered=itemsModelList;
                } else {
                    String filterPattern = constraint.toString().toLowerCase().replaceAll(" ","");
                    for (ItemsModel item : itemsModelList) {
                        if (item.getName().toLowerCase().replaceAll(" ","").contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                    itemsModelListFiltered= (ArrayList<ItemsModel>) filteredList;
                }
                FilterResults results = new FilterResults();
                results.values = itemsModelListFiltered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemsModelListFiltered=(ArrayList<ItemsModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    @Override
    public int getItemCount() {
        return itemsModelListFiltered.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView name,duration,size;
        public View linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView =  itemView.findViewById(R.id.imageView);
            this.name =  itemView.findViewById(R.id.name);
            this.size=itemView.findViewById(R.id.tv_size);
            this.duration=itemView.findViewById(R.id.tv_duration);
            this.linearLayout = itemView.findViewById(R.id.list_item);
        }
    }
    public void updateDataAndNotify(List<ItemsModel> itemsModelList) {
        this.itemsModelListFiltered = (ArrayList<ItemsModel>) itemsModelList;
        notifyDataSetChanged();
    }
}