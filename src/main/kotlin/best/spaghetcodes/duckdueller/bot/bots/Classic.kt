package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.StateManager
import best.spaghetcodes.duckdueller.bot.player.Combat
import best.spaghetcodes.duckdueller.bot.player.Inventory
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.utils.EntityUtils
import best.spaghetcodes.duckdueller.utils.RandomUtils
import best.spaghetcodes.duckdueller.utils.TimeUtils
import best.spaghetcodes.duckdueller.utils.WorldUtils
import net.minecraft.init.Blocks

class Classic : BotBase("/play duels_classic_duel"){

    override fun getName(): String {
        return "Classic"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.classic_duel_wins",
                "losses" to "player.stats.Duels.classic_duel_losses",
                "ws" to "player.stats.Duels.current_classic_winstreak",
            )
        )
    }

    var shotsFired = 0

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
    }

    override fun onGameEnd() {
        shotsFired = 0
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
                val n = mc.thePlayer.heldItem.displayName.lowercase()
                if (n.contains("rod")) { // wait after hitting with the rod
                    Combat.wTap(300)
                    combo--
                } else if (n.contains("sword")) {
                    Mouse.rClick(RandomUtils.randomIntInRange(80, 100)) // otherwise just blockhit
                }
            }
        }
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

            if (distance > 8.8) {
                if (opponent() != null && opponent()!!.heldItem != null && opponent()!!.heldItem.displayName.lowercase().contains("bow")) {
                    if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) == Blocks.air) {
                        if (!EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
                            Movement.stopJumping()
                        } else {
                            Movement.startJumping()
                        }
                    } else {
                        Movement.startJumping()
                    }
                } else {
                    Movement.startJumping()
                }
            } else {
                if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) == Blocks.air) {
                    Movement.stopJumping()
                }
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance < 1 || (distance < 2.7 && combo >= 1)) {
                Movement.stopForward()
            } else {
                Movement.startForward()
            }

            if (distance < 1.5 && mc.thePlayer.heldItem != null && !mc.thePlayer.heldItem.displayName.lowercase().contains("sword")) {
                Inventory.setInvItem("sword")
                Mouse.rClickUp()
                Mouse.startLeftAC()
            }

            if ((distance in 7.0..7.5 || distance in 10.0..10.5) && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
                if (!Mouse.isUsingProjectile()) {
                    Mouse.stopLeftAC()
                    Mouse.setUsingProjectile(true)
                    TimeUtils.setTimeout(fun () {
                        Inventory.setInvItem("rod")
                        TimeUtils.setTimeout(fun () {
                            val r = RandomUtils.randomIntInRange(100, 200)
                            Mouse.rClick(r)
                            TimeUtils.setTimeout(fun () {
                                Mouse.setUsingProjectile(false)
                            }, r + RandomUtils.randomIntInRange(50, 100))
                            TimeUtils.setTimeout(fun () {
                                Inventory.setInvItem("sword")
                                TimeUtils.setTimeout(fun () {
                                    if (StateManager.state == StateManager.States.PLAYING) {
                                        Mouse.startLeftAC()
                                    }
                                }, RandomUtils.randomIntInRange(100, 150))
                            }, RandomUtils.randomIntInRange(500, 600))
                        }, RandomUtils.randomIntInRange(50, 90))
                    }, RandomUtils.randomIntInRange(10, 30))
                }
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            if (EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!) && distance in 3.5f..30f) {
                // bruh they running, that's cringe
                if (distance > 5 && !Mouse.isUsingProjectile() && shotsFired < 5) {
                    clear = true
                    Mouse.stopLeftAC()
                    Mouse.setUsingProjectile(true)
                    TimeUtils.setTimeout(fun () {
                        Inventory.setInvItem("bow")
                        TimeUtils.setTimeout(fun () {
                            val r = RandomUtils.randomIntInRange(1000, 1500)
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
                    if (EntityUtils.entityMovingLeft(mc.thePlayer, opponent()!!)) {
                        movePriority[0] += 1
                    } else {
                        movePriority[1] += 1
                    }
                }
            } else {
                if (distance in 15f..8f) {
                    randomStrafe = true
                } else {
                    randomStrafe = false
                    if (opponent() != null && opponent()!!.heldItem != null && (opponent()!!.heldItem.displayName.lowercase().contains("bow") || opponent()!!.heldItem.displayName.lowercase().contains("rod"))) {
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