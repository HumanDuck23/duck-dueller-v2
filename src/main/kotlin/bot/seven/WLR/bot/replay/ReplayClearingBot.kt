package bot.seven.WLR.bot.replay

// Import Config

import bot.seven.WLR.wlr
import bot.seven.WLR.bot.BotBase
import bot.seven.WLR.core.Config
import bot.seven.WLR.utils.ChatUtils
import bot.seven.WLR.utils.RandomUtils
import bot.seven.WLR.utils.TimeUtils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// TickEvent is not directly used in this class, but BotBase uses it. Fine to keep if no specific import error.

class ReplayClearingBot : BotBase(queueCommand = "") {

    private var commandsSentThisSession = 0
    private var commandsSkippedThisSession = 0
    private var sessionStartTimeMillis = 0L

    private var maxCommandsToExecute = 500 // Default, will be overridden
    private var minDelayMs = 2000          // Default, will be overridden
    private var maxDelayMs = 5000          // Default, will be overridden

    private var nextCommandTime = 0L
    private var sessionInProgress = false

    private var lastCommandSentTime = 0L
    private var waitingForCooldown = false
    private val cooldownMessageTrigger = "This command is on cooldown!"
    private val housingCommand = "/housing random"

    override fun getName(): String = "Replay Clearing"

    private fun startSession() {
        commandsSentThisSession = 0
        commandsSkippedThisSession = 0
        sessionStartTimeMillis = System.currentTimeMillis()

        // Use Config object directly
        maxCommandsToExecute = Config.replayClearingCommandCount
        minDelayMs = Config.replayClearingMinDelay
        maxDelayMs = Config.replayClearingMaxDelay

        if (minDelayMs <= 0) minDelayMs = 1000 // Ensure minDelay is at least 1s
        if (maxDelayMs < minDelayMs) maxDelayMs = minDelayMs // Ensure maxDelay is not less than minDelay

        nextCommandTime = System.currentTimeMillis() + RandomUtils.randomIntInRange(minDelayMs / 2, minDelayMs)
        sessionInProgress = true
        waitingForCooldown = false
        ChatUtils.info("Replay Clearing session started. Target: $maxCommandsToExecute commands.")
    }

    private fun stopSession(completed: Boolean = false) {
        sessionInProgress = false
        waitingForCooldown = false
        if (completed) {
            ChatUtils.info("Replay Clearing session completed. Sent: $commandsSentThisSession, Skipped: $commandsSkippedThisSession.")
        } else {
            ChatUtils.info("Replay Clearing session stopped. Sent: $commandsSentThisSession, Skipped: $commandsSkippedThisSession.")
        }
        // Consider toggling the bot off or switching to another bot if that's desired behavior
        // DuckDueller.updateActiveBot(newReplayClearingModeState = false) // Example
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        // No direct config access here, but uses DuckDueller.bot which is fine
        if (wlr.bot == null || !toggled() || wlr.bot !== this || !sessionInProgress) {
            return
        }

        val message = event.message.unformattedText
        if (message.contains(cooldownMessageTrigger)) {
            // Check if the cooldown message is genuinely for the command we just sent
            if (System.currentTimeMillis() - lastCommandSentTime < 2000) { // Within 2s of sending our command
                if (commandsSentThisSession > 0) {
                    commandsSentThisSession-- // Decrement as it was effectively skipped
                }
                commandsSkippedThisSession++

                val cooldownWaitDelay = 1000L + RandomUtils.randomIntInRange(0, 500) // Wait 1-1.5s for cooldown
                nextCommandTime = System.currentTimeMillis() + cooldownWaitDelay
                waitingForCooldown = true // Mark that we are waiting for a server-side cooldown
                ChatUtils.info("Replay Clearing: Cooldown detected, waiting ${cooldownWaitDelay}ms.")
            }
        }
    }

    override fun onTick() {
        if (mc.thePlayer == null) return

        if (wlr.bot == null || !toggled() || wlr.bot !== this) {
            if (sessionInProgress) {
                stopSession(false) // Stop if bot changes or is toggled off
            }
            return
        }

        // Use Config object directly
        val currentCommandCountTarget = Config.replayClearingCommandCount

        // Start a new session if not already in progress and target not met
        if (!sessionInProgress && commandsSentThisSession < currentCommandCountTarget && currentCommandCountTarget > 0) {
            startSession()
        }

        if (!sessionInProgress) {
            return // Nothing to do if session isn't active
        }

        if (System.currentTimeMillis() >= nextCommandTime) {
            if (commandsSentThisSession < currentCommandCountTarget) {
                ChatUtils.sendAsPlayer(housingCommand)
                lastCommandSentTime = System.currentTimeMillis()
                commandsSentThisSession++
                waitingForCooldown = false // Reset cooldown waiting state

                // Check for completion immediately after sending
                if (commandsSentThisSession >= currentCommandCountTarget) {
                    val self = this // Capture 'this' for use in lambda
                    // Short delay to ensure the last command is processed / cooldown message can arrive
                    TimeUtils.setTimeout(fun() {
                        // Re-check condition in case target changed or session stopped by other means
                        if (self.commandsSentThisSession >= (Config.replayClearingCommandCount) && self.sessionInProgress) {
                            self.stopSession(completed = true)
                        }
                    }, 200) // Increased delay slightly to allow for cooldown message processing
                }

                // If session is still in progress (not just completed), schedule next command
                if (sessionInProgress) {
                    // Use Config object directly
                    val currentMinDelay = Config.replayClearingMinDelay
                    val currentMaxDelay = Config.replayClearingMaxDelay
                    val delay = RandomUtils.randomIntInRange(
                        if (currentMinDelay <= 0) 1000 else currentMinDelay,
                        if (currentMaxDelay < currentMinDelay) currentMinDelay else currentMaxDelay
                    )
                    nextCommandTime = System.currentTimeMillis() + delay
                }
            } else { // Target met or exceeded
                if (sessionInProgress) {
                    stopSession(completed = true)
                }
            }
        }
    }

    private fun getSessionUptimeString(): String {
        if (sessionStartTimeMillis == 0L) return if (sessionInProgress) "00:00:00 (Starting)" else "00:00:00 (Not Started)"
        // if (!sessionInProgress) return "00:00:00 (Paused)" // Covered by sessionInProgress check above implicitly

        val uptimeMillis = System.currentTimeMillis() - sessionStartTimeMillis
        val seconds = (uptimeMillis / 1000) % 60
        val minutes = (uptimeMillis / (1000 * 60)) % 60
        val hours = (uptimeMillis / (1000 * 60 * 60))

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @SubscribeEvent
    fun onRenderReplayClearingHUD(event: RenderGameOverlayEvent.Text) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) {
            return
        }

        if (wlr.bot == null || !this.toggled() || wlr.bot !== this) {
            return
        }

        val fr: FontRenderer = mc.fontRendererObj
        val M = EnumChatFormatting.GRAY
        val V = EnumChatFormatting.WHITE
        val H = EnumChatFormatting.LIGHT_PURPLE

        val xPos = 5f
        var yPos = 5f
        val yStep = fr.FONT_HEIGHT + 2

        fr.drawStringWithShadow(
            "${H}${EnumChatFormatting.BOLD}WLR${EnumChatFormatting.RESET} ${M}> ${EnumChatFormatting.YELLOW}${getName()}",
            xPos,
            yPos,
            0xFFFFFF
        )
        yPos += yStep + 2

        // Use Config object directly
        val targetCommands = Config.replayClearingCommandCount.toString() // Use toString for consistency if it can be 0
        fr.drawStringWithShadow("${M}Sent: ${V}$commandsSentThisSession / $targetCommands", xPos, yPos, 0xFFFFFF)
        yPos += yStep
        fr.drawStringWithShadow("${M}Skipped (Cooldown): ${EnumChatFormatting.RED}$commandsSkippedThisSession", xPos, yPos, 0xFFFFFF)
        yPos += yStep
        fr.drawStringWithShadow("${M}Uptime: ${EnumChatFormatting.AQUA}${getSessionUptimeString()}", xPos, yPos, 0xFFFFFF)
        yPos += yStep

        if (!sessionInProgress) {
            // Use Config object directly
            val configTarget = Config.replayClearingCommandCount
            if (commandsSentThisSession >= configTarget && configTarget > 0) {
                fr.drawStringWithShadow("${EnumChatFormatting.GREEN}Session Complete!", xPos, yPos, 0xFFFFFF)
            } else {
                fr.drawStringWithShadow("${EnumChatFormatting.YELLOW}Session Paused/Stopped.", xPos, yPos, 0xFFFFFF)
            }
            yPos += yStep
        }
    }

    // Override empty methods from BotBase as this bot doesn't participate in duels
    override fun onGameStart() {}
    override fun onGameEnd() {}
    override fun onAttack() {}
    override fun onAttacked() {}
    override fun onJoinGame() {}
    override fun beforeStart() {}
    override fun beforeLeave() {}
    override fun onFoundOpponent() {}

    override fun onToggleOff() {
        super.onToggleOff() // Call super for common cleanup
        stopSession(false) // Specifically stop the replay clearing session
        ChatUtils.info("Replay Clearing Bot toggled off.")
    }
}