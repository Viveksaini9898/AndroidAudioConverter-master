package cafe.adriel.androidaudioconverter.sample.ui.main;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Context context;
    public SectionsPagerAdapter(Context context, FragmentManager fm,int mNumOfTabs) {
        super(fm);
        this.mNumOfTabs=mNumOfTabs;
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new VideoToAudio();
            case 1: return new VideoCutter();
            case 2: return new AudioCutterOutput(context);
            case 3: return new AudioMergerOutput();
            case 4: return new RingtoneCutter();
            default: return null;
        }
    }
    @Override
    public int getCount() {
        // Show 2 total pages.
        return mNumOfTabs;
    }
}