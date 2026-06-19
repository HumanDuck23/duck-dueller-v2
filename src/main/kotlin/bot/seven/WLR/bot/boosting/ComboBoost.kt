package bot.seven.WLR.bot.boosting

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class ComboBoost : BoostingBotBase(
    queueCommand = "/play duels_combo_duel",
    gameStartTriggerMessage = "Opponent:",
    quickRefresh = 500
) {

    override fun getName(): String {
        return "Combo Boosting"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.combo_duel_wins",
                "losses" to "player.stats.Duels.combo_duel_losses",
                "ws" to "player.stats.Duels.current_combo_winstreak",
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