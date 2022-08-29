package best.spaghetcodes.duckdueller.bot

import net.minecraft.util.EnumChatFormatting
import java.math.RoundingMode
import java.text.DecimalFormat

object Session {

    var wins = 0
    var losses = 0
    var startTime: Long = -1

    fun getSession(): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        val ratio = df.format(wins.toFloat() / (if (losses == 0) 1F else losses.toFloat()))
        return "Session: ${EnumChatFormatting.GREEN}Wins: $wins${EnumChatFormatting.RESET} - ${EnumChatFormatting.RED}Losses: $losses${EnumChatFormatting.RESET} - W/L: ${EnumChatFormatting.LIGHT_PURPLE}${ratio}${EnumChatFormatting.RESET}"
    }

}