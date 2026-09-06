package com.mail2dev.planfora.util

import java.text.SimpleDateFormat
import java.util.*

object TimeFormatter {
    fun formatTime(timestamp: Long, use24Hour: Boolean): String {
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDate(timestamp: Long, pattern: String = "MMM dd, yyyy"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}
