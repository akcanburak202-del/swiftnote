package dev.swiftnote.probe.media

import android.service.notification.NotificationListenerService

/** Exists only to unlock MediaSessionManager.getActiveSessions. Deliberately ignores every notification. */
class ProbeListener : NotificationListenerService()
