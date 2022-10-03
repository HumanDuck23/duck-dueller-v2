package best.spaghetcodes.duckdueller.core

import best.spaghetcodes.duckdueller.DuckDueller
import gg.essential.api.EssentialAPI
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard

object KeyBindings {

    val toggleBotKeyBinding = KeyBinding("duck.toggleBot", Keyboard.KEY_SEMICOLON, "category.duck")
    val configGuiKeyBinding = KeyBinding("duck.configGui", Keyboard.KEY_RSHIFT, "category.duck")

    fun register() {
        ClientRegistry.registerKeyBinding(toggleBotKeyBinding)
        ClientRegistry.registerKeyBinding(configGuiKeyBinding)
    }

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (configGuiKeyBinding.isPressed) {
            EssentialAPI.getGuiUtil().openScreen(DuckDueller.config?.gui())
        }
    }

}