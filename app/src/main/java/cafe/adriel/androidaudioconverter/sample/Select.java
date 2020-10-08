package cafe.adriel.androidaudioconverter.sample;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Select extends RecyclerView.Adapter<Select.MyViewHolder> {
    List nameList;
    private OnClick onClick;
    public Select(List nameList,OnClick onClick) {
        this.nameList=nameList;
        this.onClick=onClick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.select, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int i) {
        selectList data= (selectList) nameList.get(i);
        holder.cardImage.setImageResource(data.cardImage);
        holder.tvName.setText(data.tvName);
        holder.cardView.setBackgroundResource(data.background);

    }



    @Override
    public int getItemCount() {
        return nameList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        LinearLayout linearLayout;
        ImageView cardImage;
        CardView cardView;
        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.parent);
            cardImage=itemView.findViewById(R.id.cardImage);
            tvName=itemView.findViewById(R.id.tvName);
            cardView=itemView.findViewById(R.id.cardview);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getAdapterPosition()==0)
                   onClick.onClick(AdjustType.AUDIOCUTTER);
                    else if (getAdapterPosition()==1)
                        onClick.onClick(AdjustType.AUDIOMERGE);
                    else if (getAdapterPosition()==2)
                        onClick.onClick(AdjustType.RINGTONECUTTER);
                    else if (getAdapterPosition()==3)
                        onClick.onClick(AdjustType.OUTPUTFOLDER);
                }
            });
        }
    }

    interface OnClick{
        void onClick(AdjustType adjustType);
    }
}
