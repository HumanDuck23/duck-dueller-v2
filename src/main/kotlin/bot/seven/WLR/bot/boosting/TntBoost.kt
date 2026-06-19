package bot.seven.WLR.bot.boosting

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class TntBoost : BoostingBotBase(
    queueCommand = "/play duels_bowspleef_duel",
    gameStartTriggerMessage = "Opponent:",
    quickRefresh = 500
) {

    private var gameHasStartedForBoosting = false

    override fun getName(): String {
        return "TNT Boosting"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.bowspleef_duel_wins",
                "losses" to "player.stats.Duels.bowspleef_duel_losses",
                "ws" to "player.stats.Duels.current_bowspleef_winstreak",
            )
        )
    }

    override fun onJoinGame() {
        super.onJoinGame()
    }

    override fun onGameStart() {
        super.onGameStart()
    }

    override fun onGameEnd() {
        super.onGameEnd()
    }

    @SubscribeEvent
    fun onBoostClientTick(ev: ClientTickEvent) {
        if (mc.thePlayer != null && toggled()) {
            super.onTick()
        }
    }
}