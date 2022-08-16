package best.spaghetcodes.duckdueller.core

import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.lwjgl.input.Keyboard

object KeyBindings {

    val toggleBotKeyBinding = KeyBinding("duck.toggleBot", Keyboard.KEY_SEMICOLON, "category.duck")

    fun register() {
        ClientRegistry.registerKeyBinding(toggleBotKeyBinding)
    }

}