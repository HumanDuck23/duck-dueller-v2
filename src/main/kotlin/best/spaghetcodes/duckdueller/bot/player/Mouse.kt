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

    private var lastLeftClick = 0L

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

    fun startLeftAC() {
        if (DuckDueller.bot?.toggled() == true) {
            leftAC = true
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
            if (!DuckDueller.mc.thePlayer.isUsingItem) {
                val minCPS = DuckDueller.config?.minCPS ?: 10
                val maxCPS = DuckDueller.config?.maxCPS ?: 14

                if (System.currentTimeMillis() >= lastLeftClick + (1000 / RandomUtils.randomIntInRange(minCPS, maxCPS))) {
                    leftClick()
                    lastLeftClick = System.currentTimeMillis()
                }
            }
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
        if (DuckDueller.mc.thePlayer != null && DuckDueller.bot?.toggled() == true) {
            if (leftAC) {
                leftACFunc()
            }

            if (leftClickDur > 0) {
                leftClickDur--
            } else {
                KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindAttack.keyCode, false)
            }
        }
        if (DuckDueller.mc.thePlayer != null && DuckDueller.bot?.toggled() == true && tracking && DuckDueller.bot?.opponent() != null) {
            val rotations = EntityUtils.getRotations(DuckDueller.mc.thePlayer, DuckDueller.bot?.opponent(), false)

            if (rotations != null) {
                val lookRand = (DuckDueller.config?.lookRand ?: 0).toDouble()
                var dyaw = ((rotations[0] - DuckDueller.mc.thePlayer.rotationYaw) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()
                var dpitch = ((rotations[1] - DuckDueller.mc.thePlayer.rotationPitch) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()

                val maxRotH = (DuckDueller.config?.lookSpeedHorizontal ?: 10).toFloat()
                val maxRotV = (DuckDueller.config?.lookSpeedVertical ?: 5).toFloat()

                if (abs(dyaw) > maxRotH) {
                    dyaw = if (dyaw > 0) {
                        maxRotH
                    } else {
                        -maxRotH
                    }
                }

                if (abs(dpitch) > maxRotV) {
                    dpitch = if (dpitch > 0) {
                        maxRotV
                    } else {
                        -maxRotV
                    }
                }

                DuckDueller.mc.thePlayer.rotationYaw += dyaw
                DuckDueller.mc.thePlayer.rotationPitch += dpitch
            }
        }
    }

}