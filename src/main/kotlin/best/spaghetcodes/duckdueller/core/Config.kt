package best.spaghetcodes.duckdueller.core

import best.spaghetcodes.duckdueller.DuckDueller
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType

import java.io.File

class Config : Vigilant(File(DuckDueller.configLocation)) {

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Swag Mod",
        description = "Example Switch",
        category = "General"
    )
    var toggleSwag = false

    @Property(
        type = PropertyType.TEXT,
        name = "Swag Text",
        description = "Example Text",
        category = "General"
    )
    var swagText = ":sunglasses:"

    init {
        addDependency("swagText", "toggleSwag")

        initialize()
    }
}
