package bot.seven.WLR.bot.features

import bot.seven.WLR.bot.player.Combat
import bot.seven.WLR.bot.player.Movement
import bot.seven.WLR.utils.RandomUtils

interface MovePriority {

    fun handle(clear: Boolean, randomStrafe: Boolean, movePriority: ArrayList<Int>) {
        if (clear) {
            Combat.stopRandomStrafe()
            Movement.clearLeftRight()
        } else {
            if (randomStrafe) {
                Combat.startRandomStrafe(1000, 2000)
            } else {
                Combat.stopRandomStrafe()
                if (movePriority[0] > movePriority[1]) {
                    Movement.stopRight()
                    Movement.startLeft()
                } else if (movePriority[1] > movePriority[0]) {
                    Movement.stopLeft()
                    Movement.startRight()
                } else {
                    if (RandomUtils.randomBool()) {
                        Movement.startLeft()
                    } else {
                        Movement.startRight()
                    }
                }
            }
        }
    }

}