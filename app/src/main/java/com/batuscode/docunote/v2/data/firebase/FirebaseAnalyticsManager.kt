package com.batuscode.docunote.v2.data.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsManager @Inject constructor(
    private val analytics: FirebaseAnalytics
){
    fun setUserAttributes(userTier: String, preferredLanguage: String, location: String) {
        // Bu nitelikler Firebase Console'da "Conditions" oluşturmak için kullanılır
        analytics.setUserProperty("user_tier", userTier)
        analytics.setUserProperty("app_language", preferredLanguage)
        analytics.setUserProperty("client_location", location)
    }
}