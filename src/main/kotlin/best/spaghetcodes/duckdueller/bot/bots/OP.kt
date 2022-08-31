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
import java.util.Random

class OP : BotBase("/play duels_op_duel") {

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
    var lastGapUse = 0L
    var lastPotUse = 0L

    var tapping = false

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
    }

    override fun onGameEnd() {
        shotsFired = 0

        speedPotsLeft = 2
        regenPotsLeft = 2
        gapsLeft = 6

        lastSpeedUse = 0L
        lastRegenUse = 0L
        lastGapUse = 0L

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
        if (distance < 3) {
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
                    Mouse.rClick(RandomUtils.randomIntInRange(80, 100)) // otherwise just blockhit
                }
            }
        } else {
            Combat.wTap(100)
            tapping = true
            TimeUtils.setTimeout(fun () {
                tapping = false
            }, 100)
        }
    }

    private fun pot(dmg: Int) {
        ChatUtils.info("POTTING! $dmg")
        fun _pot(dmg: Int) {
            if (Inventory.setInvItemByDamage(dmg)) {
                TimeUtils.setTimeout(fun () {
                    Mouse.setUsingPotion(true)

                    TimeUtils.setTimeout(fun () {
                        Mouse.rClick(RandomUtils.randomIntInRange(200, 250))
                        lastPotUse = System.currentTimeMillis()

                        TimeUtils.setTimeout(fun () {
                            Mouse.setUsingPotion(false)

                            TimeUtils.setTimeout(fun () {
                                Mouse.setRunningAway(false)
                                Inventory.setInvItem("sword")
                            }, RandomUtils.randomIntInRange(700, 1000))
                        }, RandomUtils.randomIntInRange(250, 290))
                    }, RandomUtils.randomIntInRange(200, 400))
                }, RandomUtils.randomIntInRange(200, 400))
            } else {
                println("Unable to use potion $dmg! Not found!")
            }
        }

        val dist = EntityUtils.getDistanceNoY(mc.thePlayer, opponent()!!)
        val run = dist < 8

        if (run) {
            Mouse.setUsingProjectile(false)
            Mouse.setRunningAway(true)
            TimeUtils.setTimeout(fun () {
                _pot(dmg)
            }, RandomUtils.randomIntInRange(300, 500))
        } else {
            _pot(dmg)
        }
    }

    private fun gap() {
        ChatUtils.info("GAPPING!")
        val dist = EntityUtils.getDistanceNoY(mc.thePlayer, opponent()!!)
        val time = when (dist) {
            in 0f..7f -> RandomUtils.randomIntInRange(2200, 2600)
            in 7f..15f -> RandomUtils.randomIntInRange(1700, 2200)
            else -> RandomUtils.randomIntInRange(1400, 1700)
        }
        if (Inventory.setInvItem("gold")) {
            Mouse.setUsingProjectile(false)
            Mouse.setRunningAway(true)

            TimeUtils.setTimeout(fun () {
                Mouse.rClick(RandomUtils.randomIntInRange(1800, 2100))

                TimeUtils.setTimeout(fun () {
                    lastGapUse = System.currentTimeMillis()
                    Inventory.setInvItem("sword")

                    TimeUtils.setTimeout(fun () {
                        Mouse.setRunningAway(false)
                    }, RandomUtils.randomIntInRange(1900, 2200))
                }, RandomUtils.randomIntInRange(1900, 2200))
            }, time)
        } else {
            println("Unable to gap! No gaps found!")
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
                    Movement.stopJumping()
                } else {
                    Movement.startJumping()
                }
            } else {
                Movement.stopJumping()
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance < 1 || (distance < 2.7 && combo >= 1)) {
                Movement.stopForward()
            } else {
                if (!tapping) {
                    Movement.startForward()
                }
            }

            if (distance < 1.5 && mc.thePlayer.heldItem != null && !mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword")) {
                Inventory.setInvItem("sword")
                Mouse.rClickUp()
                Mouse.startLeftAC()
            }

            if (!hasSpeed && speedPotsLeft > 0 && System.currentTimeMillis() - lastSpeedUse > 15000) {
                pot(speedDamage)
                speedPotsLeft--
                lastSpeedUse = System.currentTimeMillis()
            }

            if (WorldUtils.blockInFront(mc.thePlayer, 3f, 1.5f) != Blocks.air) {
                // wall
                Mouse.setRunningAway(false)
            }

            if (mc.thePlayer.health < 9 && combo < 3) {
                // time to pot up
                if (!Mouse.isUsingProjectile() && !Mouse.isRunningAway() && !Mouse.isUsingPotion()) {
                    if (regenPotsLeft > 0 && System.currentTimeMillis() - lastRegenUse > 7000) {
                        pot(regenDamage)
                        regenPotsLeft--
                        lastRegenUse = System.currentTimeMillis()
                    } else {
                        if (regenPotsLeft == 0 && System.currentTimeMillis() - lastRegenUse > 7000) {
                            if (gapsLeft > 0 && System.currentTimeMillis() - lastGapUse > 7000) {
                                gap()
                                gapsLeft--
                                lastGapUse = System.currentTimeMillis()
                            }
                        }
                    }
                }
            }

            if (!Mouse.isUsingProjectile() && !Mouse.isRunningAway() && !Mouse.isUsingPotion()) {
                if ((distance in 5.7..6.5 || distance in 9.0..9.5) && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
                    Mouse.stopLeftAC()
                    Mouse.setUsingProjectile(true)
                    TimeUtils.setTimeout(fun () {
                        Inventory.setInvItem("rod")
                        TimeUtils.setTimeout(fun () {
                            val r = RandomUtils.randomIntInRange(100, 200)
                            Mouse.rClick(r)
                            TimeUtils.setTimeout(fun () {
                                Mouse.setUsingProjectile(false)
                            }, RandomUtils.randomIntInRange(30, 70))
                            TimeUtils.setTimeout(fun () {
                                if (mc.thePlayer.heldItem != null && !mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("bow")) {
                                    Inventory.setInvItem("sword")
                                }
                                TimeUtils.setTimeout(fun () {
                                    if (StateManager.state == StateManager.States.PLAYING) {
                                        Mouse.startLeftAC()
                                    }
                                }, RandomUtils.randomIntInRange(100, 150))
                            }, r + RandomUtils.randomIntInRange(250, 400))
                        }, RandomUtils.randomIntInRange(50, 90))
                    }, RandomUtils.randomIntInRange(10, 30))
                } else if ((EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!) && distance in 3.5f..30f) || (distance in 28.0..33.0 && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))) {
                    if (distance > 10 && shotsFired < maxArrows && System.currentTimeMillis() - lastPotUse > 5000) {
                        clear = true
                        Mouse.stopLeftAC()
                        Mouse.setUsingProjectile(true)
                        TimeUtils.setTimeout(fun () {
                            Inventory.setInvItem("bow")
                            TimeUtils.setTimeout(fun () {
                                val r = when (distance) {
                                    in 0f..7f -> RandomUtils.randomIntInRange(700, 900)
                                    in 7f..15f -> RandomUtils.randomIntInRange(1000, 1200)
                                    else -> RandomUtils.randomIntInRange(1300, 1500)
                                }
                                Mouse.rClick(r)
                                TimeUtils.setTimeout(fun () {
                                    shotsFired++
                                    Mouse.setUsingProjectile(false)
                                    Inventory.setInvItem("sword")
                                    TimeUtils.setTimeout(fun () {
                                        if (StateManager.state == StateManager.States.PLAYING) {
                                            Mouse.startLeftAC()
                                        }
                                    }, RandomUtils.randomIntInRange(100, 200))
                                }, r + RandomUtils.randomIntInRange(100, 150))
                            }, RandomUtils.randomIntInRange(100, 200))
                        }, RandomUtils.randomIntInRange(50, 100))
                    } else {
                        clear = false
                        if (WorldUtils.leftOrRightToPoint(mc.thePlayer, Vec3(0.0, 0.0, 0.0))) {
                            movePriority[0] += 4
                        } else {
                            movePriority[1] += 4
                        }
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
                                if (combo < 2 && distance < 8) {
                                    if (EntityUtils.entityMovingLeft(mc.thePlayer, opponent()!!)) {
                                        movePriority[1] += 1
                                    } else {
                                        movePriority[0] += 1
                                    }
                                } else {
                                    clear = true
                                }
                            }
                        }
                    }
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