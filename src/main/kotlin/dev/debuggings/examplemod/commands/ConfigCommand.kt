package dev.debuggings.examplemod.commands

import dev.debuggings.examplemod.ExampleMod
import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler

class ConfigCommand : Command("examplemod") {

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(ExampleMod.config?.gui())
    }
}
