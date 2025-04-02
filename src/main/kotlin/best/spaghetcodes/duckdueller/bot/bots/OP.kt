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
    
    // Nye variabler for forbedret kampkontroll
    var tapping = false
    var strafePattern = 0  // 0 = aws, 1 = wsd
    var criticalCooldown = 0L
    var criticalJumpTimer = 0L
    var isCriticalReady = true
    var tryhardModeThreshold = 3  // Tryhard mode aktiveres etter 3 treff
    
    // Forbedret healing variabler
    var isHealing = false
    var healingStartTime = 0L
    var healingDuration = 2000L // Hvor lenge botten fokuserer på healing (2 sekunder)
    var lastDefensiveAction = 0L
    var defensiveActionCooldown = 5000L // Minst 5 sekunder mellom defensive perioder
    var inFireDamageTicks = 0
    var wasInFire = false
    var fleeingFromFire = false
    var fleeFireStartTime = 0L
    var fleeFireDuration = 1500L // Hvor lenge botten prøver å unngå brann
    var lastHealthCheck = 0L
    var healthCheckInterval = 400L // Sjekk helse oftere (400ms)

    override fun onGameStart() {
        Movement.startSprinting()
        Movement.startForward()
        TimeUtils.setTimeout(Movement::startJumping, RandomUtils.randomIntInRange(400, 1200))
        
        // Reset variabler
        speedPotsLeft = 2
        regenPotsLeft = 2
        gapsLeft = 6
        
        lastSpeedUse = 0L
        lastRegenUse = 0L
        lastPotion = 0L
        lastGap = 0L
        lastHealthCheck = 0L
        lastDefensiveAction = 0L
        isHealing = false
        healingStartTime = 0L
        inFireDamageTicks = 0
        wasInFire = false
        fleeingFromFire = false
        fleeFireStartTime = 0L
        
        // Aktiver speed pot med forsinkelse (ikke umiddelbart)
        // Merk: Vi bruker kun én speed pot tidlig i kampen
        TimeUtils.setTimeout({
            if (speedPotsLeft > 0) {
                useSplashPotion(speedDamage, false, false)
                speedPotsLeft--
                lastSpeedUse = System.currentTimeMillis()
            }
        }, RandomUtils.randomIntInRange(4000, 7000)) // Vent 4-7 sekunder med første pot
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
        isHealing = false

        Mouse.stopLeftAC()
        val i = TimeUtils.setInterval(Mouse::stopLeftAC, 100, 100)
        TimeUtils.setTimeout(fun () {
            i?.cancel()
            Mouse.stopTracking()
            Movement.clearAll()
            Combat.stopRandomStrafe()
        }, RandomUtils.randomIntInRange(200, 400))
    }
    
    // Pro player inspired 7-tap with AWS/WSD pattern (Marcel style)
    fun performSevenTap() {
        // True random direction choice
        val useLeft = RandomUtils.randomBool()
        strafePattern = if (useLeft) 0 else 1
        
        if (useLeft) { // AWS pattern
            Movement.stopRight()
            Movement.startForward()
            Movement.startLeft()
            
            TimeUtils.setTimeout({
                // Micro-pause for better hit registration
                Movement.stopForward()
                Movement.stopLeft()
                
                TimeUtils.setTimeout({
                    Movement.startForward()
                    Movement.startLeft()
                }, 50)
            }, 70)
        } else { // WSD pattern
            Movement.stopLeft()
            Movement.startForward()
            Movement.startRight()
            
            TimeUtils.setTimeout({
                // Micro-pause for better hit registration
                Movement.stopForward()
                Movement.stopRight()
                
                TimeUtils.setTimeout({
                    Movement.startForward()
                    Movement.startRight()
                }, 50)
            }, 70)
        }
        
        // W-tap with precise timing (Marcel technique)
        Combat.wTap(70)  // Shorter w-tap timing
    }
    
    // Forbedret mikro-pause for tidlige komboer
    fun performEarlyComboMicroPause() {
        // Use true random direction
        val useLeft = RandomUtils.randomBool()
        
        // Brief movement pause before hit
        Movement.stopForward()
        
        // Small directional change during pause
        if (useLeft) {
            Movement.stopRight()
            Movement.startLeft()
        } else {
            Movement.stopLeft()
            Movement.startRight()
        }
        
        TimeUtils.setTimeout({
            // Resume movement with immediate attack
            Movement.startForward()
            Mouse.leftClick()
        }, 40)  // Very short pause for early combo stage
    }
    
    // Function to perform a critical hit
    fun attemptCriticalHit() {
        val currentTime = System.currentTimeMillis()
        if (isCriticalReady && currentTime - criticalCooldown > 800) {  // Cooldown for critical hits
            isCriticalReady = false
            criticalCooldown = currentTime

            // Jump for critical hit
            if (mc.thePlayer.onGround) {
                Movement.singleJump(90)  // Quick jump for crit

                // Immediately attack when in the air for a critical hit
                TimeUtils.setTimeout({
                    Mouse.leftClick()
                }, 50)

                TimeUtils.setTimeout({
                    isCriticalReady = true
                }, 800)  // Reset critical readiness after cooldown
            }
        }
    }
    
    // Avansert defensive bevegelse under healing
    fun performDefensiveMovement(distance: Float) {
        // Fokuser på å holde avstand, blokkere og strafe uforutsigbart
        if (distance < 5) {
            // For nær fiende - prøv å lage avstand
            Movement.startBackward()
            Mouse.rClick(500)  // Blokkere lenger mens du trekker deg tilbake
            
            // Strafe uforutsigbart
            if (RandomUtils.randomIntInRange(0, 100) > 50) {
                if (RandomUtils.randomBool()) {
                    Movement.startRight()
                    TimeUtils.setTimeout({ Movement.stopRight() }, 250)
                } else {
                    Movement.startLeft()
                    TimeUtils.setTimeout({ Movement.stopLeft() }, 250)
                }
            }
            
            // Hopp av og til for å være vanskeligere å treffe
            if (RandomUtils.randomIntInRange(0, 100) > 80 && mc.thePlayer.onGround) {
                Movement.singleJump(RandomUtils.randomIntInRange(150, 250))
            }
            
            TimeUtils.setTimeout({
                Movement.stopBackward()
                Movement.startForward()
            }, 500)
        } else {
            // Mer avslappet defensive bevegelser hvis fienden er lenger unna
            Mouse.rClick(300)  // Blokkere kortere
            
            // Strafe i en retning litt lenger
            if (RandomUtils.randomBool()) {
                Movement.startRight()
                TimeUtils.setTimeout({ Movement.stopRight() }, 350)
            } else {
                Movement.startLeft()
                TimeUtils.setTimeout({ Movement.stopLeft() }, 350)
            }
        }
    }
    
    // Fluktstrategi fra ild
    fun fleeFromFire() {
        // Forsøk å finne retningen bort fra ilden
        val fleeDirection = findSafeDirection()
        
        // Hopp for å komme ut av ilden raskt
        if (mc.thePlayer.onGround) {
            Movement.singleJump(RandomUtils.randomIntInRange(200, 300))
        }
        
        // Beveg i den valgte flyktretningen
        if (fleeDirection == "forward") {
            Movement.stopBackward()
            Movement.startForward()
        } else if (fleeDirection == "backward") {
            Movement.stopForward()
            Movement.startBackward()
        } else if (fleeDirection == "left") {
            Movement.stopRight()
            Movement.startLeft()
        } else if (fleeDirection == "right") {
            Movement.stopLeft()
            Movement.startRight()
        }
        
        // Fortsett normal kampbevegelse etter litt
        TimeUtils.setTimeout({
            if (fleeDirection == "backward") {
                Movement.stopBackward()
                Movement.startForward()
            }
        }, 500)
    }
    
    // Finn en trygg retning vekk fra ild
    fun findSafeDirection(): String {
        // Sjekk blokker i ulike retninger
        val forwardBlock = WorldUtils.blockInFront(mc.thePlayer, 1f, 0.5f)
        val backwardBlock = WorldUtils.blockBehind(mc.thePlayer, 1f, 0.5f)
        val leftBlock = WorldUtils.blockToLeft(mc.thePlayer, 1f, 0.5f)
        val rightBlock = WorldUtils.blockToRight(mc.thePlayer, 1f, 0.5f)
        
        // Prioriter retninger uten ild
        if (forwardBlock != Blocks.fire) return "forward"
        if (backwardBlock != Blocks.fire) return "backward"
        if (leftBlock != Blocks.fire) return "left"
        if (rightBlock != Blocks.fire) return "right"
        
        // Hvis alle retninger har ild, velg forward som standard
        return "forward"
    }
    
    // Sjekk om spilleren er i ild
    fun isInFire(): Boolean {
        return mc.thePlayer.fire > 0 || 
               WorldUtils.blockUnder(mc.thePlayer, 0.1f) == Blocks.fire ||
               WorldUtils.blockInFront(mc.thePlayer, 0.5f, 0.5f) == Blocks.fire
    }
    
    // Forbedret healing-strategi for optimal potions og gapples
    fun checkAndHeal(distance: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastHealthCheck < healthCheckInterval) return
        lastHealthCheck = currentTime
        
        // Sjekk om i ild og aktiver flukt hvis nødvendig
        if (isInFire()) {
            inFireDamageTicks++
            if (!fleeingFromFire) {
                fleeingFromFire = true
                fleeFireStartTime = currentTime
                fleeFromFire()
            }
            wasInFire = true
        } else {
            if (wasInFire) {
                inFireDamageTicks = 0
                wasInFire = false
            }
            
            // Stopp flukt etter en viss tid
            if (fleeingFromFire && currentTime - fleeFireStartTime > fleeFireDuration) {
                fleeingFromFire = false
            }
        }
        
        // Ikke start healing hvis vi allerede healer
        if (isHealing) {
            // Sjekk om healing-periode er over
            if (currentTime - healingStartTime > healingDuration) {
                isHealing = false
            } else {
                // Fortsett defensive bevegelser under healing
                performDefensiveMovement(distance)
                return
            }
        }
        
        val healthPercent = mc.thePlayer.health / mc.thePlayer.maxHealth
        val opponentHealth = opponent()?.health ?: 20f
        val hasDisadvantage = mc.thePlayer.health < opponentHealth
        
        // Sjekk for speed effekt først
        var hasSpeed = false
        for (effect in mc.thePlayer.activePotionEffects) {
            if (effect.effectName.lowercase().contains("speed")) {
                hasSpeed = true
                break
            }
        }
        
        // Sjekk for regeneration effekt
        var hasRegen = false
        for (effect in mc.thePlayer.activePotionEffects) {
            if (effect.effectName.lowercase().contains("regen")) {
                hasRegen = true
                break
            }
        }
        
        // Appliser speed potion hvis ikke aktiv og cooldown er over
        // Viktig: Vi bruker kun den andre speed potion når vi virkelig trenger den
        if (!hasSpeed && speedPotsLeft > 0 && currentTime - lastSpeedUse > 20000 && currentTime - lastPotion > 3000) {
            // Kun bruk andre speed pot hvis helsen er under 60% eller under kamp
            if ((healthPercent < 0.6 || distance < 6) && speedPotsLeft == 1) {
                useSplashPotion(speedDamage, distance < 3.5, EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))
                speedPotsLeft--
                lastSpeedUse = currentTime
                return
            }
        }
        
        // Healing-strategi basert på helse og kampstatus
        if (!Mouse.isUsingProjectile() && !Mouse.isRunningAway() && !Mouse.isUsingPotion()) {
            
            // Forbedret helsesjekk-logikk basert på No Debuff PvP
            if ((healthPercent < 0.6) || 
                (healthPercent < 0.75 && hasDisadvantage) || 
                (healthPercent < 0.85 && inFireDamageTicks > 3) ||
                (healthPercent < 0.85 && distance < 3 && hasDisadvantage)) {
                
                // Aktiver defensive modus hvis det ikke er på cooldown
                if (currentTime - lastDefensiveAction > defensiveActionCooldown) {
                    isHealing = true
                    healingStartTime = currentTime
                    lastDefensiveAction = currentTime
                    performDefensiveMovement(distance)
                }
                
                // Prioriter regen pots først, så gapples
                if (!hasRegen && regenPotsLeft > 0 && currentTime - lastRegenUse > 3500 && currentTime - lastPotion > 3000) {
                    useSplashPotion(regenDamage, distance < 2.5, EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))
                    regenPotsLeft--
                    lastRegenUse = currentTime
                    return
                }
                
                // Bruk gapple hvis ingen regen potions er tilgjengelige eller på cooldown
                if ((regenPotsLeft == 0 || hasRegen) && gapsLeft > 0 && currentTime - lastGap > 3500) {
                    useGap(distance, distance < 2.5, EntityUtils.entityFacingAway(mc.thePlayer, opponent()!!))
                    gapsLeft--
                    return
                }
            }
        }
    }

    override fun onAttack() {
        val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())
        
        // Ikke angrip hvis vi er i healing-modus
        if (isHealing) {
            return
        }
        
        if (mc.thePlayer != null && mc.thePlayer.heldItem != null) {
            val n = mc.thePlayer.heldItem.unlocalizedName.lowercase()
            if (n.contains("rod")) { 
                // Bruk 7-tap istedenfor vanlig w-tap for bedre rod kontroll
                performSevenTap()
                tapping = true
                combo--
                TimeUtils.setTimeout(fun () {
                    tapping = false
                }, 300)
            } else if (n.contains("sword")) {
                if (distance < 3) {
                    // Sverd strategi basert på combo
                    if (combo < tryhardModeThreshold) {
                        // Initial hits (0-2): Kombiner block hitting og mikro-pauser
                        if (RandomUtils.randomIntInRange(0, 100) > 70) {
                            performEarlyComboMicroPause()
                        } else {
                            if (mc.thePlayer.onGround) {
                                attemptCriticalHit()
                            } else {
                                Mouse.rClick(RandomUtils.randomIntInRange(60, 90)) // otherwise just blockhit
                            }
                        }
                    } else if (combo >= tryhardModeThreshold && combo < 6) {
                        // Early tryhard phase (3-5): Block hitting + attacks
                        Mouse.rClick(RandomUtils.randomIntInRange(50, 70))
                        Combat.wTap(70)
                    } else if (combo >= 6) {
                        // Late combo phase (6+): Pure DPS
                        Mouse.rClickUp() // No blocking to maximize damage
                        TimeUtils.setTimeout({
                            Mouse.leftClick()
                        }, 40)
                        TimeUtils.setTimeout({
                            Mouse.leftClick()
                        }, 100)
                    }
                } else {
                    // På lenger avstand, bruk 7-tap noen ganger istedenfor vanlig w-tap
                    if (RandomUtils.randomIntInRange(0, 100) > 65) {
                        performSevenTap()
                    } else {
                        Combat.wTap(100)
                    }
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
            if (!mc.thePlayer.isSprinting && !isHealing && !fleeingFromFire) {
                Movement.startSprinting()
            }

            val distance = EntityUtils.getDistanceNoY(mc.thePlayer, opponent())
            
            // Sjekk helse og healing-strategi
            checkAndHeal(distance)

            if (distance < (DuckDueller.config?.maxDistanceLook ?: 150)) {
                Mouse.startTracking()
            } else {
                Mouse.stopTracking()
            }

            // Ikke angrip hvis vi er i healing-modus
            if (!isHealing && !fleeingFromFire) {
                if (distance < (DuckDueller.config?.maxDistanceAttack ?: 10)) {
                    if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword")) {
                        Mouse.startLeftAC()
                    }
                } else {
                    Mouse.stopLeftAC()
                }
            } else {
                Mouse.stopLeftAC()
            }

            // Hopping og bevegelseslogikk
            if (!isHealing && !fleeingFromFire) {
                if (distance > 8.8) {
                    if (opponent() != null && opponent()!!.heldItem != null && opponent()!!.heldItem.unlocalizedName.lowercase().contains("bow")) {
                        if (!Mouse.isRunningAway()) {
                            Movement.stopJumping()
                            
                            // Forbedret bow-dodge med tilfeldige retningsskifter
                            if (RandomUtils.randomIntInRange(0, 100) > 70) {
                                if (RandomUtils.randomBool()) {
                                    Movement.stopRight()
                                    Movement.startLeft()
                                } else {
                                    Movement.stopLeft()
                                    Movement.startRight()
                                }
                                
                                // Mikro-pauser under bue-unnvikelse
                                if (RandomUtils.randomIntInRange(0, 100) > 75) {
                                    Movement.stopForward()
                                    TimeUtils.setTimeout({ Movement.startForward() }, 70)
                                }
                            }
                        }
                    } else {
                        Movement.startJumping()
                    }
                } else {
                    Movement.stopJumping()
                }
            }

            val movePriority = arrayListOf(0, 0)
            var clear = false
            var randomStrafe = false

            if (!isHealing && !fleeingFromFire) {
                if (distance < 0.7 || (distance < 1.4 && combo >= 1)) {
                    Movement.stopForward()
                } else {
                    if (!tapping) {
                        Movement.startForward()
                    }
                }
            }

            if (distance < 1.5 && mc.thePlayer.heldItem != null && !mc.thePlayer.heldItem.unlocalizedName.lowercase().contains("sword") && !Mouse.isUsingPotion() && !isHealing) {
                Inventory.setInvItem("sword")
                Mouse.rClickUp()
                Mouse.startLeftAC()
            }

            if (WorldUtils.blockInFront(mc.thePlayer, 3f, 1.5f) != Blocks.air) {
                // wall
                Mouse.setRunningAway(false)
            }

            // Forbedret ild-unnvikelseslogikk
            if (WorldUtils.blockInPath(mc.thePlayer, RandomUtils.randomIntInRange(3, 7), 1f) == Blocks.fire || 
                WorldUtils.blockInFront(mc.thePlayer, 3f, 0.5f) == Blocks.fire) {
                
                // Prøv å hoppe over eller unngå ilden
                if (mc.thePlayer.onGround) {
                    Movement.singleJump(RandomUtils.randomIntInRange(250, 350)) // Høyere hopp for å unngå ild
                }
                
                // Sjekk for alternative ruter
                if (WorldUtils.blockToLeft(mc.thePlayer, 2f, 0.5f) != Blocks.fire) {
                    Movement.stopRight()
                    Movement.startLeft()
                    TimeUtils.setTimeout({ Movement.stopLeft() }, 400)
                } else if (WorldUtils.blockToRight(mc.thePlayer, 2f, 0.5f) != Blocks.fire) {
                    Movement.stopLeft()
                    Movement.startRight()
                    TimeUtils.setTimeout({ Movement.stopRight() }, 400)
                }
            }

            if (!isHealing && !fleeingFromFire && !Mouse.isUsingProjectile() && !Mouse.isRunningAway() && !Mouse.isUsingPotion() && !Mouse.rClickDown && System.currentTimeMillis() - lastGap > 2500) {
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
                                        val rotations = EntityUtils.getRotations(opponent()!!, mc.thePlayer, false)
                                        if (rotations != null) {
                                            // Forbedret strafing med ekte 50/50 sjanse
                                            if (RandomUtils.randomBool()) {
                                                if (rotations[0] < 0) {
                                                    movePriority[1] += 5
                                                } else {
                                                    movePriority[0] += 5
                                                }
                                            } else {
                                                // Noen ganger velg tilfeldig retning for å være uforutsigbar
                                                if (RandomUtils.randomBool()) {
                                                    movePriority[0] += 5
                                                } else {
                                                    movePriority[1] += 5
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            handle(clear, randomStrafe, movePriority)
        }
    }
}
