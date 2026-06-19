package bot.seven.WLR

import bot.seven.WLR.bot.BotBase
import bot.seven.WLR.bot.StateManager
import bot.seven.WLR.bot.player.Camera
import bot.seven.WLR.bot.player.LobbyMovement
import bot.seven.WLR.bot.player.Mouse
import bot.seven.WLR.commands.ConfigCommand
import bot.seven.WLR.core.Config
import bot.seven.WLR.core.KeyBindings
import bot.seven.WLR.events.packet.PacketListener
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(
    modid = wlr.MOD_ID,
    name = wlr.MOD_NAME,
    version = wlr.VERSION,
    clientSideOnly = true
)
class wlr {

    companion object {
        const val MOD_ID = "wlr"
        const val MOD_NAME = "WLR"
        const val VERSION = "1.0"
        const val configLocation = "./config/wlr.json"

        val mc: Minecraft by lazy { Minecraft.getMinecraft() }

        var bot: BotBase? = null

        fun updateActiveBot(
            newReplayClearingModeState: Boolean? = null,
            newBoostingModeState: Boolean? = null,
            newRegularBotIndex: Int? = null,
            newBoostingBotIndex: Int? = null
        ) {
            val effectiveReplayClearingEnabled = newReplayClearingModeState ?: Config.enableReplayClearingMode
            val effectiveBoostingEnabled = newBoostingModeState ?: Config.enableBoostingMode

            val effectiveRegularBotIdx = newRegularBotIndex ?: Config.currentBot
            val effectiveBoostingBotIdx = newBoostingBotIndex ?: Config.selectedBoostingBotIndex

            var newBotToSelect: BotBase? = null

            if (effectiveReplayClearingEnabled) {
                newBotToSelect = Config.replayClearingBotInstance
            } else if (effectiveBoostingEnabled) {
                if (Config.boostingBotInstances.isNotEmpty()) {
                    val minIdx = 0
                    val maxIdx = Config.boostingBotInstances.size - 1
                    val actualIndexToUse = effectiveBoostingBotIdx.coerceIn(minIdx, maxIdx)
                    newBotToSelect = Config.boostingBotInstances.getOrNull(actualIndexToUse)
                } else {
                    newBotToSelect = null
                }
            } else {
                newBotToSelect = Config.bots[effectiveRegularBotIdx]
                if (newBotToSelect == null && Config.bots.isNotEmpty()) {
                    val fallbackIndex = Config.bots.keys.minOrNull() ?: 0
                    newBotToSelect = Config.bots[fallbackIndex]
                    if (newBotToSelect != null) {
                    }
                }
            }

            if (bot === newBotToSelect && bot != null) {
                return
            }

            val oldSelectedBot = bot
            if (oldSelectedBot != null) {
                try {
                    MinecraftForge.EVENT_BUS.unregister(oldSelectedBot)
                } catch (e: Exception) {
                    println("Error unregistering old bot: ${e.message}")
                }
            }

            bot = newBotToSelect

            if (bot != null) {
                try {
                    MinecraftForge.EVENT_BUS.register(bot)
                    println("WLR: Active bot set to ${bot!!::class.java.simpleName}")
                } catch (e: Exception) {
                    println("Error registering new bot: ${e.message}")
                    bot = null
                }
            } else {
                println("WLR: No active bot selected.")
            }
        }
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        try {
            println("$MOD_NAME: Loading configuration... Current bot from config: ${Config.currentBot}")
        } catch (e: Throwable) {
            System.err.println("Error during $MOD_NAME pre-initialization (Config loading):")
            e.printStackTrace()
        }
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        try {
            ConfigCommand().register()
            KeyBindings.register()

            MinecraftForge.EVENT_BUS.register(this)
            MinecraftForge.EVENT_BUS.register(PacketListener())
            MinecraftForge.EVENT_BUS.register(Camera)
            MinecraftForge.EVENT_BUS.register(StateManager)
            MinecraftForge.EVENT_BUS.register(Mouse)
            MinecraftForge.EVENT_BUS.register(LobbyMovement)
            MinecraftForge.EVENT_BUS.register(KeyBindings)

            updateActiveBot()

            println("$MOD_NAME Initialized successfully!")

        } catch (e: Throwable) {
            System.err.println("Error during $MOD_NAME initialization:")
            e.printStackTrace()
        }
    }
}