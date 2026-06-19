package bot.seven.WLR.bot.player

import bot.seven.WLR.utils.TimeUtils
import bot.seven.WLR.wlr
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
        if (wlr.bot?.toggled() == true) { // need to do this because the type is Boolean? so it could be null
            forward = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindForward.keyCode, true)
        }
    }

    fun stopForward() {
        if (wlr.bot?.toggled() == true) {
            forward = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindForward.keyCode, false)
        }
    }

    fun startBackward() {
        if (wlr.bot?.toggled() == true) {
            backward = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindBack.keyCode, true)
        }
    }

    fun stopBackward() {
        if (wlr.bot?.toggled() == true) {
            backward = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindBack.keyCode, false)
        }
    }

    fun startLeft() {
        if (wlr.bot?.toggled() == true) {
            left = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindLeft.keyCode, true)
        }
    }

    fun stopLeft() {
        if (wlr.bot?.toggled() == true) {
            left = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindLeft.keyCode, false)
        }
    }

    fun startRight() {
        if (wlr.bot?.toggled() == true) {
            right = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindRight.keyCode, true)
        }
    }

    fun stopRight() {
        if (wlr.bot?.toggled() == true) {
            right = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindRight.keyCode, false)
        }
    }

    fun startJumping() {
        if (wlr.bot?.toggled() == true) {
            jumping = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindJump.keyCode, true)
        }
    }

    fun stopJumping() {
        if (wlr.bot?.toggled() == true) {
            jumping = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindJump.keyCode, false)
        }
    }

    fun startSprinting() {
        if (wlr.bot?.toggled() == true) {
            sprinting = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindSprint.keyCode, true)
        }
    }

    fun stopSprinting() {
        if (wlr.bot?.toggled() == true) {
            sprinting = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindSprint.keyCode, false)
        }
    }

    fun startSneaking() {
        if (wlr.bot?.toggled() == true) {
            sneaking = true
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindSneak.keyCode, true)
        }
    }

    fun stopSneaking() {
        if (wlr.bot?.toggled() == true) {
            sneaking = false
            KeyBinding.setKeyBindState(wlr.mc.gameSettings.keyBindSneak.keyCode, false)
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