package com.lockminds.brass_services

import android.content.Context
import android.content.SharedPreferences


/**
 * Session manager to save and fetch data from SharedPreferences
 */
class SessionManager (context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences( Constants.PREFERENCE_KEY, Context.MODE_PRIVATE)

    /**
     * Function to fetch user id
     */
    fun getUserId(): String? {
        return prefs.getString(Constants.USER_ID, null)
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(Constants.LOGIN_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun getLoginToken(): String? {
        return prefs.getString(Constants.LOGIN_TOKEN, null)
    }


    /**
     * Function to fetch user id
     */
    fun fetchId(): String? {
        return prefs.getString(Constants.USER_ID, null)
    }

    /**
     * Function to fetch user id
     */
    fun getFCMToken(): String? {
        return prefs.getString(Constants.FCM_TOKEN, null)
    }

    /**
     * Function to fetch user id
     */
    fun getEmail(): String? {
        return prefs.getString(Constants.EMAIL, null)
    }

    /**
     * Function to fetch user id
     */
    fun getName(): String? {
        return prefs.getString(Constants.NAME, null)
    }

    /**
     * Function to fetch user id
     */
    fun getPhotoUrl(): String? {
        return prefs.getString(Constants.PHOTO_URL, null)
    }

}