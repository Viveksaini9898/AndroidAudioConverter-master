package cafe.adriel.androidaudioconverter.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SelectorAdapter extends RecyclerView.Adapter<SelectorAdapter.ViewHolder> {
    private Activity context;
    List<Integer> imageList;
    List<String> textList;
    Boolean added;
    public SelectorAdapter(Activity context,Boolean added) {
        super();
        this.context = context;
        this.added=added;
        imageList=new ArrayList<>();
        imageList.add(R.drawable.video_cutter);
        imageList.add(R.drawable.video_to_audio);
        imageList.add(R.drawable.audio_cutter);
        imageList.add(null);
        imageList.add(R.drawable.outputfolder);
        imageList.add(null);
        textList=new ArrayList<>();
        textList.add("Video to Audio");
        textList.add("Video Cutter");
        textList.add("Audio Cutter");
        textList.add("Audio Merger");
        textList.add("Output Folders");
        textList.add("Ringtone Cutter");
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.layout_for_selection, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        holder.textView.setText(textList.get(position));
        if (position<3||position==4)
        holder.imageView.setImageResource(imageList.get(position));
        if(position==2) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("main activity", "item clicked");
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }
            });
        }
            if(position==3)
            {
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("main activity", "item clicked");
                        Intent intent = new Intent(context, AudioMergerActivity.class);
                        context.startActivity(intent);
                    }
                });
            }

            if(position==4) {
                if(added)
                    holder.addedNew.setVisibility(View.VISIBLE);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("main activity", "item clicked");
                        Intent intent = new Intent(context, OutputFoldersActivity.class);
                        context.startActivity(intent);
                    }
                });
        }
    }
    @Override
    public int getItemCount() {
        return textList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView,addedNew;
        public RelativeLayout view;
        public ViewHolder(View itemView) {
            super(itemView);
            addedNew =  itemView.findViewById(R.id.addedNew);
            this.imageView =  itemView.findViewById(R.id.image_to_select);
            this.textView =  itemView.findViewById(R.id.text_to_select);
            this.view = itemView.findViewById(R.id.item_view);
        }
    }
}
