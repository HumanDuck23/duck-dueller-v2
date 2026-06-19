package bot.seven.WLR.bot.boosting

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class UhcBoost : BoostingBotBase(
    queueCommand = "/play duels_uhc_duel",
    gameStartTriggerMessage = "Opponent:",
    quickRefresh = 500
) {

    private var gameHasStartedForBoosting = false

    override fun getName(): String {
        return "UHC Boosting (Solos)"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.uhc_duel_wins",
                "losses" to "player.stats.Duels.uhc_duel_losses",
                "ws" to "player.stats.Duels.current_uhc_winstreak",
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