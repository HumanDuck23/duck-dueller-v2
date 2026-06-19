package bot.seven.WLR.commands

import bot.seven.WLR.gui.ConfigGui
import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import net.minecraft.client.Minecraft

class ConfigCommand : Command("wlr") {

    @DefaultHandler
    fun handle() {
        Minecraft.getMinecraft().addScheduledTask {
            EssentialAPI.getGuiUtil().openScreen(ConfigGui())
        }
    }
}