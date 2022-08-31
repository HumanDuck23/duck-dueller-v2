package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.StateManager
import best.spaghetcodes.duckdueller.bot.player.Combat
import best.spaghetcodes.duckdueller.bot.player.Inventory
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.utils.*
import net.minecraft.init.Blocks
import net.minecraft.util.Vec3
import java.util.*
import kotlin.math.abs

class Boxing : BotBase("/play duels_boxing_duel") {

    override fun getName(): String {
        return "Boxing"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.boxing_duel_wins",
                "losses" to "player.stats.Duels.boxing_duel_losses",
                "ws" to "player.stats.Duels.current_boxing_winstreak",
            )
        )
    }

    private var tapping = false
    private var fishTimer: Timer? = null

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
        TimeUtils.setTimeout(this::fishFunc, RandomUtils.randomIntInRange(10000, 20000))
    }

    fun fishFunc(fish: Boolean = true) {
        if (StateManager.state == StateManager.States.PLAYING) {
            if (fish) {
                Inventory.setInvItem("fish")
            } else {
                Inventory.setInvItem("sword")
            }
            fishTimer = TimeUtils.setTimeout(fun () {
                fishFunc(!fish)
            }, RandomUtils.randomIntInRange(10000, 20000))
        }
    }

    override fun onGameEnd() {
        TimeUtils.setTimeout(fun () {
            Movement.clearAll()
            Mouse.stopLeftAC()
            Combat.stopRandomStrafe()
            fishTimer?.cancel()
        }, RandomUtils.randomIntInRange(100, 300))
    }

    override fun onAttack() {
        tapping = true
        ChatUtils.info("W-Tap")
        Combat.wTap(100)
        TimeUtils.setTimeout(fun () {
            tapping = false
        }, 100)
        if (combo >= 3) {
            Movement.clearLeftRight()
        }
    }

    override fun onTick() {
        if (mc.thePlayer != null) {
            if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) != Blocks.air && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(150, 250))
            }
        }
        if (opponent() != null && mc.theWorld != null && mc.thePlayer != null) {
            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())

            if (distance < (DuckDueller.config?.maxDistanceLook ?: 150)) {
                Mouse.startTracking()
            } else {
                Mouse.stopTracking()
            }

            if (combo < 3) {
                if (distance < (DuckDueller.config?.maxDistanceAttack ?: 10)) {
                    Mouse.startLeftAC()
                } else {
                    Mouse.stopLeftAC()
                }
            } else {
                if (distance < 3.5) {
                    Mouse.startLeftAC()
                } else {
                    Mouse.stopLeftAC()
                }
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (!EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
                if (distance in 15f..8f) {
                    randomStrafe = true
                } else {
                    if (combo < 3 && distance < 8) {
                        if (EntityUtils.entityMovingLeft(mc.thePlayer, opponent()!!)) {
                            movePriority[1] += 1
                        } else {
                            movePriority[0] += 1
                        }
                    } else {
                        clear = true
                    }
                }
            } else {
                // runner
                if (WorldUtils.leftOrRightToPoint(mc.thePlayer, Vec3(0.0, 0.0, 0.0))) {
                    movePriority[0] += 4
                } else {
                    movePriority[1] += 4
                }
            }

            if (clear) {
                Combat.stopRandomStrafe()
                Movement.clearLeftRight()
            } else {
                if (randomStrafe) {
                    Combat.startRandomStrafe(1000, 2000)
                } else {
                    Combat.stopRandomStrafe()
                    if (movePriority[0] > movePriority[1]) {
                        Movement.stopRight()
                        Movement.startLeft()
                    } else if (movePriority[1] > movePriority[0]) {
                        Movement.stopLeft()
                        Movement.startRight()
                    } else {
                        if (RandomUtils.randomBool()) {
                            Movement.startLeft()
                        } else {
                            Movement.startRight()
                        }
                    }
                }
            }
        }
    }

}