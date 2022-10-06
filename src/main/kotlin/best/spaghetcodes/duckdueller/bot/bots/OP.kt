package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.StateManager
import best.spaghetcodes.duckdueller.bot.features.*
import best.spaghetcodes.duckdueller.bot.player.Combat
import best.spaghetcodes.duckdueller.bot.player.Inventory
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.utils.*
import net.minecraft.init.Blocks
import net.minecraft.util.Vec3
import java.util.Random
import kotlin.math.floor

class OP : BotBase("/play duels_op_duel"), Bow, Rod, MovePriority, Potion, Gap {

    override fun getName(): String {
        return "OP"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.op_duel_wins",
                "losses" to "player.stats.Duels.op_duel_losses",
                "ws" to "player.stats.Duels.current_op_winstreak",
            )
        )
    }

    var shotsFired = 0
    var maxArrows = 20

    var speedDamage = 16386
    var regenDamage = 16385

    var speedPotsLeft = 2
    var regenPotsLeft = 2
    var gapsLeft = 6

    var lastSpeedUse = 0L
    var lastRegenUse = 0L
    override var lastPotion = 0L
    override var lastGap = 0L

    var tapping = false

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
        TimeUtils.setTimeout(Movement::startJumping, RandomUtils.randomIntInRange(400, 1200))
    }

    override fun onGameEnd() {
        shotsFired = 0

        speedPotsLeft = 2
        regenPotsLeft = 2
        gapsLeft = 6

        lastSpeedUse = 0L
        lastRegenUse = 0L
        lastPotion = 0L
        lastGap = 0L

        Mouse.stopLeftAC()
        val i = TimeUtils.setInterval(Mouse::stopLeftAC, 100, 100)
        TimeUtils.setTimeout(fun () {
            i?.cancel()
            Mouse.stopTracking()
            Movement.clearAll()
            Combat.stopRandomStrafe()
        }, RandomUtils.randomIntInRange(200, 400))
    }

    override fun onAttack() {
        val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())
        if (mc.thePlayer != null && mc.thePlayer.heldItem != null) {
            val n = mc.thePlayer.heldItem.unlocalizedName.lowercase()
            if (n.contains("rod")) { // wait after hitting with the rod
                Combat.wTap(300)
                tapping = true
                combo--
                TimeUtils.setTimeout(fun () {
                    tapping = false
                }, 300)
            } else if (n.contains("sword")) {
                if (distance < 2) {
                    Mouse.rClick(RandomUtils.randomIntInRange(60, 90)) // otherwise just blockhit
                } else {
                    Combat.wTap(100)
                    tapping = true
                    TimeUtils.setTimeout(fun () {
                        tapping = false
                    }, 100)
                }
            }
        }
    }

    override fun onTick() {
        if (opponent() != null && mc.theWorld != null && mc.thePlayer != null) {
            if (!mc.thePlayer.isSprinting) {
                Movement.startSprinting()
            }

            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())

            var hasSpeed = false
            for (effect in mc.thePlayer.activePotionEffects) {
                if (effect.effectName.lowercase().contains("speed")) {
                    hasSpeed = true
                }
            }

            if (distance < (DuckDueller.config?.maxDistanceLook ?: 150)) {
                Mouse.startTracking()
            } else {
                Mouse.stopTracking()
            }

            if (distance < (DuckDueller.config?.maxDistanceAttack ?: 10)) {
                if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword")) {
                    Mouse.startLeftAC()
                }
            } else {
                Mouse.stopLeftAC()
            }

            if (distance > 8.8) {
                if (opponent() != null && opponent()!!.heldItem != null && opponent()!!.heldItem.unlocalizedName.lowercase().contains("bow")) {
                    if (!Mouse.isRunningAway()) {
                        Movement.stopJumping()
                    }
                } else {
                    Movement.startJumping()
                }
            } else {
                Movement.stopJumping()
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance < 0.7 || (distance < 1.4 && combo >= 1)) {
                Movement.stopForward()
            } else {
                if (!tapping) {
                    Movement.startForward()
                }
            }

            if (distance < 1.5 && mc.thePlayer.heldItem != null && !mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword") && !Mouse.isUsingPotion()) {
                Inventory.setInvItem("sword")
                Mouse.rClickUp()
                Mouse.startLeftAC()
            }

            if (!hasSpeed && speedPotsLeft > 0 && System.currentTimeMillis() - lastSpeedUse > 15000 && System.currentTimeMillis() - lastPotion > 3500) {
                useSplashPotion(speedDamage, distance < 3.5, EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))
                speedPotsLeft--
                lastSpeedUse = System.currentTimeMillis()
            }

            if (WorldUtils.blockInFront(mc.thePlayer, 3f, 1.5f) != Blocks.air) {
                // wall
                Mouse.setRunningAway(false)
            }

            if (((distance > 3 && mc.thePlayer.health < 12) || mc.thePlayer.health < 9) && combo < 2 && mc.thePlayer.health <= opponent()!!.health) {
                // time to pot up
                if (!Mouse.isUsingProjectile() && !Mouse.isRunningAway() && !Mouse.isUsingPotion() && System.currentTimeMillis() - lastPotion > 3500) {
                    if (regenPotsLeft > 0 && System.currentTimeMillis() - lastRegenUse > 3500) {
                        useSplashPotion(regenDamage, distance < 2, EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))
                        regenPotsLeft--
                        lastRegenUse = System.currentTimeMillis()
                    } else {
                        if (regenPotsLeft == 0 && System.currentTimeMillis() - lastRegenUse > 4000) {
                            if (gapsLeft > 0 && System.currentTimeMillis() - lastGap > 4000) {
                                useGap(distance, distance < 2, EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))
                                gapsLeft--
                            }
                        }
                    }
                }
            }

            if (!Mouse.isUsingProjectile() && !Mouse.isRunningAway() && !Mouse.isUsingPotion() && !Mouse.rClickDown && System.currentTimeMillis() - lastGap > 2500) {
                if ((distance in 5.7..6.5 || distance in 9.0..9.5) && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
                    useRod()
                } else if ((EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!) && distance in 3.5f..30f) || (distance in 28.0..33.0 && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))) {
                    if (distance > 10 && shotsFired < maxArrows && System.currentTimeMillis() - lastPotion > 5000) {
                        clear = true
                        useBow(distance, fun () {
                            shotsFired++
                        })
                    } else {
                        clear = false
                        if (WorldUtils.leftOrRightToPoint(mc.thePlayer, Vec3(0.0, 0.0, 0.0))) {
                            movePriority[0] += 4
                        } else {
                            movePriority[1] += 4
                        }
                    }
                } else {
                    if (opponent()!!.isInvisibleToPlayer(mc.thePlayer)) {
                        clear = false
                        if (WorldUtils.leftOrRightToPoint(mc.thePlayer, Vec3(0.0, 0.0, 0.0))) {
                            movePriority[0] += 4
                        } else {
                            movePriority[1] += 4
                        }
                    } else {
                        if (EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
                            if (WorldUtils.leftOrRightToPoint(mc.thePlayer, Vec3(0.0, 0.0, 0.0))) {
                                movePriority[0] += 4
                            } else {
                                movePriority[1] += 4
                            }
                        } else {
                            if (distance in 15f..8f) {
                                randomStrafe = true
                            } else {
                                randomStrafe = false
                                if (opponent() != null && opponent()!!.heldItem != null && (opponent()!!.heldItem.unlocalizedName.lowercase().contains("bow") || opponent()!!.heldItem.unlocalizedName.lowercase().contains("rod"))) {
                                    randomStrafe = true
                                    if (distance < 15) {
                                        Movement.stopJumping()
                                    }
                                } else {
                                    if (distance < 8) {
                                        val swap = floor(combo.toDouble() / RandomUtils.randomIntInRange(3, 6).toDouble())
                                        /*if (EntityUtils.entityMovingLeft(mc.thePlayer, opponent()!!)) {
                                            if (swap % 2 == 0.0) {
                                                movePriority[1] += 1
                                            } else {
                                                movePriority[0] += 1
                                            }
                                        } else {
                                            if (swap % 2 == 0.0) {
                                                movePriority[0] += 1
                                            } else {
                                                movePriority[1] += 1
                                            }
                                        }*/
                                        val rotations = EntityUtils.getRotations(opponent()!!, mc.thePlayer, false)
                                        if (rotations != null) {
                                            if (rotations[0] < 0) {
                                                movePriority[1] += 5
                                            } else {
                                                movePriority[0] += 5
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (WorldUtils.blockInPath(mc.thePlayer, RandomUtils.randomIntInRange(3, 7), 1f) == Blocks.fire) {
                Movement.singleJump(RandomUtils.randomIntInRange(200, 400))
            }

            handle(clear, randomStrafe, movePriority)
        }
    }
}