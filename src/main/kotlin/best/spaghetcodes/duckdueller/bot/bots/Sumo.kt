package best.spaghetcodes.duckdueller.bot.bots

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.BotBase
import best.spaghetcodes.duckdueller.bot.player.Combat
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.bot.player.Movement
import best.spaghetcodes.duckdueller.utils.*
import kotlin.math.abs

class Sumo : BotBase("/play duels_sumo_duel") {

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.sumo_duel_wins",
                "losses" to "player.stats.Duels.sumo_duel_losses",
                "ws" to "player.stats.Duels.current_sumo_winstreak",
            )
        )
    }

    private var tapping = false

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
    }

    override fun onGameEnd() {
        TimeUtils.setTimeout(fun () {
            Movement.clearAll()
            Mouse.stopLeftAC()
            Combat.stopRandomStrafe()
        }, RandomUtils.randomIntInRange(100, 300))
    }

    override fun onAttack() {
        tapping = true
        ChatUtils.info("W-Tap")
        Combat.wTap(80)
        TimeUtils.setTimeout(fun () {
            tapping = false
        }, 80)
        Movement.clearLeftRight()
    }

    override fun onFoundOpponent() {
        Mouse.startTracking()
    }

    fun leftEdge(distance: Float): Boolean {
        return (WorldUtils.airOnLeft(mc.thePlayer, distance))
    }

    fun rightEdge(distance: Float): Boolean {
        return (WorldUtils.airOnRight(mc.thePlayer, distance))
    }

    fun nearEdge(distance: Float): Boolean { // doesnt check front
        return (rightEdge(distance) || leftEdge(distance) || WorldUtils.airInBack(mc.thePlayer, distance))
    }

    fun opponentNearEdge(distance: Float): Boolean {
        return (WorldUtils.airInBack(opponent()!!, distance) || WorldUtils.airOnLeft(opponent()!!, distance) || WorldUtils.airOnRight(
            opponent()!!, distance))
    }

    override fun onTick() {
        if (mc.thePlayer != null && opponent() != null) {
            if (!mc.thePlayer.isSprinting) {
                Movement.startSprinting()
            }

            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())

            if (distance > (DuckDueller.config?.maxDistanceAttack ?: 5)) {
                Mouse.stopLeftAC()
            } else {
                Mouse.startLeftAC()
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance > 2) {
                if (nearEdge(2f)) {
                    if (EntityUtils.entityMovingRight(mc.thePlayer, opponent()!!)) {
                        movePriority[1] += 2
                    } else if (EntityUtils.entityMovingLeft(mc.thePlayer, opponent()!!)) {
                        movePriority[0] += 2
                    }
                } else {
                    if (opponentNearEdge(2f)) {
                        if (opponent()?.onGround == true) {
                            if (EntityUtils.entityMovingRight(mc.thePlayer, opponent()!!)) {
                                movePriority[0] += 2
                            } else if (EntityUtils.entityMovingLeft(mc.thePlayer, opponent()!!)) {
                                movePriority[1] += 2
                            } else {
                                clear = true
                            }
                        }
                    } else {
                        val le = WorldUtils.distanceToLeftEdge(mc.thePlayer)
                        val re = WorldUtils.distanceToRightEdge(mc.thePlayer)
                        val diff = abs(abs(le) - abs(re))
                        if (diff > 2) {
                            if (le < re) {
                                movePriority[1] += 5
                            } else if (re < le) {
                                movePriority[0] += 5
                            } else {
                                randomStrafe = true
                            }
                        } else {
                            randomStrafe = true
                        }
                    }
                }
            } else {
                clear = true
            }

            // placing this here so that it only strafes in a combo if it's REALLY close to an edge
            if (combo >= 2) {
                clear = true
            }

            // a bunch of if's to detect edges and avoid them instead of just not walking off

            if (
                (WorldUtils.airCheckAngle(mc.thePlayer, 5f, 20f, 60f)
                        || WorldUtils.airCheckAngle(mc.thePlayer, 4.5f, 70f, 110f)
                        || WorldUtils.airCheckAngle(mc.thePlayer, 5f, 120f, 160f))
                && combo <= 3
            ) {
                movePriority[1] += 5
                clear = false
            }

            if (
                (WorldUtils.airCheckAngle(mc.thePlayer, 5f, -20f, -60f)
                        || WorldUtils.airCheckAngle(mc.thePlayer, 4.5f, -70f, -110f)
                        || WorldUtils.airCheckAngle(mc.thePlayer, 5f, -120f, -160f))
                && combo <= 3
            ) {
                movePriority[0] += 5
                clear = false
            }

            if (distance < 2) {
                clear = true
            }

            if (rightEdge(4f)) {
                movePriority[0] += 10
                clear = false
            }
            if (leftEdge(4f)) {
                movePriority[1] += 10
                clear = false
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround && !nearEdge(5f) && !WorldUtils.airInFront(mc.thePlayer, 3f)) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            if (clear) {
                Combat.stopRandomStrafe()
                Movement.clearLeftRight()
            } else {
                if (randomStrafe) {
                    Combat.startRandomStrafe(900, 1400)
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

            if (distance < 1.2 || (distance < 2 && combo > 2)) {
                Movement.stopForward()
            } else {
                if (!tapping) {
                    Movement.startForward()
                }
            }

            // don't walk off an edge
            if (WorldUtils.airInFront(mc.thePlayer, 2f) && mc.thePlayer.onGround) {
                Movement.startSneaking()
            } else {
                Movement.stopSneaking()
            }
            if (WorldUtils.airInBack(mc.thePlayer, 2.5f) && mc.thePlayer.onGround) {
                Movement.startForward()
                Movement.clearLeftRight()
            }
        }
    }

}