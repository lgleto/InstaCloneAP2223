package ipca.example.instaclone

import android.content.Context

object Preferences {
    const val PREF_FCM_TOKEN = "pref_fcm_token"
}

var Context.PREF_FCM_TOKEN : String?
    get(){
        val sharedPref = getSharedPreferences(Preferences.PREF_FCM_TOKEN, Context.MODE_PRIVATE) ?:  return null
        return sharedPref.getString(Preferences.PREF_FCM_TOKEN, "")
    }
    set(value){
        val sharedPref = getSharedPreferences(Preferences.PREF_FCM_TOKEN, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            if (value != null) {
                putString(Preferences.PREF_FCM_TOKEN, value.toString())
            }
            commit()
        }
    }
