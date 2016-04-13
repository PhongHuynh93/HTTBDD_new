package dhbk.android.testgooglesearchreturn.ClassHelp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by huynhducthanhphong on 4/2/16.
 * load image which has downloaded from Google in Viewpager
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    private final int mSize;

    public ImagePagerAdapter(FragmentManager fm, int size) {
        super(fm);
        mSize = size;
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageDetailFragment.newInstance(position);
    }
}
