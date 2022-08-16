package best.spaghetcodes.duckdueller.bot

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.player.Combat
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.core.KeyBindings
import best.spaghetcodes.duckdueller.events.packet.PacketEvent
import best.spaghetcodes.duckdueller.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Timer
import kotlin.concurrent.thread

/**
 * Base class for all bots
 * @param queueCommand Command to join a new game
 * @param quickRefresh MS for which to quickly refresh opponent entity
 */
open class BotBase(val queueCommand: String, val quickRefresh: Int = 10000) {

    protected val mc = Minecraft.getMinecraft()

    private var toggled = false
    fun toggled() = toggled
    fun toggle() {
        toggled = !toggled
    }

    private var attackedID = -1

    private var statKeys: Map<String, String> = mapOf("wins" to "", "losses" to "", "ws" to "")

    private var playerCache: HashMap<String, String> = hashMapOf()
    private var playersSent: ArrayList<String> = arrayListOf()

    private var opponent: EntityPlayer? = null
    private var opponentTimer: Timer? = null
    private var calledFoundOpponent = false

    protected var combo = 0
    protected var opponentCombo = 0

    fun opponent() = opponent

    /********
     * Methods to override
     ********/

    /**
     * Called when the bot attacks the opponent
     * Triggered by the damage sound, not the clientside attack event
     */
    protected open fun onAttack() {}

    /**
     * Called when the bot is attacked
     * Triggered by the damage sound, not the clientside attack event
     */
    protected open fun onAttacked() {}

    /**
     * Called when the game starts
     */
    protected open fun onGameStart() {}

    /**
     * Called when the game ends
     */
    protected open fun onGameEnd() {}

    /**
     * Called when the bot joins a game
     */
    protected open fun onJoinGame() {}

    /**
     * Called before the game starts (1s)
     */
    protected open fun beforeStart() {}

    /**
     * Called before the bot leaves the game (dodge)
     */
    protected open fun beforeLeave() {}

    /**
     * Called when the opponent entity is found
     */
    protected open fun onFoundOpponent() {}

    /**
     * Called every tick
     */
    protected open fun onTick() {}

    /********
     * Protected Methods
     ********/

    protected fun setStatKeys(keys: Map<String, String>) {
        statKeys = keys
    }

    /********
     * Base Methods
     ********/

    @SubscribeEvent
    fun onPacket(ev: PacketEvent) {
        if (toggled) {
            when (ev.getPacket()) {
                is S19PacketEntityStatus -> { // use the status packet for attack events
                    val packet = ev.getPacket() as S19PacketEntityStatus
                    if (packet.opCode.toInt() == 2) { // damage
                        val entity = packet.getEntity(mc.theWorld)
                        if (entity != null) {
                            if (entity.entityId == attackedID) {
                                attackedID = -1
                                onAttack()
                                combo++
                                opponentCombo = 0
                            } else if (mc.thePlayer != null && entity.entityId == mc.thePlayer.entityId) {
                                onAttacked()
                                combo = 0
                                opponentCombo++
                            }
                        }
                    }
                }
                is S3EPacketTeams -> { // use this for stat checking
                    val packet = ev.getPacket() as S3EPacketTeams
                    if (packet.action == 3 && packet.name == "§7§k") {
                        val players = packet.players
                        for (player in players) {
                            TimeUtils.setTimeout(fun () { // timeout to allow ingame state to update
                                handlePlayer(player)
                            }, 1500)
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onAttackEntityEvent(ev: AttackEntityEvent) {
        if (toggled() && ev.entity == mc.thePlayer) {
            attackedID = ev.target.entityId
        }
    }

    @SubscribeEvent
    fun onClientTick(ev: ClientTickEvent) {
        if (toggled) {
            onTick()
        }

        if (KeyBindings.toggleBotKeyBinding.isPressed) {
            toggle()
            ChatUtils.info("Duck Dueller has been toggled ${if (toggled()) "${EnumChatFormatting.GREEN}on" else "${EnumChatFormatting.RED}off"}")
            if (toggled()) {
                joinGame()
            }
        }
    }

    @SubscribeEvent
    fun onChat(ev: ClientChatReceivedEvent) {
        if (toggled() && mc.thePlayer != null) {
            val unformatted = ev.message.unformattedText

            if (unformatted.contains("The game starts in 2 seconds!")) {
                println(playersSent.joinToString(", "))
                var found = false
                if (playersSent.contains(mc.thePlayer.displayNameString)) {
                    if (playersSent.size > 1) {
                        found = true
                    }
                } else {
                    if (playersSent.size > 0) {
                        found = true
                    }
                }

                if (!found && DuckDueller.config?.dodgeNoStats == true) {
                    ChatUtils.info("No stats found, dodging...")
                    leaveGame()
                    TimeUtils.setTimeout(this::joinGame, RandomUtils.randomIntInRange(4000, 6000))
                }
            }

            if (unformatted.contains("Are you sure? Type /lobby again")) {
                leaveGame()
            }

            if (unformatted.contains("Opponent:")) {
                gameStart()
            }

            if (unformatted.contains("Accuracy")) {
                gameEnd()
            }
        }
    }

    @SubscribeEvent
    fun onJoinWorld(ev: EntityJoinWorldEvent) {
        if (DuckDueller.mc.thePlayer != null && ev.entity == DuckDueller.mc.thePlayer) {
            if (toggled()) {
                playersSent.clear()
                Movement.clearAll()
                Combat.stopRandomStrafe()
                Mouse.stopLeftAC()
            }
        }
    }

    /********
     * Private Methods
     ********/

    private fun gameStart() {
        if (toggled()) {
            val quickRefreshTimer = TimeUtils.setInterval(this::bakery, 200, 50)
            TimeUtils.setTimeout(fun () {
                quickRefreshTimer?.cancel()
                opponentTimer = TimeUtils.setInterval(this::bakery, 0, 5000)
            }, quickRefresh)

            onGameStart()
        }
    }

    private fun gameEnd() {
        if (toggled()) {
            onGameEnd()
            playersSent.clear()
            calledFoundOpponent = true
            opponentTimer?.cancel()
            opponent = null

            if (DuckDueller.config?.sendAutoGG == true) {
                TimeUtils.setTimeout(fun () {
                    ChatUtils.sendAsPlayer(DuckDueller.config?.ggMessage ?: "gg")
                }, DuckDueller.config?.ggDelay ?: 100)

                TimeUtils.setTimeout(this::joinGame, DuckDueller.config?.autoRqDelay ?: 2000)
            }
        }
    }

    private fun bakery() {
        if (StateManager.state == StateManager.States.PLAYING) {
            val entity = EntityUtils.getOpponentEntity()
            if (entity != null) {
                opponent = entity
                if (!calledFoundOpponent) {
                    calledFoundOpponent = true
                    onFoundOpponent()
                }
            }
        }
    }

    /**
     * Called for each player that joins
     */
    private fun handlePlayer(player: String) {
        thread { // move into new thread to avoid blocking the main thread
            if (StateManager.state == StateManager.States.GAME) { // make sure we're in-game, to not spam the api
                if (player.length > 2) { // hypixel sends a bunch of fake 1-2 char entities
                    var uuid: String? = null
                    if (playerCache.containsKey(player)) { // check if the player is in the cache
                        uuid = playerCache[player]
                    } else {
                        uuid = HttpUtils.usernameToUUID(player)
                    }

                    if (uuid == null) { // nicked or fake player
                        //TODO: Check the list of players the bot has lost to
                    } else {
                        playerCache[player] = uuid // cache this player
                        if (!playersSent.contains(player)) { // don't send the same player twice
                            if (mc.thePlayer != null) {
                                if (player == mc.thePlayer.displayNameString) { // if the player is the bot
                                    onJoinGame()
                                } else {
                                    val stats = HttpUtils.getPlayerStats(uuid) ?: return@thread
                                    handleStats(stats, player)
                                }
                            } else {
                                val stats = HttpUtils.getPlayerStats(uuid) ?: return@thread
                                handleStats(stats, player)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle the response from the hypixel api
     */
    private fun handleStats(stats: JsonObject, player: String) {
        if (toggled() && stats.get("success").asBoolean) {
            if (statKeys.containsKey("wins") && statKeys.containsKey("losses") && statKeys.containsKey("ws")) {
                fun getStat(key: String): JsonElement? {
                    var tmpObj = stats

                    for (p in key.split(".")) {
                        if (tmpObj.has(p) && tmpObj.get(p).isJsonObject)
                            tmpObj = tmpObj.get(p).asJsonObject
                        else if (tmpObj.has(p))
                            return tmpObj.get(p)
                        else
                            return null
                    }
                    return null
                }


                if (!playersSent.contains(player)) {
                    playersSent.add(player)
                } else {
                    return
                }

                val wins = getStat(statKeys["wins"]!!)?.asInt ?: 0
                val losses = getStat(statKeys["losses"]!!)?.asInt ?: 0
                val ws = getStat(statKeys["ws"]!!)?.asInt ?: 0

                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.DOWN

                val wlr = wins.toDouble() / (if (losses == 0) 1.0 else losses.toDouble())


                ChatUtils.info("$player ${EnumChatFormatting.GOLD} >> ${EnumChatFormatting.GOLD}Wins: ${EnumChatFormatting.GREEN}$wins ${EnumChatFormatting.GOLD}WLR: ${EnumChatFormatting.GREEN}${df.format(wlr)} ${EnumChatFormatting.GOLD}WS: ${EnumChatFormatting.GREEN}$ws")

                var dodge = false

                if (DuckDueller.config?.enableDodging == true) {
                    val config = DuckDueller.config
                    if (wins > config?.dodgeWins!!) {
                        dodge = true
                    } else if (wlr > config.dodgeWLR) {
                        dodge = true
                    } else if (ws > config.dodgeWS) {
                        dodge = true
                    }
                }

                if (dodge) {
                    beforeLeave()
                    leaveGame()
                    TimeUtils.setTimeout(this::joinGame, RandomUtils.randomIntInRange(4000, 6000))
                }
            }
        } else if (toggled()) {
            ChatUtils.error("Error getting stats! Check the log for more information.")
            println("Error getting stats! success == false")
            println(DuckDueller.gson.toJson(stats))
        }
    }

    private fun leaveGame() {
        TimeUtils.setTimeout(fun () {
            ChatUtils.sendAsPlayer("/l")
        }, RandomUtils.randomIntInRange(100, 300))
    }

    private fun joinGame() {
        TimeUtils.setTimeout(fun () {
            ChatUtils.sendAsPlayer(queueCommand)
        }, RandomUtils.randomIntInRange(100, 300))
    }

}