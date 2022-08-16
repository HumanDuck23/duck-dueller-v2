package dev.debuggings.examplemod

import dev.debuggings.examplemod.commands.ConfigCommand
import dev.debuggings.examplemod.core.Config
import dev.debuggings.examplemod.events.packet.PacketEvent
import dev.debuggings.examplemod.events.packet.PacketListener
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod(
    modid = ExampleMod.MOD_ID,
    name = ExampleMod.MOD_NAME,
    version = ExampleMod.VERSION
)
class ExampleMod {

    companion object {
        const val MOD_ID = "examplemod"
        const val MOD_NAME = "ExampleMod"
        const val VERSION = "1.0"
        const val configLocation = "./config/examplemod.toml"

        val mc: Minecraft = Minecraft.getMinecraft()
        var config: Config? = null
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config = Config()
        config?.preload()

        ConfigCommand().register()

        MinecraftForge.EVENT_BUS.register(PacketListener())
    }
}
