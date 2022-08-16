package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding

object Mouse {

    private var leftAC = false
    private var rClickDown = false

    private var tracking = false

    private var _usingProjectile = false

    fun leftClick() {
        if (DuckDueller.bot?.toggled() == true) {
            KeyBinding.onTick(DuckDueller.mc.gameSettings.keyBindAttack.keyCode)
        }
    }

    fun rClick(duration: Int) {
        if (DuckDueller.bot?.toggled() == true) {
            if (!rClickDown) {
                rClickDown()
                TimeUtils.setTimeout(this::rClickUp, duration)
            }
        }
    }

    fun startLeftAC(delay: Int = 0) {
        if (DuckDueller.bot?.toggled() == true) {
            if (!leftAC) {
                leftAC = true
                TimeUtils.setTimeout(this::leftACFunc, delay)
            }
        }
    }

    fun stopLeftAC() {
        // no need to check for toggled state here
        leftAC = false
    }

    fun startTracking() {
        tracking = true
    }

    fun stopTracking() {
        tracking = false
    }

    fun setUsingProjectile(proj: Boolean) {
        _usingProjectile = proj
    }

    fun isUsingProjectile(): Boolean {
        return _usingProjectile
    }

    private fun leftACFunc() {
        if (DuckDueller.bot?.toggled() == true && leftAC) {
            val minCPS = DuckDueller.config?.minCPS ?: 10
            val maxCPS = DuckDueller.config?.minCPS ?: 14

            val cps = RandomUtils.randomIntInRange(minCPS, maxCPS)

            // this could be the whole ac method, BUT the cps should jitter between min and max
            val cpsTimer = TimeUtils.setInterval(fun () {
                val minDelay = 1000 / cps / 3
                val maxDelay = minDelay * 2
                val delay = RandomUtils.randomIntInRange(minDelay, maxDelay)
                TimeUtils.setTimeout(this::leftClick, delay)
            }, 0, 1000/cps)

            TimeUtils.setTimeout(fun () {
                cpsTimer?.cancel()
                leftACFunc()
            }, RandomUtils.randomIntInRange(1500, 2000)) // run this method every 1.5-2 seconds
        }
    }

    private fun rClickDown() {
        if (DuckDueller.bot?.toggled() == true) {
            rClickDown = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindUseItem.keyCode, true)
        }
    }

    fun rClickUp() {
        if (DuckDueller.bot?.toggled() == true) {
            rClickDown = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindUseItem.keyCode, false)
        }
    }

}