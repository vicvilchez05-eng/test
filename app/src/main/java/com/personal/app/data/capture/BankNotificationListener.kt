package com.personal.app.data.capture

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.personal.app.PersonalApplication
import com.personal.app.data.model.CapturedTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * Listens to the phone's notifications and forwards the ones posted by a bank app to the inbox.
 * Requires the user to grant "notification access" in system settings (see [openAccessSettings]);
 * the app never asks for it silently. Only packages in [BankNotificationParser.bankApps] (or
 * containing "bbva") are read; everything else is ignored without being stored.
 */
class BankNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!BankNotificationParser.isBankApp(sbn.packageName)) return
        val app = application as? PersonalApplication ?: return
        val container = app.container
        if (!container.preferences.prefs.value.captureEnabled) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()
        if (title.isNullOrBlank() && text.isNullOrBlank()) return

        val captured = build(sbn.packageName, title.orEmpty(), text.orEmpty(), sbn.postTime)
        scope.launch { container.repository.addCaptured(captured) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        /** Stable id from the content, so the same notification re-posted (or updated) is not duplicated. */
        fun build(packageName: String, title: String, text: String, postedAt: Long): CapturedTransaction {
            val parsed = BankNotificationParser.parse(title, text)
            val digest = MessageDigest.getInstance("SHA-1").digest("$packageName|$title|$text|${postedAt / 60_000}".toByteArray())
            val id = "cap:" + digest.joinToString("") { "%02x".format(it) }.take(24)
            return CapturedTransaction(
                id = id, packageName = packageName, title = title, text = text, postedAt = postedAt,
                amountMinor = parsed?.amountMinor, merchant = parsed?.merchant, suggestedCategory = parsed?.category,
            )
        }

        fun hasAccess(context: Context): Boolean =
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

        fun openAccessSettings(context: Context) {
            val component = ComponentName(context, BankNotificationListener::class.java)
            val detail = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component.flattenToString())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(detail) }.onFailure {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
}
