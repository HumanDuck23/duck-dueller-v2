package best.spaghetcodes.duckdueller.core

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.bots.Boxing
import best.spaghetcodes.duckdueller.bot.bots.Classic
import best.spaghetcodes.duckdueller.bot.bots.Sumo
import best.spaghetcodes.duckdueller.utils.ChatUtils
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
    var apiKey = ""

    @Property(
        type = PropertyType.SWITCH,
        name = "Lobby Movement",
        description = "Whether or not the bot should move in pre-game lobbies.",
        category = "General",
    )
    val lobbyMovement = true

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
    val maxCPS = 14

    @Property(
        type = PropertyType.NUMBER,
        name = "Horizontal Look Speed",
        description = "How fast the bot can look left/right (lower number = less snappy, slightly less accurate when teleporting)",
        category = "Combat",
        min = 5,
        max = 15,
        increment = 1
    )
    val lookSpeedHorizontal = 10

    @Property(
        type = PropertyType.NUMBER,
        name = "Vertical Look Speed",
        description = "How fast the bot can look up/down (lower number = less snappy, slightly less accurate when teleporting)",
        category = "Combat",
        min = 1,
        max = 15,
        increment = 1
    )
    val lookSpeedVertical = 5

    @Property(
        type = PropertyType.DECIMAL_SLIDER,
        name = "Look Randomization",
        description = "How much randomization should happen when looking (higher number = more jittery aim)",
        category = "Combat",
        minF = 0f,
        maxF = 2f,
    )
    val lookRand = 1f

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
        increment = 1
    )
    val maxDistanceAttack = 10

    /*
        Auto GG
     */

    @Property(
        type = PropertyType.SWITCH,
        name = "Enable AutoGG",
        description = "Send a gg message after every game",
        category = "AutoGG",
    )
    val sendAutoGG = true

    @Property(
        type = PropertyType.TEXT,
        name = "AutoGG Message",
        description = "AutoGG message the bot sends after every game",
        category = "AutoGG",
    )
    val ggMessage = "gg"

    @Property(
        type = PropertyType.NUMBER,
        name = "AutoGG Delay",
        description = "How long to wait after the game before sending the message",
        category = "AutoGG",
        min = 50,
        max = 1000,
        increment = 50
    )
    val ggDelay = 100

    @Property(
        type = PropertyType.SWITCH,
        name = "Game Start Message",
        description = "Send a message as soon as the game starts",
        category = "AutoGG",
    )
    val sendStartMessage = false

    @Property(
        type = PropertyType.TEXT,
        name = "Start Message",
        description = "Message to send at the beginning of the game",
        category = "AutoGG",
    )
    val startMessage = "GL HF!"

    @Property(
        type = PropertyType.NUMBER,
        name = "Start Message Delay",
        description = "How long to wait before sending the start message",
        category = "AutoGG",
        min = 50,
        max = 1000,
        increment = 50
    )
    val startMessageDelay = 100

    /*
        Auto Requeue
     */

    @Property(
        type = PropertyType.NUMBER,
        name = "Auto Requeue Delay",
        description = "How long to wait after a game before re-queueing",
        category = "Auto Requeue",
        min = 500,
        max = 5000,
        increment = 50
    )
    val autoRqDelay = 2500

    @Property(
        type = PropertyType.NUMBER,
        name = "Requeue After No Game",
        description = "How long to wait before re-queueing if no game starts",
        category = "Auto Requeue",
        min = 15,
        max = 60,
        increment = 5
    )
    val rqNoGame = 30

    @Property(
        type = PropertyType.SWITCH,
        name = "Paper Requeue",
        description = "Use the paper to requeue",
        category = "Auto Requeue",
    )
    val paperRequeue = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Fast Requeue",
        description = "Faster Requeue (no rewards)",
        category = "Auto Requeue",
    )
    val fastRequeue = true

    /*
        Queue Dodging
     */

    @Property(
        type = PropertyType.SWITCH,
        name = "Enable Queue Dodging",
        description = "Whether or not the bot should dodge people based on stats",
        category = "Queue Dodging",
    )
    val enableDodging = true

    @Property(
        type = PropertyType.SLIDER,
        name = "Dodge Wins",
        description = "How many wins a player can have before being dodged",
        category = "Queue Dodging",
        min = 500,
        max = 20000
    )
    val dodgeWins = 4000

    @Property(
        type = PropertyType.NUMBER,
        name = "Dodge WS",
        description = "How large a player's winstreak can be before being dodged",
        category = "Queue Dodging",
        min = 10,
        max = 100,
        increment = 5
    )
    val dodgeWS = 15

    @Property(
        type = PropertyType.DECIMAL_SLIDER,
        name = "Dodge W/L",
        description = "How large a player's w/l ratio can be before being dodged",
        category = "Queue Dodging",
        minF = 2f,
        maxF = 15f,
    )
    val dodgeWLR = 3.0f

    @Property(
        type = PropertyType.SWITCH,
        name = "Dodge Lost To",
        description = "Whether or not the bot should dodge people it already lost against",
        category = "Queue Dodging",
    )
    val dodgeLostTo = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Dodge No Stats",
        description = "Whether or not the bot should dodge when no stats are found (nicked player or hypixel error)",
        category = "Queue Dodging",
    )
    val dodgeNoStats = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Strict Dodging",
        description = "If Hypixel prevents the bot from leaving (woah there, slow down!), it will disconnect and reconnect to dodge.",
        category = "Queue Dodging",
    )
    val strictDodging = false

    val bots = mapOf(0 to Sumo(), 1 to Boxing(), 2 to Classic())

    init {
        addDependency("webhookURL", "sendWebhookMessages")

        addDependency("ggMessage", "sendAutoGG")
        addDependency("ggDelay", "sendAutoGG")

        addDependency("startMessage", "sendStartMessage")
        addDependency("startMessageDelay", "sendStartMessage")

        addDependency("dodgeWins", "enableDodging")
        addDependency("dodgeWS", "enableDodging")
        addDependency("dodgeWLR", "enableDodging")
        addDependency("dodgeLostTo", "enableDodging")
        addDependency("dodgeNoStats", "enableDodging")

        registerListener("currentBot") { bot: Int ->
            if (bots.keys.contains(bot)) {
                DuckDueller.swapBot(bots[bot]!!)
            }
        }

        initialize()
    }
}
