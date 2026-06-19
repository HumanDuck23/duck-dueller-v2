package bot.seven.WLR.bot.features

import bot.seven.WLR.bot.player.Inventory
import bot.seven.WLR.bot.player.Mouse
import bot.seven.WLR.bot.player.Movement
import bot.seven.WLR.utils.ChatUtils
import bot.seven.WLR.utils.RandomUtils
import bot.seven.WLR.utils.TimeUtils

interface Gap {

    var lastGap: Long

    fun useGap(distance: Float, run: Boolean, facingAway: Boolean) {
        lastGap = System.currentTimeMillis()
        fun gap() {
            Mouse.stopLeftAC()
            if (Inventory.setInvItem("gold")) {
                ChatUtils.info("About to gap")
                val r = RandomUtils.randomIntInRange(2100, 2200)
                Mouse.rClick(r)

                TimeUtils.setTimeout(fun () {
                    Inventory.setInvItem("sword")

                    TimeUtils.setTimeout(fun () {
                        Mouse.setRunningAway(false)
                    }, RandomUtils.randomIntInRange(200, 400))
                }, r + RandomUtils.randomIntInRange(200, 300))
            }
        }

        val time = when (distance) {
            in 0f..7f -> RandomUtils.randomIntInRange(2200, 2600)
            in 7f..15f -> RandomUtils.randomIntInRange(1700, 2200)
            else -> RandomUtils.randomIntInRange(1400, 1700)
        }
        if (run && !facingAway) {
            Mouse.setUsingProjectile(false)
            Mouse.setRunningAway(true)
            Movement.startJumping()

            TimeUtils.setTimeout(fun () { gap() }, time)
        } else {
            gap()
        }
    }

}