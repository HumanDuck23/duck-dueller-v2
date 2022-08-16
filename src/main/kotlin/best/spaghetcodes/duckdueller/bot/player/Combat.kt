package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils

object Combat {

    private var randomStrafe = false
    private var randomStrafeMin = 0
    private var randomStrafeMax = 0

    fun wTap(duration: Int) {
        Movement.stopForward()
        TimeUtils.setTimeout(Movement::startForward, duration)
    }

    fun sTap(duration: Int) {
        Movement.startBackward()
        TimeUtils.setTimeout(Movement::stopBackward, duration)
    }

    fun aTap(duration: Int) {
        Movement.startLeft()
        TimeUtils.setTimeout(Movement::stopLeft, duration)
    }

    fun dTap(duration: Int) {
        Movement.startRight()
        TimeUtils.setTimeout(Movement::stopRight, duration)
    }

    fun shiftTap(duration: Int) {
        Movement.startSneaking()
        TimeUtils.setTimeout(Movement::stopSneaking, duration)
    }

    fun spamA(hold: Int, duration: Int) {
        val spamTimer = TimeUtils.setInterval(fun () { aTap(hold) }, 0, hold * 2 + RandomUtils.randomIntInRange(0, hold/5))
        TimeUtils.setTimeout(fun () { spamTimer?.cancel() }, duration)
    }

    fun spamD(hold: Int, duration: Int) {
        val spamTimer = TimeUtils.setInterval(fun () { dTap(hold) }, 0, hold * 2 + RandomUtils.randomIntInRange(0, hold/5))
        TimeUtils.setTimeout(fun () { spamTimer?.cancel() }, duration)
    }

    fun startRandomStrafe(min: Int, max: Int) {
        randomStrafeMin = min
        randomStrafeMax = max
        if (!randomStrafe) {
            randomStrafe = true
            randomStrafeFunc()
        }
    }

    fun stopRandomStrafe() {
        if (randomStrafe) {
            randomStrafe = false
            Movement.clearLeftRight()
        }
    }

    private fun randomStrafeFunc() {
        if (randomStrafe) {
            if (!(Movement.left() || Movement.right())) {
                if (RandomUtils.randomBool()) {
                    Movement.startLeft()
                } else {
                    Movement.startRight()
                }
            } else {
                Movement.swapLeftRight()
            }
            TimeUtils.setTimeout(this::randomStrafeFunc, RandomUtils.randomIntInRange(randomStrafeMin, randomStrafeMax))
        }
    }

}