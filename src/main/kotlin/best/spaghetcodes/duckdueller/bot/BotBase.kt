package best.spaghetcodes.duckdueller.bot

import net.minecraft.client.Minecraft

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

}