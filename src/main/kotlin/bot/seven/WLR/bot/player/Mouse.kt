package bot.seven.WLR.bot.player

import bot.seven.WLR.wlr
import bot.seven.WLR.core.Config
import bot.seven.WLR.utils.EntityUtils
import bot.seven.WLR.utils.RandomUtils
import bot.seven.WLR.utils.TimeUtils
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
        if (wlr.bot?.toggled() == true && wlr.mc.thePlayer != null && !wlr.mc.thePlayer.isUsingItem) {
            wlr.mc.thePlayer.swingItem()
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindAttack.keyCode, true)
            if (wlr.mc.objectMouseOver != null && wlr.mc.objectMouseOver.entityHit != null) {
                wlr.mc.playerController.attackEntity(wlr.mc.thePlayer, wlr.mc.objectMouseOver.entityHit)
            }
        }
    }

    fun rClick(duration: Int) {
        if (wlr.bot?.toggled() == true) {
            if (!rClickDown) {
                rClickDown()
                TimeUtils.setTimeout(this::rClickUp, duration)
            }
        }
    }

    fun startLeftAC() {
        if (wlr.bot?.toggled() == true) {
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
        runningRotations = null
    }

    fun isRunningAway(): Boolean {
        return _runningAway
    }

    private fun leftACFunc() {
        if (wlr.bot?.toggled() == true && leftAC) {
            if (!wlr.mc.thePlayer.isUsingItem) {
                val minCPS = Config.minCPS
                val maxCPS = Config.maxCPS

                if (System.currentTimeMillis() >= lastLeftClick + (1000 / RandomUtils.randomIntInRange(minCPS, maxCPS))) {
                    leftClick()
                    lastLeftClick = System.currentTimeMillis()
                }
            }
        }
    }

    private fun rClickDown() {
        if (wlr.bot?.toggled() == true) {
            rClickDown = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindUseItem.keyCode, true)
        }
    }

    fun rClickUp() {
        rClickDown = false
        KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindUseItem.keyCode, false)
    }

    @SubscribeEvent
    fun onTick(ev: TickEvent.ClientTickEvent) {
        if (wlr.mc.thePlayer == null) return

        if (wlr.bot?.toggled() == true) {
            if (leftAC) {
                leftACFunc()
            }

            if (leftClickDur > 0) {
                leftClickDur--
            } else {
                if (wlr.mc.gameSettings.keyBindAttack.isKeyDown) {
                    KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindAttack.keyCode, false)
                }
            }
        } else {
            if (wlr.mc.gameSettings.keyBindAttack.isKeyDown) {
                KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindAttack.keyCode, false)
            }
        }

        val opponent = wlr.bot?.opponent()
        if (wlr.mc.thePlayer != null && wlr.bot?.toggled() == true && tracking && opponent != null) {
            if (_runningAway) {
                _usingProjectile = false
            }
            var rotations = EntityUtils.getRotations(wlr.mc.thePlayer, opponent, false)

            if (rotations != null) {
                if (_runningAway) {
                    if (runningRotations == null) {
                        runningRotations = rotations.copyOf()
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

                val lookRand = Config.lookRand.toDouble()
                var dyaw = ((rotations[0] - wlr.mc.thePlayer.rotationYaw) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()
                var dpitch = ((rotations[1] - wlr.mc.thePlayer.rotationPitch) + RandomUtils.randomDoubleInRange(-lookRand, lookRand)).toFloat()

                val distanceToOpponent = EntityUtils.getDistanceNoY(wlr.mc.thePlayer, opponent)
                val factor = when (distanceToOpponent) {
                    in 0f..10f -> 1.0f
                    in 10f..20f -> 0.6f
                    in 20f..30f -> 0.4f
                    else -> 0.2f
                }

                val maxRotH = Config.lookSpeedHorizontal.toFloat() * factor
                val maxRotV = Config.lookSpeedVertical.toFloat() * factor

                if (abs(dyaw) > maxRotH) {
                    dyaw = if (dyaw > 0) maxRotH else -maxRotH
                }

                if (abs(dpitch) > maxRotV) {
                    dpitch = if (dpitch > 0) maxRotV else -maxRotV
                }

                wlr.mc.thePlayer.rotationYaw += dyaw
                wlr.mc.thePlayer.rotationPitch += dpitch

                wlr.mc.thePlayer.rotationPitch = wlr.mc.thePlayer.rotationPitch.coerceIn(-90f, 90f)
            }
        }
    }

}