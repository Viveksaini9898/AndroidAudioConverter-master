package cafe.adriel.androidaudioconverter.sample;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cafe.adriel.androidaudioconverter.sample.ui.main.SectionsPagerAdapter;

public class OutputFoldersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_folders);
        TabLayout tabs = findViewById(R.id.tabs);
// Set the text for each tab.
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_1));
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_2));
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_3));
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_4));
        tabs.addTab(tabs.newTab().setText(R.string.tab_text_5));
// Set the tabs to fill the entire layout.
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
// Use PagerAdapter to manage page views in fragments.
        final ViewPager viewPager = findViewById(R.id.view_pager);
        final SectionsPagerAdapter adapter = new SectionsPagerAdapter(this,getSupportFragmentManager(), tabs.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new
                                                   TabLayout.OnTabSelectedListener() {
                                                       @Override
                                                       public void onTabSelected(TabLayout.Tab tab) {
                                                           viewPager.setCurrentItem(tab.getPosition());
                                                       }

                                                       @Override
                                                       public void onTabUnselected(TabLayout.Tab tab) {
                                                       }

                                                       @Override
                                                       public void onTabReselected(TabLayout.Tab tab) {
                                                       }
                                                   });
    }
}