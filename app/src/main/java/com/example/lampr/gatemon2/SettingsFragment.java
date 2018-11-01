//package com.example.lampr.gatemon2;
//
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.v7.preference.ListPreference;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceFragmentCompat;
//import android.support.v7.preference.PreferenceManager;
//
//public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
//
//    private static final String TAG = SettingsFragment.class.getSimpleName();
//
//    SharedPreferences sharedPreferences;
//
////    @Override
////    public void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////
////        addPreferencesFromResource(R.xml.preferences);
////    }
//
//    @Override
//    public void onCreatePreferences(Bundle bundle, String s) {
//        //add xml
//        addPreferencesFromResource(R.xml.preferences);
//
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//
//        onSharedPreferenceChanged(sharedPreferences, "rpzw_ip");
//    }
//
//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Preference preference = findPreference(key);
//        if (preference instanceof ListPreference) {
//            ListPreference listPreference = (ListPreference) preference;
//            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
//            if (prefIndex >= 0) {
//                preference.setSummary(listPreference.getEntries()[prefIndex]);
//            }
//        } else {
//            preference.setSummary(sharedPreferences.getString(key, ""));
//
//        }
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        //unregister the preferenceChange listener
//        getPreferenceScreen().getSharedPreferences()
//                .registerOnSharedPreferenceChangeListener(this);
//    }
//
////    @Override
////    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
////        Preference preference = findPreference(key);
////        if (preference instanceof ListPreference) {
////            ListPreference listPreference = (ListPreference) preference;
////            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
////            if (prefIndex >= 0) {
////                preference.setSummary(listPreference.getEntries()[prefIndex]);
////            }
////        } else {
////            preference.setSummary(sharedPreferences.getString(key, ""));
////
////        }
////    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        //unregister the preference change listener
//        getPreferenceScreen().getSharedPreferences()
//                .unregisterOnSharedPreferenceChangeListener(this);
//    }
//}
