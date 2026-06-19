package bot.seven.WLR.bot.bots

import bot.seven.WLR.bot.BotBase
import bot.seven.WLR.bot.features.Gap
import bot.seven.WLR.bot.features.MovePriority
import bot.seven.WLR.bot.features.Potion
import bot.seven.WLR.bot.player.Combat
import bot.seven.WLR.bot.player.Inventory
import bot.seven.WLR.bot.player.Mouse
import bot.seven.WLR.bot.player.Movement
import bot.seven.WLR.core.Config
import bot.seven.WLR.utils.EntityUtils
import bot.seven.WLR.utils.RandomUtils
import bot.seven.WLR.utils.TimeUtils
import bot.seven.WLR.utils.WorldUtils
import net.minecraft.init.Blocks
import net.minecraft.util.Vec3

class Combo : BotBase("/play duels_combo_duel"), MovePriority, Gap, Potion {

    override fun getName(): String {
        return "Combo"
    }

    init {
        setStatKeys(
            mapOf(
                "wins" to "player.stats.Duels.combo_duel_wins",
                "losses" to "player.stats.Duels.combo_duel_losses",
                "ws" to "player.stats.Duels.current_combo_winstreak",
            )
        )
    }

    private var tapping = false

    private var strengthPots = 2
    override var lastPotion = 0L

    private var gaps = 32
    override var lastGap = 0L

    private var pearls = 5
    private var lastPearl = 0L

    private var dontStartLeftAC = false

    enum class ArmorEnum {
        BOOTS, LEGGINGS, CHESTPLATE, HELMET
    }

    private var armor = hashMapOf(
        0 to 1,
        1 to 1,
        2 to 1,
        3 to 1
    )

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
    }

    override fun onGameEnd() {
        TimeUtils.setTimeout(fun () {
            Movement.clearAll()
            Mouse.stopLeftAC()
            Combat.stopRandomStrafe()
            tapping = false
            strengthPots = 2
            lastPotion = 0L
            gaps = 32
            lastGap = 0L
            pearls = 5
            lastPearl = 0L
            armor = hashMapOf(
                0 to 1,
                1 to 1,
                2 to 1,
                3 to 1
            )
        }, RandomUtils.randomIntInRange(100, 300))
    }

    override fun onTick() {
        val opponent = opponent() // Store to avoid multiple calls
        if (opponent != null && mc.theWorld != null && mc.thePlayer != null) {
            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent)

            if (!mc.thePlayer.isSprinting) {
                Movement.startSprinting()
            }

            if (distance < Config.maxDistanceAttack) {
                if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword")) {
                    if (!dontStartLeftAC) {
                        Mouse.startLeftAC()
                    }
                }
            } else {
                Mouse.stopLeftAC()
            }

            if (distance < Config.maxDistanceLook) {
                Mouse.startTracking()
            } else {
                Mouse.stopTracking()
            }

            if (distance < 8) {
                Movement.stopJumping()
            }

            if (combo >= 3 && distance >= 3.2 && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(100, 150))
            }

            if (distance < 1.5 || (distance < 2.4 && combo >= 1)) {
                Movement.stopForward()
            } else {
                if (!tapping) {
                    Movement.startForward()
                }
            }

            if (WorldUtils.blockInFront(mc.thePlayer, 3f, 1.5f) != Blocks.air) {
                // wall
                Mouse.setRunningAway(false)
            }

            if (!mc.thePlayer.isPotionActive(net.minecraft.potion.Potion.damageBoost) && System.currentTimeMillis() - lastPotion > 5000) {
                lastPotion = System.currentTimeMillis()
                if (strengthPots > 0) {
                    strengthPots--
                    usePotion(8, distance < 3, EntityUtils.entityFacingAway(opponent, mc.thePlayer))
                }
            }

            for (i in 0..3) {
                if (mc.thePlayer.inventory.armorItemInSlot(i) == null) {
                    Mouse.stopLeftAC()
                    dontStartLeftAC = true
                    if (armor[i]!! > 0) {
                        TimeUtils.setTimeout(fun () {
                            val a = Inventory.setInvItem(ArmorEnum.values()[i].name.lowercase())
                            if (a) {
                                armor[i] = armor[i]!! - 1
                                TimeUtils.setTimeout(fun () {
                                    val r = RandomUtils.randomIntInRange(100, 150)
                                    Mouse.rClick(r)
                                    TimeUtils.setTimeout(fun () {
                                        Inventory.setInvItem("sword")
                                        TimeUtils.setTimeout(fun () {
                                            dontStartLeftAC = false
                                        }, RandomUtils.randomIntInRange(200, 300))
                                    }, r + RandomUtils.randomIntInRange(100, 150))
                                }, RandomUtils.randomIntInRange(200, 400))
                            } else {
                                dontStartLeftAC = false
                            }
                        }, RandomUtils.randomIntInRange(250, 500))
                    }
                }
            }

            if ((mc.thePlayer.health < 10 || !mc.thePlayer.isPotionActive(net.minecraft.potion.Potion.absorption)) && gaps > 0) {
                if (System.currentTimeMillis() - lastGap > 3500 && System.currentTimeMillis() - lastPotion > 3500) {
                    useGap(distance, distance < 2, EntityUtils.entityFacingAway(mc.thePlayer, opponent))
                    gaps--
                }
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (distance > 18 && EntityUtils.entityFacingAway(opponent, mc.thePlayer) && !Mouse.isRunningAway() && System.currentTimeMillis() - lastPearl > 5000 && pearls > 0) {
                lastPearl = System.currentTimeMillis()
                Mouse.stopLeftAC()
                dontStartLeftAC = true
                TimeUtils.setTimeout(fun () {
                    if (Inventory.setInvItem("pearl")) {
                        pearls--
                        Mouse.setUsingProjectile(true)
                        TimeUtils.setTimeout(fun () {
                            Mouse.rClick(RandomUtils.randomIntInRange(100, 150))
                            TimeUtils.setTimeout(fun () {
                                Mouse.setUsingProjectile(false)
                                Inventory.setInvItem("sword")
                                TimeUtils.setTimeout(fun () {
                                    dontStartLeftAC = false
                                }, RandomUtils.randomIntInRange(200, 300))
                            }, RandomUtils.randomIntInRange(250, 300))
                        }, RandomUtils.randomIntInRange(300, 600))
                    } else {
                        dontStartLeftAC = false
                    }
                }, RandomUtils.randomIntInRange(250, 500))
            } else {
                if (distance < 8) {
                    if (opponent.isInvisibleToPlayer(mc.thePlayer)) {
                        clear = false
                        if (WorldUtils.leftOrRightToPoint(mc.thePlayer, Vec3(0.0, 0.0, 0.0))) {
                            movePriority[0] += 4
                        } else {
                            movePriority[1] += 4
                        }
                    } else {
                        if (distance < 4 && combo > 2) {
                            randomStrafe = false
                            val rotations = EntityUtils.getRotations(opponent, mc.thePlayer, false)
                            if (rotations != null) {
                                if (rotations[0] < 0) {
                                    movePriority[1] += 5
                                } else {
                                    movePriority[0] += 5
                                }
                            }
                        } else {
                            randomStrafe = true
                        }
                    }
                }

                handle(clear, randomStrafe, movePriority)
            }
        }
    }

}