package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.EntityUtils
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs

object Mouse {

    private var leftAC = false
    private var rClickDown = false

    private var tracking = false

    private var _usingProjectile = false

    private var leftClickDur = 0

    fun leftClick() {
        if (DuckDueller.bot?.toggled() == true && DuckDueller.mc.thePlayer != null && !DuckDueller.mc.thePlayer.isUsingItem) {
            DuckDueller.mc.thePlayer.swingItem()
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindAttack.keyCode, true)
            if (DuckDueller.mc.objectMouseOver != null && DuckDueller.mc.objectMouseOver.entityHit != null) {
                DuckDueller.mc.playerController.attackEntity(DuckDueller.mc.thePlayer, DuckDueller.mc.objectMouseOver.entityHit)
            }
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
                if (leftAC) {
                    val minDelay = 1000 / cps / 3
                    val maxDelay = minDelay * 2
                    val delay = RandomUtils.randomIntInRange(minDelay, maxDelay)
                    TimeUtils.setTimeout(this::leftClick, delay)
                }
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

    @SubscribeEvent
    fun onTick(ev: TickEvent.ClientTickEvent) {
        if (DuckDueller.mc.thePlayer != null && DuckDueller.bot?.toggled() == true && tracking && DuckDueller.bot?.opponent() != null) {
            if (leftClickDur > 0) {
                leftClickDur--
            } else {
                KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindAttack.keyCode, false)
            }

            val rotations = EntityUtils.getRotations(DuckDueller.mc.thePlayer, DuckDueller.bot?.opponent(), false)

            if (rotations != null) {
                val lookRand = (DuckDueller.config?.lookRand ?: 0).toDouble()
                var da = ((rotations[0] - DuckDueller.mc.thePlayer.rotationYaw) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()
                val maxRot = (DuckDueller.config?.lookSpeed ?: 10).toFloat()
                if (abs(da) > maxRot) {
                    da = if (da > 0) {
                        maxRot
                    } else {
                        -maxRot
                    }
                }

                DuckDueller.mc.thePlayer.rotationYaw += da
                DuckDueller.mc.thePlayer.rotationPitch = rotations[1] // pitch is perfect screw you
            }
        }
    }

}