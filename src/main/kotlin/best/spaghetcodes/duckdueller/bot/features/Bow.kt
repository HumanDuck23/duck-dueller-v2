package best.spaghetcodes.duckdueller.bot.features

import best.spaghetcodes.duckdueller.bot.StateManager
import best.spaghetcodes.duckdueller.bot.player.Inventory
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils

interface Bow {

    fun useBow(distance: Float, cb: () -> Unit) {
        Mouse.stopLeftAC()
        Mouse.setUsingProjectile(true)
        TimeUtils.setTimeout(fun () {
            Inventory.setInvItem("bow")
            TimeUtils.setTimeout(fun () {
                val r = when (distance) {
                    in 0f..7f -> RandomUtils.randomIntInRange(700, 900)
                    in 7f..15f -> RandomUtils.randomIntInRange(1000, 1200)
                    else -> RandomUtils.randomIntInRange(1300, 1500)
                }
                Mouse.rClick(r)
                TimeUtils.setTimeout(fun () {
                    Mouse.setUsingProjectile(false)
                    Inventory.setInvItem("sword")
                    TimeUtils.setTimeout(fun () {
                        if (StateManager.state == StateManager.States.PLAYING) {
                            Mouse.startLeftAC()
                            cb()
                        }
                    }, RandomUtils.randomIntInRange(100, 200))
                }, r + RandomUtils.randomIntInRange(100, 150))
            }, RandomUtils.randomIntInRange(100, 200))
        }, RandomUtils.randomIntInRange(50, 100))
    }

}