package com.example.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent

object UrlLauncher {

    private const val TAG = "UrlLauncher"

    fun openUrl(context: Context, url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            Log.w(TAG, "Chrome Custom Tabs not available, falling back to ACTION_VIEW", e)
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open URL: $url", e2)
            }
        }
    }
}
