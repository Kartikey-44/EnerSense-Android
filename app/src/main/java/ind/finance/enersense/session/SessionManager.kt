package ind.finance.enersense.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var consumerId: String
        get() = prefs.getString(KEY_CONSUMER_ID, "APT01") ?: "APT01"
        set(value) = prefs.edit().putString(KEY_CONSUMER_ID, value).apply()

    var meterId: String
        get() = prefs.getString(KEY_METER_ID, "MTR01") ?: "MTR01"
        set(value) = prefs.edit().putString(KEY_METER_ID, value).apply()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "enersense_session"
        private const val KEY_CONSUMER_ID = "consumer_id"
        private const val KEY_METER_ID = "meter_id"
    }
}
