package cr4zyc4t.cafe24h;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;


public class SettingsActivity extends AppCompatActivity {
    private static boolean needRestart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.root, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        if (needRestart) {
//            new AlertDialog.Builder(this).setMessage("Do you want to restart app?").setCancelable(false)
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent mStartActivity = new Intent(SettingsActivity.this, MainActivity.class);
//                            int mPendingIntentId = 123456;
//                            PendingIntent mPendingIntent = PendingIntent.getActivity(SettingsActivity.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                            AlarmManager mgr = (AlarmManager) SettingsActivity.this.getSystemService(Context.ALARM_SERVICE);
//                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//                            System.exit(0);
//
//                            SettingsActivity.super.onBackPressed();
//                        }
//                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            SettingsActivity.super.onBackPressed();
//                        }
//                    })
//                    .show();
//        } else {
//            super.onBackPressed();
//        }
//    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

            Preference preference = findPreference("statusbar_pref");
            if (preference != null) {
                preference.setSummary(sharedPreferences.getString("statusbar_pref", ""));
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals("actionbar_hide")) {
                needRestart = true;
            }
            if (s.equals("statusbar_pref")) {
                Preference preference = findPreference(s);
                preference.setSummary(sharedPreferences.getString(s, ""));
            }
        }
    }
}
