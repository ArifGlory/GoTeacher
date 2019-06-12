package com.goteacher.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.goteacher.utils.model.UserModel;

import static com.goteacher.Apps.app;

/**
 * Created by raya on 11/07/17.
 */

public class PreferencesHelper {

    private SharedPreferences prefs;

    private String keySort = "keySort";
    private String keyCategory = "keyCategory";

    public PreferencesHelper() {
        prefs = PreferenceManager.getDefaultSharedPreferences(app());
    }

    public void clear() {
        setUsername("");
        setImage("");
        setPhone("");
        setGender(true);
        setAddress("");
        setOccupation("");
        setEducation("");
        setAdmin(false);
        setActive(false);
        setSort(0);
        setCategory(0);
    }

    public void setUsername(String value) {
        prefs.edit().putString(UserModel.key.name.name(), value).apply();
    }

    public String getUsername() {
        return prefs.getString(UserModel.key.name.name(),"");
    }

    public void setEducation(String value) {
        prefs.edit().putString(UserModel.key.education.name(), value).apply();
    }

    public String getEducation() {
        return prefs.getString(UserModel.key.education.name(),"");
    }

    public void setAdmin(boolean value) {
        prefs.edit().putBoolean(UserModel.key.admin.name(), value).apply();
    }

    public boolean isActive() {
        return prefs.getBoolean(UserModel.key.active.name(),false);
    }

    public void setActive(boolean value) {
        prefs.edit().putBoolean(UserModel.key.active.name(), value).apply();
    }

    public boolean isAdmin() {
        return prefs.getBoolean(UserModel.key.admin.name(),false);
    }

    public void setSort(int value) {
        prefs.edit().putInt(keySort, value).apply();
    }

    public int getSort() {
        return prefs.getInt(keySort,0);
    }


    public void setCategory(int value) {
        prefs.edit().putInt(keyCategory, value).apply();
    }

    public int getCategory() {
        return prefs.getInt(keyCategory,0);
    }

    public void setGender(boolean value) {
        prefs.edit().putBoolean(UserModel.key.gender.name(), value).apply();
    }

    public boolean getGender() {
        return prefs.getBoolean(UserModel.key.gender.name(),true);
    }

    public void setPhone(String value) {
        prefs.edit().putString(UserModel.key.phone.name(), value).apply();
    }

    public String getPhone() {
        return prefs.getString(UserModel.key.phone.name(),"");
    }

    public void setOccupation(String value) {
        prefs.edit().putString(UserModel.key.occupation.name(), value).apply();
    }

    public String getOccupation() {
        return prefs.getString(UserModel.key.occupation.name(),"");
    }

    public void setAddress(String value) {
        prefs.edit().putString(UserModel.key.address.name(), value).apply();
    }

    public String getAddress() {
        return prefs.getString(UserModel.key.address.name(),"");
    }

    public void setImage(String value) {
        prefs.edit().putString(UserModel.key.imgURL.name(), value).apply();
    }

    public String getImage() {
        return prefs.getString(UserModel.key.imgURL.name(),"");
    }

}
