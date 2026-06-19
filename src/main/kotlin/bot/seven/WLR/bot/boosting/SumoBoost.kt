package bot.seven.WLR.bot.boosting

import bot.seven.WLR.wlr
import bot.seven.WLR.bot.StateManager
import bot.seven.WLR.bot.player.Movement
import bot.seven.WLR.core.Config
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class SumoBoost : BoostingBotBase(
    queueCommand = "/play duels_sumo_duel",
    gameStartTriggerMessage = "Opponent:",
    quickRefresh = 500
) {

    override fun getName(): String {
        return "Sumo Boosting"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.sumo_duel_wins",
                "losses" to "player.stats.Duels.sumo_duel_losses",
                "ws" to "player.stats.Duels.current_sumo_winstreak",
            )
        )
    }

    override fun onGameStartDetected() {
        super.onGameStartDetected()
        Movement.clearAll()
    }

    override fun onJoinGame() {
        super.onJoinGame()
        if (Movement.jumping()) {
            Movement.stopJumping()
        }
        Movement.clearAll()
    }

    override fun onGameStart() {
        super.onGameStart()
        Movement.clearAll()
    }

    override fun onGameEnd() {
        Movement.clearAll()
        super.onGameEnd()
    }

    @SubscribeEvent
    fun onBoostClientTick(ev: ClientTickEvent) {
        if (mc.thePlayer == null) {
            return
        }

        if (toggled() && wlr.bot === this) {

            if (StateManager.state != StateManager.States.PLAYING && !gameHasStartedCurrentCycle) {
                if (!Movement.forward()) {
                    Movement.startForward()
                }
                if (!Movement.sprinting() && Config.lobbyMovement) {
                    Movement.startSprinting()
                }
                if (mc.thePlayer.onGround && !Movement.jumping()) {
                    Movement.startJumping()
                }
            } else {
                if (Movement.forward() || Movement.sprinting() || Movement.jumping()) {
                    Movement.clearAll()
                }
            }
        } else {
            if (Movement.forward() || Movement.sprinting() || Movement.jumping()) {
                Movement.clearAll()
            }
        }
    }
}