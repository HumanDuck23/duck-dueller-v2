class Classic : BotBase("/play duels_classic_duel"), Bow, Rod, MovePriority {

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
    var maxArrows = 5

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
        TimeUtils.setTimeout(Movement::startJumping, RandomUtils.randomIntInRange(400, 1200))
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

        if (distance in 3.0..6.5) { // Ideal distance for using the rod
            if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("rod")) {
                useRod() // Use the rod to interrupt momentum
                TimeUtils.setTimeout(fun() {
                    if (distance < 3.5) {
                        executeCrit() // Perform a crit after using the rod
                    }
                }, RandomUtils.randomIntInRange(100, 200)) // Slight delay for better timing
            }
        } else if (distance < 3.0) { // Close range combat
            if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword")) {
                Mouse.rClick(RandomUtils.randomIntInRange(80, 100)) // Block-hit for defense
            }
        }

        if (combo >= 3) {
            Movement.clearLeftRight()
        }
    }

    fun executeCrit() {
        if (mc.thePlayer.onGround) {
            Movement.singleJump(RandomUtils.randomIntInRange(50, 100)) // Jump for crit
            TimeUtils.setTimeout(fun() {
                Mouse.startLeftAC() // Attack when falling
                TimeUtils.setTimeout(Mouse::stopLeftAC, RandomUtils.randomIntInRange(150, 200)) // Stop attacking to reset
            }, RandomUtils.randomIntInRange(50, 100))
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

            if (distance in 5.7..6.5 && !Mouse.isUsingProjectile()) {
                useRod() // Use the rod proactively when opponent is at a good distance
            }

            if (distance < 3.5 && combo < 3 && mc.thePlayer.onGround) {
                executeCrit() // Trigger crit if conditions are met
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

            if (distance > 5 && !Mouse.isUsingProjectile() && shotsFired < maxArrows) {
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

            handle(clear, randomStrafe, movePriority)
        }
    }

}
