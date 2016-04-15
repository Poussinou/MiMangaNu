package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import ar.rulosoft.mimanganu.utils.ThemeColors;

public class MainActivity extends AppCompatActivity {
    public int[] colors;
    public ActionBar mActBar;
    boolean darkTheme;
    OnBackListener backListener;
    private SharedPreferences pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        darkTheme = pm.getBoolean("dark_theme", false);
        setTheme(darkTheme ? R.style.AppTheme_miDark : R.style.AppTheme_miLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.fragment_preferences, false);
        if (savedInstanceState == null) {
            MainFragment fmm = new MainFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fmm).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Bundle bundle = new Bundle();
        super.onSaveInstanceState(bundle, outPersistentState);
    }

    @Override
    protected void onResume() {
        if (darkTheme != pm.getBoolean("dark_theme", false)) {
            // re start to apply new theme
            Intent i = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            System.exit(0);
        }

        colors = ThemeColors.getColors(pm, getApplicationContext());
        mActBar = getSupportActionBar();
        if (mActBar != null) mActBar.setBackgroundDrawable(new ColorDrawable(colors[0]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(colors[0]);
            window.setStatusBarColor(colors[4]);
        }
        super.onResume();
    }

    public void setTitle(String title) {
        if (mActBar == null)
            mActBar = getSupportActionBar();
        mActBar.setTitle(title);
    }

    public void enableHomeButton(boolean enable) {
        if (mActBar == null)
            mActBar = getSupportActionBar();
        mActBar.setDisplayHomeAsUpEnabled(enable);
    }

    public void replaceFragment(Fragment fragment, String name) {
        backListener = null;
        getSupportFragmentManager().beginTransaction()/*.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)/*/.replace(R.id.fragment_container, fragment).addToBackStack(name).commit();
    }

    @Override
    public void onBackPressed() {
        if (backListener != null) {
            if (!backListener.onBackPressed())
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    super.onBackPressed();
                }
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public interface OnBackListener {
        boolean onBackPressed();
    }

    public interface OnKeyListener {
        boolean onKeyPressed();
    }
}