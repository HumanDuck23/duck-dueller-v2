package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import net.minecraft.client.settings.KeyBinding

object Movement {
    private var forward = false
    private var backward = false
    private var left = false
    private var right = false
    private var jumping = false
    private var sprinting = false
    private var sneaking = false

    fun startForward() {
        if (DuckDueller.bot?.toggled() == true) { // need to do this because the type is Boolean? so it could be null
            forward = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindForward.keyCode, true)
        }
    }

    fun stopForward() {
        if (DuckDueller.bot?.toggled() == true) {
            forward = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindForward.keyCode, false)
        }
    }

    fun startBackward() {
        if (DuckDueller.bot?.toggled() == true) {
            backward = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindBack.keyCode, true)
        }
    }

    fun stopBackward() {
        if (DuckDueller.bot?.toggled() == true) {
            backward = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindBack.keyCode, false)
        }
    }

    fun startLeft() {
        if (DuckDueller.bot?.toggled() == true) {
            left = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindLeft.keyCode, true)
        }
    }

    fun stopLeft() {
        if (DuckDueller.bot?.toggled() == true) {
            left = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindLeft.keyCode, false)
        }
    }

    fun startRight() {
        if (DuckDueller.bot?.toggled() == true) {
            right = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindRight.keyCode, true)
        }
    }

    fun stopRight() {
        if (DuckDueller.bot?.toggled() == true) {
            right = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindRight.keyCode, false)
        }
    }

    fun startJumping() {
        if (DuckDueller.bot?.toggled() == true) {
            jumping = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindJump.keyCode, true)
        }
    }

    fun stopJumping() {
        if (DuckDueller.bot?.toggled() == true) {
            jumping = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindJump.keyCode, false)
        }
    }

    fun startSprinting() {
        if (DuckDueller.bot?.toggled() == true) {
            sprinting = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSprint.keyCode, true)
        }
    }

    fun stopSprinting() {
        if (DuckDueller.bot?.toggled() == true) {
            sprinting = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSprint.keyCode, false)
        }
    }

    fun startSneaking() {
        if (DuckDueller.bot?.toggled() == true) {
            sneaking = true
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSneak.keyCode, true)
        }
    }

    fun stopSneaking() {
        if (DuckDueller.bot?.toggled() == true) {
            sneaking = false
            KeyBinding.setKeyBindState(DuckDueller.mc.gameSettings.keyBindSneak.keyCode, false)
        }
    }

    fun singleJump(holdDuration: Int) {
        startJumping()
        TimeUtils.setTimeout(this::stopJumping, holdDuration)
    }

    fun clearAll() {
        stopForward()
        stopBackward()
        stopLeft()
        stopRight()
        stopJumping()
        stopSprinting()
        stopSneaking()
    }

    fun clearLeftRight() {
        stopLeft()
        stopRight()
    }

    fun swapLeftRight() {
        if (left) {
            stopLeft()
            startRight()
        } else if (right) {
            stopRight()
            startLeft()
        }
    }

    fun forward(): Boolean {
        return forward
    }

    fun backward(): Boolean {
        return backward
    }

    fun left(): Boolean {
        return left
    }

    fun right(): Boolean {
        return right
    }

    fun jumping(): Boolean {
        return jumping
    }

    fun sprinting(): Boolean {
        return sprinting
    }

    fun sneaking(): Boolean {
        return sneaking
    }

}