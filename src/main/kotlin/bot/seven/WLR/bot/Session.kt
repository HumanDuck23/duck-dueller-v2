package bot.seven.WLR.bot

import net.minecraft.util.EnumChatFormatting
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

object Session {

    var wins = 0
    var losses = 0
    var startTime: Long = System.currentTimeMillis()
    var bestWinstreak = 0
    var currentStreak = 0

    fun reset() {
        startTime = System.currentTimeMillis()
        wins = 0
        losses = 0
        bestWinstreak = 0
        currentStreak = 0
    }

    fun addWin() {
        wins++
        if (currentStreak >= 0) {
            currentStreak++
        } else {
            currentStreak = 1
        }

        if (currentStreak > bestWinstreak) {
            bestWinstreak = currentStreak
        }
    }

    fun addLoss() {
        losses++
        if (currentStreak <= 0) {
            currentStreak--
        } else {
            currentStreak = -1
        }
    }

    fun getUptimeMillis(): Long {
        return System.currentTimeMillis() - startTime
    }

    fun getUptimeString(): String {
        val millis = getUptimeMillis()
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getSession(): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        val ratio = df.format(wins.toFloat() / (if (losses == 0) 1F else losses.toFloat()))
        return "Session: ${EnumChatFormatting.GREEN}Wins: $wins${EnumChatFormatting.RESET} - ${EnumChatFormatting.RED}Losses: $losses${EnumChatFormatting.RESET} - W/L: ${EnumChatFormatting.LIGHT_PURPLE}${ratio}${EnumChatFormatting.RESET}"
    }

}