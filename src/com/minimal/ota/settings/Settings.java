/*=========================================================================
 *
 *  PROJECT:  SlimRoms
 *            Team Slimroms (http://www.slimroms.net)
 *
 *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
 *            All rights reserved
 *
 *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
 *
 *  AUTHORS:     fronti90, mnazim, tchaari, kufikugel
 *  DESCRIPTION: SlimOTA keeps our rom up to date
 *
 *=========================================================================
 */

package com.minimal.ota.settings;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceActivity;

import com.minimal.ota.R;
import com.minimal.ota.updater.UpdateListener;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class Settings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MinimalOTASettings";

    private static final String KEY_UPDATE_INTERVAL = "update_interval";
    private static final String LAST_INTERVAL = "lastInterval";

    private ListPreference mUpdateInterval;
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.minimal_ota_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mUpdateInterval = (ListPreference) prefs.findPreference(KEY_UPDATE_INTERVAL);
        mUpdateInterval.setValueIndex(getUpdateInterval());
        mUpdateInterval.setSummary(mUpdateInterval.getEntry());
        mUpdateInterval.setOnPreferenceChangeListener(this);     
        
     }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mUpdateInterval) {
            int intervalValue = Integer.valueOf((String) objValue);
            int index = mUpdateInterval.findIndexOfValue((String) objValue);
            setUpdateInterval(intervalValue);
            mUpdateInterval.setSummary(mUpdateInterval.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void setUpdateInterval(int interval) {
            boolean enableUpdateCheck = true;
            switch(interval) {
                case 0:
                    UpdateListener.interval = AlarmManager.INTERVAL_DAY*7;
                    break;
                case 1:
                    UpdateListener.interval = AlarmManager.INTERVAL_HALF_DAY*2;
                    break;
                case 2:
                    UpdateListener.interval = AlarmManager.INTERVAL_HOUR*12;
                    break;
                case 3:
                    enableUpdateCheck = false;
                    break;
            }
            if (enableUpdateCheck) {
                SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
                prefs.edit().putLong(LAST_INTERVAL, UpdateListener.interval).apply();
                WakefulIntentService.cancelAlarms(this);
                WakefulIntentService.scheduleAlarms(new UpdateListener(), this, true);
            } else {
                SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
                prefs.edit().putLong(LAST_INTERVAL, 1).apply();
                com.minimal.ota.updater.ConnectivityReceiver.disableReceiver(this);
                WakefulIntentService.cancelAlarms(this);
            }
    }

    private int getUpdateInterval() {
        SharedPreferences prefs = getSharedPreferences(LAST_INTERVAL, 0);
        long value = prefs.getLong(LAST_INTERVAL,0);
        int settingsValue;
        if (value == AlarmManager.INTERVAL_DAY*7) {
            settingsValue = 0;
        } else if (value == AlarmManager.INTERVAL_HALF_DAY*2 || value == 0) {
            settingsValue = 1;
        } else if (value == AlarmManager.INTERVAL_HOUR*12) {
            settingsValue = 2;
        } else {
            settingsValue = 3;
        }
        return settingsValue;
    }

}
