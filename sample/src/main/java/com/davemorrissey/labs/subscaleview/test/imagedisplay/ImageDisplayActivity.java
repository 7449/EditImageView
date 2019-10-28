package com.davemorrissey.labs.subscaleview.test.imagedisplay;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.davemorrissey.labs.subscaleview.test.AbstractFragmentsActivity;
import com.davemorrissey.labs.subscaleview.test.Page;
import com.davemorrissey.labs.subscaleview.test.R.id;

import java.util.Arrays;
import java.util.List;

import static com.davemorrissey.labs.subscaleview.test.R.layout.fragments_activity;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_p1_subtitle;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_p1_text;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_p2_subtitle;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_p2_text;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_p3_subtitle;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_p3_text;
import static com.davemorrissey.labs.subscaleview.test.R.string.display_title;

public class ImageDisplayActivity extends AbstractFragmentsActivity {

    private static final List<Class<? extends Fragment>> FRAGMENTS = Arrays.asList(
            ImageDisplayLargeFragment.class,
            ImageDisplayRotateFragment.class,
            ImageDisplayRegionFragment.class
    );

    public ImageDisplayActivity() {
        super(display_title, fragments_activity, Arrays.asList(
                new Page(display_p1_subtitle, display_p1_text),
                new Page(display_p2_subtitle, display_p2_text),
                new Page(display_p3_subtitle, display_p3_text)
        ));
    }

    @Override
    protected void onPageChanged(int page) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(id.frame, FRAGMENTS.get(page).newInstance())
                    .commit();
        } catch (Exception e) {
            Log.e(ImageDisplayActivity.class.getName(), "Failed to load fragment", e);
        }
    }

}
