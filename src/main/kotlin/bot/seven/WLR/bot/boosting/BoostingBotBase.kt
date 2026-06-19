package bot.seven.WLR.bot.boosting

import bot.seven.WLR.wlr // Still needed for DuckDueller.bot
import bot.seven.WLR.bot.BotBase
import bot.seven.WLR.bot.Session
import bot.seven.WLR.core.Config // CRITICAL: Import the Config object directly
import bot.seven.WLR.utils.ChatUtils
import bot.seven.WLR.utils.RandomUtils
import bot.seven.WLR.utils.TimeUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

abstract class BoostingBotBase(
    queueCommand: String,
    val gameStartTriggerMessage: String,
    quickRefresh: Int
) : BotBase(queueCommand, quickRefresh = quickRefresh) {

    protected var gameHasStartedCurrentCycle = false

    abstract override fun getName(): String

    @SubscribeEvent
    open fun onBoostingBotChatEvent(ev: ClientChatReceivedEvent) {
        if (toggled() && mc.thePlayer != null && Config.enableBoostingMode && wlr.bot === this) {
            val unformatted = ev.message.unformattedText
            if (unformatted.contains(gameStartTriggerMessage)) {
                if (!gameHasStartedCurrentCycle) {
                    gameHasStartedCurrentCycle = true
                    onGameStartDetected()

                    val configuredDelay = Config.boostingRequeueDelay
                    val randomizedAdditionalDelay = RandomUtils.randomIntInRange(50, 200)
                    val totalRandomizedDelay = configuredDelay + randomizedAdditionalDelay

                    TimeUtils.setTimeout(fun() {
                        if (toggled() && Config.enableBoostingMode && wlr.bot === this) {
                            ChatUtils.sendAsPlayer(this.queueCommand)
                            Session.addLoss()
                            TimeUtils.setTimeout(fun () { gameHasStartedCurrentCycle = false }, 750)
                        } else {
                            gameHasStartedCurrentCycle = false
                        }
                    }, totalRandomizedDelay)
                }
            }
        }
    }

    protected open fun onGameStartDetected() {
    }

    override fun onJoinGame() {
        super.onJoinGame()
        if (Config.enableBoostingMode && wlr.bot === this) {
            gameHasStartedCurrentCycle = false
        }
    }

    override fun onGameStart() {
        if (Config.enableBoostingMode && wlr.bot === this) {
        } else {
            super.onGameStart()
        }
    }

    override fun onGameEnd() {
        if (Config.enableBoostingMode && wlr.bot === this) {
            gameHasStartedCurrentCycle = false
        } else {
            super.onGameEnd()
        }
    }

    override fun onTick() {
        if (wlr.bot === this) {
            super.onTick()
        }
    }
}