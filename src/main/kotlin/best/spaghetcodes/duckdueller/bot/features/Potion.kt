package best.spaghetcodes.duckdueller.bot.features

import best.spaghetcodes.duckdueller.bot.player.Inventory
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.utils.ChatUtils
import best.spaghetcodes.duckdueller.utils.EntityUtils
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils

interface Potion {

    var lastPotion: Long

    fun useSplashPotion(damage: Int, run: Boolean, facingAway: Boolean) {
        fun pot(dmg: Int) {
            if (Inventory.setInvItemByDamage(dmg)) {
                ChatUtils.info("About to splash $dmg")
                lastPotion = System.currentTimeMillis()
                TimeUtils.setTimeout(fun() {
                    Mouse.setUsingPotion(true)

                    TimeUtils.setTimeout(fun () {
                        val r = RandomUtils.randomIntInRange(80, 120)
                        Mouse.rClick(r)

                        TimeUtils.setTimeout(fun () {
                            Mouse.setUsingPotion(false)
                            TimeUtils.setTimeout(fun() {
                                Inventory.setInvItem("sword")

                                TimeUtils.setTimeout(fun() {
                                    Mouse.setRunningAway(false)
                                }, RandomUtils.randomIntInRange(500, 700))
                            }, RandomUtils.randomIntInRange(80, 120))
                        }, r + RandomUtils.randomIntInRange(80, 120))
                    }, RandomUtils.randomIntInRange(100, 200))
                }, RandomUtils.randomIntInRange(50, 100))
            }
        }

        if (run && !facingAway) {
            Mouse.setUsingProjectile(false)
            Mouse.setRunningAway(true)
            TimeUtils.setTimeout(fun() {
                pot(damage)
            }, RandomUtils.randomIntInRange(300, 500))
        } else {
            pot(damage)
        }
    }

}