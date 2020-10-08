package cafe.adriel.androidaudioconverter.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class BaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Select select;
    private List<selectList> nameList = new ArrayList<>();
    private LinearLayout audiocutter,audiomerge,ringtonecutter,outputfolder;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        //recyclerView=findViewById(R.id.recyclerview);
        /*select = new Select(nameList, new Select.OnClick() {
            @Override
            public void onClick(AdjustType adjustType) {
                if (adjustType==adjustType.AUDIOMERGE) {
                    Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toasty.success(getApplicationContext(), "" + adjustType.name()).show();
                }else if(adjustType==adjustType.AUDIOMERGE){
                    Toasty.success(getApplicationContext(), "" + adjustType.name()).show();
                }else if (adjustType==adjustType.RINGTONECUTTER) {
                    Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toasty.success(getApplicationContext(), "" + adjustType.name()).show();
                }else if (adjustType==adjustType.OUTPUTFOLDER) {
                    *//*Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();*//*
                    Toasty.success(getApplicationContext(), "" + adjustType.name()).show();
                }
            }

        });
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(select);
        selectItem();

    }

    private void selectItem() {

        nameList.add( new selectList(R.drawable.music_white_24dp, "Audio cutter",R.drawable.gradient_blue));
        nameList.add(new selectList(R.drawable.add_white_24dp, "Audio merge",R.drawable.gradient_blue));
        nameList.add(new selectList(R.drawable.music_white_24dp, "Ringtone cutter",R.drawable.gradient_pink_background));
        nameList.add(new selectList(R.drawable.add_white_24dp, "Output folder",R.drawable.gradient_pink_background));
    }*/

        audiocutter=findViewById(R.id.audiocutter);
        audiocutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BaseActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        audiomerge=findViewById(R.id.audiomerge);
        audiomerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"AudioMerge",Toast.LENGTH_SHORT).show();
            }
        });

        ringtonecutter=findViewById(R.id.ringtonecutter);
        ringtonecutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BaseActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        outputfolder=findViewById(R.id.outputfolder);
        outputfolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"OutputFolder",Toast.LENGTH_SHORT).show();
            }
        });


    }
}