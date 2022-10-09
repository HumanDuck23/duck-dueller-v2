package best.spaghetcodes.duckdueller.bot

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object StateManager {

    enum class States {
        LOBBY,
        GAME,
        PLAYING
    }

    var state = States.LOBBY
    var gameFull = false
    var gameStartedAt = -1L
    var lastGameDuration = 0L

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(ev: ClientChatReceivedEvent) {
        val unformatted = ev.message.unformattedText
        if (unformatted.matches(Regex(".* has joined \\(./2\\)!"))) {
            state = States.GAME
            if (unformatted.matches(Regex(".* has joined \\(2/2\\)!"))) {
                gameFull = true
            }
        } else if (unformatted.contains("Opponent:")) {
            state = States.PLAYING
            gameStartedAt = System.currentTimeMillis()
        } else if (unformatted.contains("Accuracy")) {
            state = States.GAME
            gameFull = false
            lastGameDuration = System.currentTimeMillis() - gameStartedAt
        } else if (unformatted.contains("has quit!")) {
            gameFull = false
        }
    }

    @SubscribeEvent
    fun onJoinWorld(ev: EntityJoinWorldEvent) {
        if (DuckDueller.mc.thePlayer != null && ev.entity == DuckDueller.mc.thePlayer) {
            state = States.LOBBY
            gameFull = false
            gameStartedAt = -1L
        }
    }

}