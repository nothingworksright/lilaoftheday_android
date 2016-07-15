package com.lilaoftheday.lilaoftheday.activities;


import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lilaoftheday.lilaoftheday.R;
import com.lilaoftheday.lilaoftheday.fragments.MainFragment;
import com.lilaoftheday.lilaoftheday.fragments.PreferenceFragment;
import com.lilaoftheday.lilaoftheday.utilities.FragmentBoss;

public class MainActivity extends AppCompatActivity {

    /*
     * TODO: Write unit tests.
     * https://developer.android.com/training/testing/unit-testing/local-unit-tests.html
     *
     * TODO: Create separate layout under layout-large-land and other similar qualifiers.
     * https://developer.android.com/guide/practices/screens_support.html
     *
     */

    public Boolean savedInstanceNow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        savedInstanceNow = false;

        setContentView(R.layout.activity_main);

        // Initialize default preference values.
        PreferenceManager.setDefaultValues(
                this, // Context
                R.xml.preferences, // Resource ID
                false // only if this method has never been called in the past
        );

        // Create a toolbar.
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Only do this stuff when the activity is started for the very first time.
        if (savedInstanceState == null) {
            showMainFragment();
        }

        updateSupportActionBarTitle(getString(R.string.app_name));

    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int backStackCount = fm.getBackStackEntryCount();
        // If there's only one fragment left open, finish() the activity. If not, proceed.
        if (backStackCount == 1 && !savedInstanceNow) {
            finish();
        } else if (backStackCount > 1 && !savedInstanceNow) {
            // Details to identify the mainFragment photo list.
            int containerViewId = R.id.mainContainer;
            String tagTitle = getString(R.string.app_name);
            int dbRecordId = -1;
            String tagCombo = FragmentBoss.tagJoiner(tagTitle, containerViewId, dbRecordId);
            // If the fragment being backed out of is the mainFragment photo list, bury it instead
            // of removing it. If not, pop it.
            if (fm.getBackStackEntryAt(backStackCount - 1).getName().equals(tagCombo)) {
                FragmentBoss.buryFragmentInBackStack(fm, tagCombo);
            } else {
                FragmentBoss.popBackStack(fm);
            }
        } else {
            // If all else fails, call the super.onBackPressed() method.
            super.onBackPressed();
        }
        // Redundancy to call the resulting top fragment's onResume() method.
        FragmentBoss.topFragmentOnResume(fm);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceNow = true;
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        Handle tool bar item clicks here. The tool bar will automatically handle clicks on the
        Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        */
        int id = item.getItemId();
        if (id == R.id.action_preferences) {

            int containerViewId = R.id.photoContainer;
            String tagTitle = getString(R.string.action_preferences);
            int dbRecordId = -1;
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = new PreferenceFragment();
            String tagCombo = FragmentBoss.tagJoiner(tagTitle, containerViewId, dbRecordId);
            FragmentBoss.replaceFragmentInContainer(
                    containerViewId,
                    fm,
                    fragment,
                    tagCombo
            );

            return true;
        }
        // Notification check for debugging
        /*if (id == R.id.action_notification_check) {
            AlarmChecker alarmChecker;
            alarmChecker = new AlarmChecker();
            alarmChecker.checkAlarm(getApplicationContext());
        }*/
        /*return super.onOptionsItemSelected(item);*/
        return false;
    }

    public void showMainFragment() {

        String tagTitle = getString(R.string.app_name);
        int containerViewId = R.id.mainContainer;
        int dbRecordId = -1;
        String tagCombo = FragmentBoss.tagJoiner(tagTitle, containerViewId, dbRecordId);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = MainFragment.newInstance(dbRecordId);

        FragmentBoss.replaceFragmentInContainer(containerViewId, fm, fragment, tagCombo);

    }

    public void resurfaceView(int containerViewId) {
        View v = findViewById(containerViewId);
        v.bringToFront();
    }

    private void updateSupportActionBarTitle(String tag) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(tag);
        }
    }

    // http://stackoverflow.com/questions/24463691/how-to-show-imageview-full-screen-on-imageview-click
    public void fullScreenModeToggle() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("fullScreenModeToggle", "Turning immersive mode mode off.");
        } else {
            Log.i("fullScreenModeToggle", "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }

    public void actionBarVisibility(boolean visible) {
        if (getSupportActionBar() != null) {
            if (visible) {
                getSupportActionBar().show();
            } else {
                getSupportActionBar().hide();
            }
        }
    }

    public Dialog fullScreenPhoto(int imageResourceId) {

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(imageResourceId);

        RelativeLayout.LayoutParams params;
        params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                // Tap anywhere to close dialog.
                this.dismiss();
                return true;
            }
        };
        dialog.addContentView(imageView, params);

        return dialog;

    }

}

