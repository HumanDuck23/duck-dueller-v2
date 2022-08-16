package best.spaghetcodes.duckdueller.core

import best.spaghetcodes.duckdueller.DuckDueller
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType

import java.io.File

class Config : Vigilant(File(DuckDueller.configLocation)) {

    /*
        GENERAL
     */

    @Property(
        type = PropertyType.SELECTOR,
        name = "Current Bot",
        description = "The bot you want to use",
        category = "General",
        options = ["Sumo", "Boxing", "Classic"]
    )
    val currentBot = 0

    @Property(
        type = PropertyType.TEXT,
        name = "API Key",
        description = "This account's API key, can also be set using \"/api new\".",
        placeholder = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        category = "General",
    )
    val apiKey = ""

    @Property(
        type = PropertyType.SWITCH,
        name = "Send Webhook Messages",
        description = "Whether or not the bot should send a discord webhook message after each game.",
        category = "General",
    )
    val sendWebhookMessages = false

    @Property(
        type = PropertyType.TEXT,
        name = "Discord Webhook URL",
        description = "The webhook URL to send messages to.",
        category = "General",
    )
    val webhookURL = ""

    /*
        COMBAT
     */

    @Property(
        type = PropertyType.NUMBER,
        name = "Min CPS",
        description = "The minimum CPS that the bot will be clicking at.",
        category = "Combat",
        min = 6,
        max = 15,
        increment = 1
    )
    val minCPS = 10

    @Property(
        type = PropertyType.NUMBER,
        name = "Max CPS",
        description = "The maximum CPS that the bot will be clicking at.",
        category = "Combat",
        min = 9,
        max = 18,
        increment = 1
    )
    val maxCPS = 10

    @Property(
        type = PropertyType.NUMBER,
        name = "Look Speed",
        description = "How fast the bot can look around (lower number = less snappy, slightly less accurate when teleporting)",
        category = "Combat",
        min = 5,
        max = 15,
        increment = 1
    )
    val lookSpeed = 10

    @Property(
        type = PropertyType.NUMBER,
        name = "Look Randomization",
        description = "How much randomization should happen when looking (higher number = more jittery aim)",
        category = "Combat",
        min = 5,
        max = 15,
        increment = 1
    )
    val lookRand = 10

    @Property(
        type = PropertyType.NUMBER,
        name = "Max Look Distance",
        description = "How close the opponent has to be before the bot starts tracking them",
        category = "Combat",
        min = 120,
        max = 180,
        increment = 5
    )
    val maxDistanceLook = 150

    @Property(
        type = PropertyType.NUMBER,
        name = "Max Attack Distance",
        description = "How close the opponent has to be before the bot starts attacking them",
        category = "Combat",
        min = 5,
        max = 15,
        increment = 150
    )
    val maxDistanceAttack = 10

    init {
        addDependency("webhookURL", "sendWebhookMessages")

        initialize()
    }
}
