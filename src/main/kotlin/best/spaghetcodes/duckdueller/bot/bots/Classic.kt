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

    var tapping = false

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
                    if (distance < 2) {
                        Mouse.rClick(RandomUtils.randomIntInRange(80, 100)) // otherwise just blockhit
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
        if (combo >= 3) {
            Movement.clearLeftRight()
        }
    }

    override fun onTick() {
        var needJump = false
        if (mc.thePlayer != null) {
            if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) != Blocks.air && mc.thePlayer.onGround) {
                needJump = true
                Movement.singleJump(RandomUtils.randomIntInRange(150, 250))
            }
        }
        if (opponent() != null && mc.theWorld != null && mc.thePlayer != null) {
            if (!mc.thePlayer.isSprinting) {
                Movement.startSprinting()
            }

            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())

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
                    if (WorldUtils.blockInFront(mc.thePlayer, 2f, 0.5f) == Blocks.air) {
                        if (!EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!) && !needJump) {
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
                if (!needJump) {
                    Movement.stopJumping()
                }
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

            if ((distance in 5.5..6.0 || distance in 9.0..9.5) && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!)) {
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
                }
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            if ((EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!) && distance in 3.5f..30f) || (distance in 28.0..33.0 && !EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))) {
                if (distance > 5 && !Mouse.isUsingProjectile() && shotsFired < 5) {
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
                    if (opponent() != null && opponent()!!.heldItem != null && (opponent()!!.heldItem.unlocalizedName.lowercase().contains("bow") || opponent()!!.heldItem.unlocalizedName.lowercase().contains("rod"))) {
                        randomStrafe = true
                        if (distance < 15 && !needJump) {
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