package best.spaghetcodes.duckdueller.bot

import best.spaghetcodes.duckdueller.events.packet.PacketEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
            if (ev.getPacket() is S19PacketEntityStatus) { // use the status packet for attack events
                val packet = ev.getPacket() as S19PacketEntityStatus
                if (packet.opCode.toInt() == 2) { // damage
                    val entity = packet.getEntity(mc.theWorld)
                    if (entity != null) {
                        if (entity.entityId == attackedID) {
                            attackedID = -1
                            onAttack()
                        } else if (mc.thePlayer != null && entity.entityId == mc.thePlayer.entityId) {
                            onAttacked()
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

}