package cafe.adriel.androidaudioconverter.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;

import es.dmoral.toasty.Toasty;

public class SelectorActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    SelectorAdapter selectorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        recyclerView=findViewById(R.id.selectAction);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        selectorAdapter=new SelectorAdapter(this);
        recyclerView.setAdapter(selectorAdapter);
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toasty.normal(this, "Press again to exit").show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}