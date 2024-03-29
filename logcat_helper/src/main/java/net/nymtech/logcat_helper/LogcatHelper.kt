package net.nymtech.logcat_helper

import net.nymtech.logcat_helper.model.LogMessage

object LogcatHelper {
    fun logs(callback: (input: LogMessage) -> Unit) {
        clear()
        Runtime.getRuntime().exec("logcat -v epoch")
            .inputStream
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { callback(LogMessage.from(it)) }
            }
    }

    fun clear() {
        Runtime.getRuntime().exec("logcat -c")
    }
}