package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.bot.BotBase

class OP : BotBase("/play duels_op_duel") {

    override fun getName(): String {
        return "OP"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.op_duel_wins",
                "losses" to "player.stats.Duels.op_duel_losses",
                "ws" to "player.stats.Duels.current_op_winstreak",
            )
        )
    }

}