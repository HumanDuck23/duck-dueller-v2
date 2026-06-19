package bot.seven.WLR.utils

import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {

    /**
     * Call a function after delay ms
     */
    fun setTimeout(function: () -> Unit, delay: Int): Timer? {
        if (delay < 0) {
            println("Error scheduling timer: delay cannot be negative (${delay}ms)")
            return null
        }
        try {
            val timer = Timer()
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        function()
                    }
                }, delay.toLong()
            )
            return timer
        } catch (e: Exception) {
            println("Error scheduling timer with ${delay}ms: " + e.message)
        }
        return null
    }

    /**
     * Call a function every interval ms after delay ms
     */
    fun setInterval(function: () -> Unit, delay: Int, interval: Int): Timer? {
        if (delay < 0 || interval <= 0) {
            println("Error scheduling interval timer: delay cannot be negative (${delay}ms) and interval must be positive (${interval}ms)")
            return null
        }
        try {
            val timer = Timer()
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        function()
                    }
                }, delay.toLong(), interval.toLong()
            )
            return timer
        } catch (e: Exception) {
            println("Error scheduling timer with ${delay}ms delay and ${interval}m interval: " + e.message)
        }
        return null
    }

    /**
     * Formats a duration in milliseconds into a human-readable string (e.g., "1h 23m 45s").
     * @param millis The duration in milliseconds.
     * @param showSecondsWithMinutesOnly If true, seconds will only be shown if hours are zero.
     *                                   If false (default), seconds are always shown if non-zero.
     * @return A formatted string representation of the duration.
     */
    fun formatMillis(millis: Long, showSecondsWithMinutesOnly: Boolean = false): String {
        if (millis < 0) return "0s"

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        val sb = StringBuilder()

        if (hours > 0) {
            sb.append(hours).append("h")
            if (minutes > 0 || (!showSecondsWithMinutesOnly && seconds > 0) ) {
                sb.append(" ")
            }
        }

        if (minutes > 0) {
            sb.append(minutes).append("m")
            if (!showSecondsWithMinutesOnly && seconds > 0 && hours == 0L) {
                sb.append(" ")
            } else if (!showSecondsWithMinutesOnly && seconds > 0 && hours > 0L) {
                sb.append(" ")
            }
        }

        if (seconds > 0) {
            if (showSecondsWithMinutesOnly && hours > 0) {
            } else {
                sb.append(seconds).append("s")
            }
        }

        if (sb.isEmpty()) {
            return "0s"
        }

        return sb.toString().trim()
    }
}