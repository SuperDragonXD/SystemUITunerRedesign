package com.zacharee1.systemuituner.fragmenthelpers;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.zacharee1.systemuituner.ItemDetailFragment;
import com.zacharee1.systemuituner.utils.SettingsUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class StatbarHelper
{
    private ItemDetailFragment mFragment;
    private final SharedPreferences mSharedPreferences;

    public StatbarHelper(ItemDetailFragment fragment) {
        mFragment = fragment;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mFragment.getContext());

        preferenceListeners();
        setSwitchPreferenceStates();
        switchPreferenceListeners();
    }

    private void preferenceListeners() {
        Preference resetBL = mFragment.findPreference("reset_blacklist");
        Preference backupBL = mFragment.findPreference("backup_blacklist");
        Preference restoreBL = mFragment.findPreference("restore_blacklist");

        resetBL.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                SettingsUtils.writeSecure(mFragment.getContext(), "icon_blacklist", "");
                setSwitchPreferenceStates();
                return true;
            }
        });

        backupBL.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                String currentBL = Settings.Secure.getString(mFragment.getContext().getContentResolver(), "icon_blacklist");
                SettingsUtils.writeGlobal(mFragment.getContext(), "icon_blacklist_backup", currentBL);
                setSwitchPreferenceStates();
                return true;
            }
        });

        restoreBL.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                String backupBL = Settings.Global.getString(mFragment.getContext().getContentResolver(), "icon_blacklist_backup");
                SettingsUtils.writeSecure(mFragment.getContext(), "icon_blacklist", backupBL);
                setSwitchPreferenceStates();
                return true;
            }
        });
    }

    private void setSwitchPreferenceStates() {
        String blString = Settings.Secure.getString(mFragment.getActivity().getContentResolver(), "icon_blacklist");
        if (blString == null) blString = "";

        ArrayList<String> blItems = new ArrayList<>(Arrays.asList(blString.split("[,]")));

        for (int i = 0; i < mFragment.getPreferenceScreen().getRootAdapter().getCount(); i++) {
            Object o = mFragment.getPreferenceScreen().getRootAdapter().getItem(i);

            if (o instanceof SwitchPreference && !((SwitchPreference)o).getTitle().toString().toLowerCase().contains("high brightness warning")) {
                SwitchPreference pref = (SwitchPreference) o;

                pref.setChecked(true);

                if (!blString.isEmpty()) {
                    String key = pref.getKey();

                    if (key != null) {
                        ArrayList<String> keyItems = new ArrayList<>(Arrays.asList(key.split("[,]")));

                        for (String s : keyItems) {
                            if (blItems.contains(s)) {
                                pref.setChecked(false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void switchPreferenceListeners() {
        for (int i = 0; i < mFragment.getPreferenceScreen().getRootAdapter().getCount(); i++) {
            Object o = mFragment.getPreferenceScreen().getRootAdapter().getItem(i);

            if (o instanceof SwitchPreference && !((SwitchPreference)o).getTitle().toString().toLowerCase().contains("high brightness warning")) {
                final SwitchPreference pref = (SwitchPreference) o;

                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o)
                    {
                        String key = preference.getKey();

                        if (key != null) {
                            String currentBL = Settings.Secure.getString(mFragment.getContext().getContentResolver(), "icon_blacklist");
                            if (currentBL == null) currentBL = "";

                            if (!Boolean.valueOf(o.toString())) {
                                if (currentBL.isEmpty()) {
                                    currentBL = key;
                                } else {
                                    currentBL = currentBL.concat("," + key);
                                }
                            } else {
                                ArrayList<String> blItems = new ArrayList<>(Arrays.asList(currentBL.split("[,]")));
                                ArrayList<String> keyItems = new ArrayList<>(Arrays.asList(key.split("[,]")));

                                for (String s : keyItems) {
                                    if (blItems.contains(s)) {
                                        blItems.remove(s);
                                    }
                                }

                                currentBL = blItems.toString()
                                        .replace("[", "")
                                        .replace("]", "")
                                        .replace(" ", "");
                            }

                            SettingsUtils.writeSecure(mFragment.getContext(), "icon_blacklist", currentBL);
                        }
                        return true;
                    }
                });
            }
        }
    }

}
