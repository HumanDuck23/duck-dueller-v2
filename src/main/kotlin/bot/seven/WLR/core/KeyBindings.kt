package bot.seven.WLR.core

import bot.seven.WLR.bot.Session
import bot.seven.WLR.gui.ConfigGui
import bot.seven.WLR.utils.ChatUtils
import bot.seven.WLR.wlr
import gg.essential.api.EssentialAPI
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard

object KeyBindings {
    val toggleBotKeyBinding = KeyBinding("Toggle Bot", Keyboard.KEY_SEMICOLON, "WLR")
    val configGuiKeyBinding = KeyBinding("Open Config GUI", Keyboard.KEY_RSHIFT, "WLR")
    fun register() {
        ClientRegistry.registerKeyBinding(toggleBotKeyBinding)
        ClientRegistry.registerKeyBinding(configGuiKeyBinding)
    }

    @SubscribeEvent
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (configGuiKeyBinding.isPressed) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                Minecraft.getMinecraft().addScheduledTask {
                    EssentialAPI.getGuiUtil().openScreen(ConfigGui())
                }
            }
        }

        if (toggleBotKeyBinding.isPressed) {
            wlr.bot?.toggle()
            if (wlr.bot != null) {
                val isNowToggled = wlr.bot!!.toggled()
                val toggleStatusString = if (isNowToggled) {
                    "${EnumChatFormatting.GREEN}ON"
                } else {
                    "${EnumChatFormatting.RED}OFF"
                }
                ChatUtils.info("WLR has been toggled $toggleStatusString")
                if (isNowToggled) {
                    ChatUtils.info("Current selected bot: ${EnumChatFormatting.GREEN}${wlr.bot!!.getName()}")
                    wlr.bot!!.joinGame()
                    Session.reset()
                } else {
                    wlr.bot!!.onToggleOff()
                }
            }
        }
    }
}