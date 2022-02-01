package com.example.tddd80application.extensions

import android.app.Activity
import android.content.Intent
import android.view.View
import java.text.SimpleDateFormat

// - - - Activity Extensions - - -

fun <A: Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

// - - - String Extensions - - -

fun String.strToAppTimestamp(): String {
    var sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
    var date = sdf.parse(this)
    var sdfOut = SimpleDateFormat("MMM dd, yyyy HH:mm")
    return sdfOut.format(date)
}

// - - - View Extensions - - -

fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}