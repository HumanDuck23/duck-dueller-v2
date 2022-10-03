package best.spaghetcodes.duckdueller

import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.StateManager
import best.spaghetcodes.duckdueller.bot.bots.Sumo
import best.spaghetcodes.duckdueller.bot.player.LobbyMovement
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.commands.ConfigCommand
import best.spaghetcodes.duckdueller.core.Config
import best.spaghetcodes.duckdueller.core.KeyBindings
import best.spaghetcodes.duckdueller.events.packet.PacketListener
import com.google.gson.Gson
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@Mod(
    modid = DuckDueller.MOD_ID,
    name = DuckDueller.MOD_NAME,
    version = DuckDueller.VERSION
)
class DuckDueller {

    companion object {
        const val MOD_ID = "duckdueller"
        const val MOD_NAME = "DuckDueller"
        const val VERSION = "0.1.0"
        const val configLocation = "./config/duckdueller.toml"

        val mc: Minecraft = Minecraft.getMinecraft()
        val gson = Gson()
        var config: Config? = null
        var bot: BotBase? = null

        fun swapBot(b: BotBase) {
            if (bot != null) MinecraftForge.EVENT_BUS.unregister(bot) // make sure to unregister the current bot
            bot = b
            MinecraftForge.EVENT_BUS.register(bot) // register the new bot
        }
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config = Config()
        config?.preload()

        ConfigCommand().register()
        KeyBindings.register()

        MinecraftForge.EVENT_BUS.register(PacketListener())
        MinecraftForge.EVENT_BUS.register(StateManager)
        MinecraftForge.EVENT_BUS.register(Mouse)
        MinecraftForge.EVENT_BUS.register(LobbyMovement)
        MinecraftForge.EVENT_BUS.register(KeyBindings)

        swapBot(config?.bots?.get(config?.currentBot ?: 0) ?: Sumo())
    }
}
