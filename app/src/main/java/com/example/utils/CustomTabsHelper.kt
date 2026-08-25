package com.example.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.toColorInt

object CustomTabsHelper {

    const val BROWSER_CHROME = "chrome"
    const val BROWSER_FIREFOX = "firefox"

    const val PACKAGE_CHROME = "com.android.chrome"
    const val PACKAGE_FIREFOX = "org.mozilla.firefox"

    /**
     * Checks whether an app package is installed on the device.
     */
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Opens the target URL inside a Custom Tab using the user's preferred browser (Chrome or Firefox).
     */
    fun openCustomTab(
        context: Context,
        url: String,
        preferredBrowser: String = BROWSER_CHROME
    ): Boolean {
        if (url.isBlank()) return false

        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        val uri = try {
            Uri.parse(formattedUrl)
        } catch (e: Exception) {
            Toast.makeText(context, "Invalid URL: $url", Toast.LENGTH_SHORT).show()
            return false
        }

        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor("#191333".toColorInt())
            .setSecondaryToolbarColor("#0F0B1E".toColorInt())
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .setDefaultColorSchemeParams(defaultColors)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()

        val targetPackage = when (preferredBrowser.lowercase()) {
            BROWSER_FIREFOX -> PACKAGE_FIREFOX
            else -> PACKAGE_CHROME
        }

        val isPreferredInstalled = isPackageInstalled(context, targetPackage)

        try {
            if (isPreferredInstalled) {
                customTabsIntent.intent.setPackage(targetPackage)
            }
            customTabsIntent.launchUrl(context, uri)
            return true
        } catch (ex: ActivityNotFoundException) {
            // Fallback without package restriction
            try {
                val fallbackIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setDefaultColorSchemeParams(defaultColors)
                    .build()
                fallbackIntent.launchUrl(context, uri)
                return true
            } catch (fallbackEx: Exception) {
                // Fallback to standard ACTION_VIEW
                return try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(browserIntent)
                    true
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to open link in browser", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        } catch (e: Exception) {
            return try {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(browserIntent)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}
