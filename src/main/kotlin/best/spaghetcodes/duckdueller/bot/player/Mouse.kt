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
    var rClickDown = false

    private var tracking = false

    private var _usingProjectile = false
    private var _usingPotion = false
    private var _runningAway = false

    private var leftClickDur = 0

    private var lastLeftClick = 0L

    private var runningRotations: FloatArray? = null

    private var splashAim = 0.0

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

    fun setUsingPotion(potion: Boolean) {
        _usingPotion = potion
        if (!_usingPotion) {
            splashAim = 0.0
        }
    }

    fun isUsingPotion(): Boolean {
        return _usingPotion
    }

    fun setRunningAway(runningAway: Boolean) {
        _runningAway = runningAway
        runningRotations = null // make sure to clear this, otherwise running away gets buggy asf
    }

    fun isRunningAway(): Boolean {
        return _runningAway
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
            if (_runningAway) {
                _usingProjectile = false
            }
            var rotations = EntityUtils.getRotations(DuckDueller.mc.thePlayer, DuckDueller.bot?.opponent(), false)

            if (rotations != null) {
                if (_runningAway) {
                    if (runningRotations == null) {
                        runningRotations = rotations
                        runningRotations!![0] += 180 + RandomUtils.randomDoubleInRange(-5.0, 5.0).toFloat()
                    }
                    rotations = runningRotations!!
                }

                if (_usingPotion) {
                    if (splashAim == 0.0) {
                        splashAim = RandomUtils.randomDoubleInRange(80.0, 90.0)
                    }
                    rotations[1] = splashAim.toFloat()
                }

                val lookRand = (DuckDueller.config?.lookRand ?: 0).toDouble()
                var dyaw = ((rotations[0] - DuckDueller.mc.thePlayer.rotationYaw) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()
                var dpitch = ((rotations[1] - DuckDueller.mc.thePlayer.rotationPitch) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()

                val factor = when (EntityUtils.getDistanceNoY(DuckDueller.mc.thePlayer, DuckDueller.bot?.opponent()!!)) {
                    in 0f..10f -> 1.0f
                    in 10f..20f -> 0.6f
                    in 20f..30f -> 0.4f
                    else -> 0.2f
                }

                val maxRotH = (DuckDueller.config?.lookSpeedHorizontal ?: 10).toFloat() * factor
                val maxRotV = (DuckDueller.config?.lookSpeedVertical ?: 5).toFloat() * factor

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