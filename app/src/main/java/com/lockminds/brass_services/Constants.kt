package com.lockminds.brass_services

class Constants{

    companion object {
        @JvmField val BUSINESS_KEY: String ="news_key"
        @JvmField val NEWS_KEY: String ="news_key"
        @JvmField val ORDER_KEY: String ="order_key"
        @JvmField val SERVICE_KEY = "service_key"
        @JvmField val PRODUCT_KEY = "product_key"
        @JvmField val TAG = "KELLY"
        @JvmField val FILE_PATH = "imagePath"
        @JvmField val SENDER_KEY = "senderKey"
        @JvmField val RECEIVER_KEY = "receiverKey"
        @JvmField val KEY = "key"
        @JvmField val FIRSTNAME = "firstName"
        @JvmField val LASTNAME = "firstName"
        @JvmField val PREFERENCE_KEY = "com.lockminds.brass_services.preferences"
        @JvmField val PREFERENCE_USER_ID = "pref_user_id"
        @JvmField val PREFERENCE_DEVICE_TOKEN = "pref_device_token"
        @JvmField val PREFERENCE_REG_STATUS = "pref_reg_status"
        @JvmField val LOGIN_TOKEN = "login_token"
        @JvmField val LOGIN_STATUS = "login_status"
        @JvmField val USER_ID = "user_id"
        @JvmField val PHOTO_URL = "photo_url"
        @JvmField val PHONE_NUMBER = "phone"
        @JvmField val NAME = "name"
        @JvmField val EMAIL = "email"
        @JvmField val WAREHOUSE = "warehouse"
        @JvmField val TEAM_EMAIL = "team_email"
        @JvmField val TEAM_NAME = "team_name"
        @JvmField val TEAM_ID = "team_id"
        @JvmField val TEAM_PHONE = "team_phone"
        @JvmField val TEAM_ADDRESS = "team_address"
        @JvmField val POLICY_URL = "policy_url"
        @JvmField val CHANGE_DETAILS = "change_details"
        @JvmField val CHANGE_PICTURE = "change_picture"
        const val GEOFENCE_RADIUS_IN_METERS = 100F
        const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = 86400000L
        const val FCM_TOKEN = "fcm_token"
        const val INTENT_PARAM_1 = "intent_param_1"
        const val SUCCESS_RESULT = 0

        const val FAILURE_RESULT = 1

        private const val PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress"

        const val RECEIVER = "$PACKAGE_NAME.RECEIVER"

        const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"

        const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"


        const val ACTION_GEOFENCE_EVENT = "com.lockminds.brass_services.geofence.action.ACTION_GEOFENCE_EVENT"
        const val LOCKMINDS_ACTION_GEOFENCE_EVENT = "com.lockminds.brass_services.geofence.action.ACTION_GEOFENCE_EVENT_FIRED"
        const val LOCKMINDS_ACTION_GEOFENCE_EVENT_ENTERING = "com.lockminds.brass_services.geofence.action.ACTION_GEOFENCE_EVENT_ENTERING"
        const val LOCKMINDS_ACTION_GEOFENCE_EVENT_EXIT = "com.lockminds.brass_services.geofence.action.ACTION_GEOFENCE_EVENT_EXIT"
    }

}