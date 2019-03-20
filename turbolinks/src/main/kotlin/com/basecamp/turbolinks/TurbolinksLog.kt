package com.basecamp.turbolinks

import android.util.Log

internal object TurbolinksLog {
    private const val DEFAULT_TAG = "TurbolinksLog"
    internal var enableDebugLogging = false

    fun d(msg: String) = log(Log.DEBUG, DEFAULT_TAG, msg)

    fun e(msg: String) = log(Log.ERROR, DEFAULT_TAG, msg)

    private fun log(logLevel: Int, tag: String, msg: String) {
        when (logLevel) {
            Log.DEBUG -> if (enableDebugLogging) Log.d(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
        }
    }
}

internal fun logEvent(event: String, attributes: List<Pair<String, Any>>) {
    val description = attributes.joinToString(prefix = "[", postfix = "]", separator = ", ") {
        "${it.first}: ${it.second}"
    }
    TurbolinksLog.d("$event ".padEnd(35, '.') + " $description")
}
