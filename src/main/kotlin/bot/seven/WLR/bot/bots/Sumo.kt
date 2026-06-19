package bot.seven.WLR.bot.bots

import bot.seven.WLR.bot.BotBase
import bot.seven.WLR.bot.StateManager
import bot.seven.WLR.bot.player.*
import bot.seven.WLR.core.Config
import bot.seven.WLR.utils.ChatUtils
import bot.seven.WLR.utils.EntityUtils
import bot.seven.WLR.utils.RandomUtils
import bot.seven.WLR.utils.TimeUtils
import bot.seven.WLR.utils.WorldUtils
import kotlin.math.abs

class Sumo : BotBase("/play duels_sumo_duel") {

    override fun getName(): String {
        return "Sumo"
    }

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
    private var opponentOffEdge = false
    private var tap50 = false
    private var canDistanceJump = true
    private var isHitselecting = false
    private var hitselectCooldownUntil = 0L

    private val maxAttackDistanceConfigurable: Int
        get() = Config.maxDistanceAttack

    override fun onJoinGame() {
        LobbyMovement.stop()
        if (Config.enableCustomCamera && mc.thePlayer != null) {
            Camera.enable()
        }
    }

    override fun beforeStart() {
        LobbyMovement.stop()
        canDistanceJump = true
    }

    override fun beforeLeave() {
        LobbyMovement.stop()
    }

    override fun onGameStart() {
        LobbyMovement.stop()
        Movement.startSprinting()
        Movement.startForward()
        Movement.clearLeftRight()
        Combat.stopRandomStrafe()
        canDistanceJump = true
        tapping = false
        opponentOffEdge = false
        tap50 = false

        isHitselecting = false
        hitselectCooldownUntil = 0L

        if (Config.enableCustomCamera && mc.thePlayer != null) {
            Camera.enable()
        }
    }

    override fun onGameEnd() {
        Camera.disable()
        isHitselecting = false
        TimeUtils.setTimeout(fun () {
            Mouse.stopLeftAC()
            Combat.stopRandomStrafe()
            Mouse.stopTracking()
            Movement.startSprinting()
            Movement.startForward()
            Movement.startJumping()
        }, RandomUtils.randomIntInRange(100, 300))
    }

    override fun onAttack() {
        if (!tapping && !isHitselecting && StateManager.state == StateManager.States.PLAYING && mc.thePlayer != null) {
            tapping = true
            val dur = if (tap50) 50 else 100
            Combat.wTap(dur)
            tap50 = !tap50
            TimeUtils.setTimeout(fun() { tapping = false }, (dur.toLong() + 15).toInt())
        }
    }

    override fun onFoundOpponent() {
        if (StateManager.state == StateManager.States.PLAYING && mc.thePlayer != null && opponent() != null) {
            Mouse.startTracking()
        }
    }

    fun leftEdge(distance: Float): Boolean { return mc.thePlayer?.let { p -> WorldUtils.airOnLeft(p, distance) } ?: false }
    fun rightEdge(distance: Float): Boolean { return mc.thePlayer?.let { p -> WorldUtils.airOnRight(p, distance) } ?: false }
    fun nearEdge(distance: Float): Boolean { return mc.thePlayer?.let { p -> WorldUtils.airOnRight(p, distance) || WorldUtils.airOnLeft(p, distance) || WorldUtils.airInBack(p, distance) } ?: false }
    fun opponentNearEdge(distance: Float): Boolean { return opponent()?.let { opp -> WorldUtils.airInBack(opp, distance) || WorldUtils.airOnLeft(opp, distance) || WorldUtils.airOnRight(opp, distance) } ?: false }

    override fun onTick() {
        val player = mc.thePlayer
        val currentOpponent = opponent()

        if (StateManager.state == StateManager.States.GAME && player != null) {
            if (Config.lobbyMovement) {
                LobbyMovement.startMovement(Config.selectedLobbyMovementType)
            } else {
                LobbyMovement.stop()
                Movement.clearAll()
            }
            Mouse.stopTracking()
            Mouse.stopLeftAC()
            Combat.stopRandomStrafe()
            if (isHitselecting) isHitselecting = false
            return
        }

        if (player == null || currentOpponent == null || StateManager.state != StateManager.States.PLAYING) {
            Combat.stopRandomStrafe()
            Mouse.stopTracking()
            Mouse.stopLeftAC()

            if (isHitselecting) {
                isHitselecting = false
                if (Config.hitselectStopSprintDuringBait || Config.hitselectSTapDuringBait) {
                    Movement.startSprinting()
                }
            }

            if (!Config.lobbyMovement && StateManager.state != StateManager.States.PLAYING) {
                if (Movement.forward() || Movement.backward() || Movement.left() || Movement.right()) {
                    Movement.clearAll()
                }
            }
            if (currentOpponent == null) {
                opponentOffEdge = false
            }
            return
        }

        val isCurrentOpponentActuallyOffEdge = WorldUtils.entityOffEdge(currentOpponent)
        opponentOffEdge = isCurrentOpponentActuallyOffEdge || (opponentOffEdge && EntityUtils.getDistanceNoY(player, currentOpponent) > 17)

        if (!opponentOffEdge) {
            val distance = EntityUtils.getDistanceNoY(player, currentOpponent)

            if (Config.enableHitselecting && !isHitselecting && !tapping && player.onGround &&
                System.currentTimeMillis() >= hitselectCooldownUntil &&
                distance >= Config.hitselectMinActivationDistance && distance <= Config.hitselectMaxActivationDistance &&
                RandomUtils.randomDoubleInRange(0.0, 1.0) < Config.hitselectChance &&
                !WorldUtils.airInFront(player, 2.0f)
            ) {
                isHitselecting = true
                ChatUtils.info("Hitselect")

                Mouse.stopLeftAC()
                Combat.stopRandomStrafe()
                Movement.clearLeftRight()

                val originalForwardState = Movement.forward()

                if (Config.hitselectSTapDuringBait) {
                    Movement.stopForward()
                    Movement.startBackward()
                    TimeUtils.setTimeout(fun() {
                        Movement.stopBackward()
                        if (!isHitselecting) return@setTimeout
                        if (originalForwardState && !WorldUtils.airInFront(player, 1.75f)) Movement.startForward()
                    }, Config.hitselectSTapDuration)
                }

                if (Config.hitselectStopSprintDuringBait && player.isSprinting) {
                    Movement.stopSprinting()
                }

                val baitDuration = RandomUtils.randomIntInRange(Config.hitselectBaitDurationMin, Config.hitselectBaitDurationMax)
                TimeUtils.setTimeout(fun() {
                    if (!isHitselecting) return@setTimeout

                    isHitselecting = false
                    hitselectCooldownUntil = System.currentTimeMillis() + Config.hitselectCooldown

                    if ((Config.hitselectStopSprintDuringBait || Config.hitselectSTapDuringBait) && !player.isSprinting) {
                        Movement.startSprinting()
                    }
                }, baitDuration)
            }

            if (!isHitselecting) {
                if (!player.isSprinting && !WorldUtils.airInFront(player, 1.0f)) {
                    Movement.startSprinting()
                }
                Mouse.startTracking()

                if (distance > maxAttackDistanceConfigurable || WorldUtils.airInFront(player, 0.8f)) {
                    Mouse.stopLeftAC()
                } else {
                    Mouse.startLeftAC()
                }

                var performingJump = false

                if (Config.enableSumoDistanceJump) {
                    val jumpDistanceThreshold = RandomUtils.randomDoubleInRange(5.5, 7.0)
                    if (canDistanceJump && distance >= jumpDistanceThreshold && player.onGround &&
                        !WorldUtils.airInFront(player, 3f) && !tapping
                    ) {
                        Movement.clearLeftRight(); Combat.stopRandomStrafe(); Movement.startForward()
                        Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
                        canDistanceJump = false
                        TimeUtils.setTimeout(fun() { canDistanceJump = true }, RandomUtils.randomIntInRange(500, 1000))
                        performingJump = true
                    }
                }

                if (!performingJump && combo >= 3 && distance >= 3.2 &&
                    distance < (RandomUtils.randomDoubleInRange(5.5, 7.0) - 0.5) && player.onGround &&
                    !nearEdge(4f) && !WorldUtils.airInFront(player, 3f) && !tapping
                ) {
                    Movement.clearLeftRight(); Combat.stopRandomStrafe()
                    Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
                    performingJump = true
                }

                if (!performingJump) {
                    var clearStrafingInputs = false
                    var engageRandomStrafe = false

                    if (Config.enableSumoStrafing) {
                        val strafeConfig = when (Config.sumoStrafeIntensity) {
                            Config.SumoStrafeIntensity.LIGHT -> Triple(4.0, 2.0, 2)
                            Config.SumoStrafeIntensity.MEDIUM -> Triple(3.2, 1.25, 3)
                            Config.SumoStrafeIntensity.HARD -> Triple(2.6, 0.5, 5)
                        }
                        val strafeMinDistanceOpponent: Double = strafeConfig.first
                        val strafeEdgeDifferenceThreshold: Double = strafeConfig.second
                        val minComboToClearStrafe: Int = strafeConfig.third

                        val randomStrafeMinDur: Int = when (Config.sumoStrafeIntensity) {
                            Config.SumoStrafeIntensity.LIGHT -> 1200
                            Config.SumoStrafeIntensity.MEDIUM -> 800
                            Config.SumoStrafeIntensity.HARD -> 400
                        }
                        val randomStrafeMaxDur: Int = when (Config.sumoStrafeIntensity) {
                            Config.SumoStrafeIntensity.LIGHT -> 1800
                            Config.SumoStrafeIntensity.MEDIUM -> 1300
                            Config.SumoStrafeIntensity.HARD -> 900
                        }

                        if (distance <= strafeMinDistanceOpponent || combo >= minComboToClearStrafe) {
                            clearStrafingInputs = true
                        }

                        if (!clearStrafingInputs && !tapping) {
                            val le = WorldUtils.distanceToLeftEdge(player)
                            val re = WorldUtils.distanceToRightEdge(player)
                            val diff = abs(le - re)
                            if (diff > strafeEdgeDifferenceThreshold) {
                                if (le < re) { Movement.stopLeft(); Movement.startRight(); Combat.stopRandomStrafe() }
                                else if (re < le) { Movement.stopRight(); Movement.startLeft(); Combat.stopRandomStrafe() }
                                else engageRandomStrafe = true
                            } else engageRandomStrafe = true
                        }

                        if (clearStrafingInputs || tapping) {
                            Combat.stopRandomStrafe(); Movement.clearLeftRight()
                        } else if (engageRandomStrafe) {
                            Movement.clearLeftRight(); Combat.startRandomStrafe(randomStrafeMinDur, randomStrafeMaxDur)
                        }
                    } else {
                        Combat.stopRandomStrafe(); Movement.clearLeftRight()
                    }

                    if (!tapping) {
                        if (distance < 1.2 || WorldUtils.airInFront(player, 1.75f)) {
                            Movement.stopForward()
                        } else {
                            Movement.startForward()
                        }
                    }

                    if (WorldUtils.airInFront(player, 1.75f) && player.onGround) {
                        Movement.startSneaking(); Movement.stopForward(); Movement.clearLeftRight(); Combat.stopRandomStrafe()
                    } else {
                        Movement.stopSneaking()
                    }

                    if (WorldUtils.airInBack(player, 2.0f) && player.onGround) {
                        Movement.clearLeftRight(); Combat.stopRandomStrafe()
                        if (!tapping) Movement.startForward()
                    }

                    if (Movement.left() && WorldUtils.airOnLeft(player, 1.5f) && player.onGround) Movement.stopLeft()
                    if (Movement.right() && WorldUtils.airOnRight(player, 1.5f) && player.onGround) Movement.stopRight()
                }
            } else {
                Mouse.startTracking()
            }
        } else {
            Mouse.stopLeftAC()
            Combat.stopRandomStrafe()
            Mouse.stopTracking()

            if (isHitselecting) {
                isHitselecting = false
                if(Config.hitselectStopSprintDuringBait || Config.hitselectSTapDuringBait) {
                    Movement.startSprinting()
                }
                Movement.startForward()
            }

            if (StateManager.state == StateManager.States.PLAYING && opponentOffEdge) {
                Movement.clearAll()
            }
        }
    }
}