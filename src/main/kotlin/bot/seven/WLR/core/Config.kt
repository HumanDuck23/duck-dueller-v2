package bot.seven.WLR.core

import bot.seven.WLR.bot.BotBase
import bot.seven.WLR.bot.boosting.*
import bot.seven.WLR.bot.bots.*
import bot.seven.WLR.bot.replay.ReplayClearingBot
import bot.seven.WLR.wlr
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.math.max as kotlinMax
import kotlin.math.min as kotlinMin

object Config {
    val REGULAR_BOT_OPTIONS = arrayOf("Sumo", "Boxing", "Classic", "OP", "Combo")
    val BOOSTING_BOT_OPTIONS = arrayOf(
        "Sumo", "Blitz", "Boxing", "Classic", "OP", "TNT", "UHC",
        "Bow", "Combo", "NoDebuff", "MW", "Skywars"
    )

    enum class SumoStrafeIntensity {
        LIGHT, MEDIUM, HARD;
        companion object {
            private val valuesArray = values()
            fun fromOrdinal(ordinal: Int): SumoStrafeIntensity = valuesArray.getOrElse(ordinal) { MEDIUM }
            val options: List<String> = valuesArray.map { it.name.lowercase().replaceFirstChar(Char::titlecase) }
        }
    }

    enum class LobbyMovementType(val displayName: String) {
        RANDOM_MOVES("Random"),
        SUMO("Random Movements"),
        STRAFE_WALK("Strafe walk"),
        WALKER("Walker"),
        SLOW_DRIFT("Shift walk"),
        FAST_FORWARD("Sprint forward");

        companion object {
            private val valuesArray = values()
            fun fromOrdinal(ordinal: Int): LobbyMovementType = valuesArray.getOrElse(ordinal) { RANDOM_MOVES }
            val options: List<String> = valuesArray.map { it.displayName }
            fun fromDisplayName(displayName: String): LobbyMovementType = valuesArray.firstOrNull { it.displayName == displayName } ?: RANDOM_MOVES
            fun getActualRandom(): LobbyMovementType = valuesArray.filter { it != RANDOM_MOVES }.random()
        }
    }

    val boostingBotInstances: List<BoostingBotBase> = listOf(
        SumoBoost(), BlitzBoost(), BoxingBoost(), ClassicBoost(), OPBoost(),
        TntBoost(), UhcBoost(), BowBoost(), ComboBoost(), PotionBoost(),
        MwBoost(), SwBoost()
    )
    val replayClearingBotInstance: ReplayClearingBot = ReplayClearingBot()
    val bots: Map<Int, BotBase> = mapOf(0 to Sumo(), 1 to Boxing(), 2 to Classic(), 3 to OP(), 4 to Combo())
    val sumoBotIndex: Int = REGULAR_BOT_OPTIONS.indexOf("Sumo")

    private val minRegularBotIndex = 0
    private val maxRegularBotIndex = if (REGULAR_BOT_OPTIONS.isNotEmpty()) REGULAR_BOT_OPTIONS.size - 1 else 0
    private val minBoostingBotIndex = 0
    private val maxBoostingBotIndex = if (boostingBotInstances.isNotEmpty()) boostingBotInstances.size - 1 else 0

    var currentBot = 0
        private set
    var lobbyMovement = true
        set(value) { if (field != value) { field = value; save() } }
    var selectedLobbyMovementType: LobbyMovementType = LobbyMovementType.RANDOM_MOVES
        private set
    var disableChatMessages = false
    var throwAfterGames = 0
    var disconnectAfterGames = 0
    var disconnectAfterMinutes = 0
    var enableDynamicBreaks = false
        set(value) { if (field != value) { field = value; save() } }
    var playDurationHours = 5
        set(value) { if (field != value) { field = value.coerceIn(0, 24); save() } }
    var breakDurationMinMinutes = 20
        set(value) {
            val coercedValue = value.coerceIn(1, 120)
            if (field != coercedValue) {
                field = coercedValue
                if (field > breakDurationMaxMinutes) breakDurationMaxMinutes = field
                save()
            }
        }
    var breakDurationMaxMinutes = 50
        set(value) {
            val coercedValue = value.coerceIn(1, 180)
            if (field != coercedValue) {
                field = coercedValue
                if (field < breakDurationMinMinutes) breakDurationMinMinutes = field
                save()
            }
        }

    var enableBoostingMode = false
        private set
    var selectedBoostingBotIndex = 0
        private set
    var boostingRequeueDelay = 250
    var enableCustomCamera = false
        set(value) { if (field != value) { field = value; save() } }
    var cameraOffsetX = 0.5f
        set(value) { if (field != value) { field = value.coerceIn(-10.0f, 10.0f); save() } }
    var cameraOffsetY = -5.0f
        set(value) { if (field != value) { field = value.coerceIn(-10.0f, 10.0f); save() } }
    var cameraOffsetZ = 5.0f
        set(value) { if (field != value) { field = value.coerceIn(-15.0f, 15.0f); save() } }
    var cameraPitch = 40.0f
        set(value) { if (field != value) { field = value.coerceIn(-90.0f, 90.0f); save() } }
    var cameraYaw = -180.0f
        set(value) { if (field != value) { field = value.coerceIn(-180.0f, 180.0f); save() } }
    var enableCameraZoom: Boolean = false
        set(value) { if (field != value) { field = value; save() } }
    var cameraZoomFovValue: Float = 70f
        set(value) { if (field != value) { field = value.coerceIn(10f, 90f); save() } }
    var enableReplayClearingMode = false
        private set
    var replayClearingMinDelay = 2000
        set(value) { if (field != value) { field = value.coerceIn(500, 20000); if (field > replayClearingMaxDelay) replayClearingMaxDelay = field; save() } }
    var replayClearingMaxDelay = 5000
        set(value) { if (field != value) { field = value.coerceIn(500, 20000); if (field < replayClearingMinDelay) replayClearingMinDelay = field; save() } }
    var replayClearingCommandCount = 500
        set(value) { if (field != value) { field = value.coerceIn(1, 600); save() } }
    var minCPS = 10
        set(value) { if (field != value) { field = value.coerceIn(1, 20); if (field > maxCPS) maxCPS = field; save() } }
    var maxCPS = 14
        set(value) { if (field != value) { field = value.coerceIn(5, 25); if (field < minCPS) minCPS = field; save() } }
    var lookSpeedHorizontal = 10
        set(value) { if (field != value) { field = value.coerceIn(1,30); save() } }
    var lookSpeedVertical = 5
        set(value) { if (field != value) { field = value.coerceIn(1,30); save() } }
    var lookRand = 0.3f
        set(value) { if (field != value) { field = value.coerceIn(0f, 5f); save() } }
    var maxDistanceLook = 8
        set(value) { if (field != value) { field = value.coerceIn(3,150); save() } }
    var maxDistanceAttack = 5
        set(value) { if (field != value) { field = value.coerceIn(3,8); save() } }
    var enableComboResetByDistance = true
        set(value) { if (field != value) { field = value; save() } }
    var comboResetDistance = 5
        set(value) { if (field != value) { field = value.coerceIn(1,10); save() } }
    var enableSumoDistanceJump = true
        set(value) { if (field != value) { field = value; save() } }
    var enableSumoStrafing = true
        set(value) { if (field != value) { field = value; save() } }
    var sumoStrafeIntensity: SumoStrafeIntensity = SumoStrafeIntensity.MEDIUM
        set(value) { if (field != value) { field = value; save() } }

    var enableHitselecting = true
        set(value) { if (field != value) { field = value; save() } }
    var hitselectChance = 0.15
        set(value) { if (field != value) { field = value.coerceIn(0.0, 1.0); save() } }
    var hitselectMinActivationDistance = 2.8
        set(value) {
            val coercedValue = value.coerceIn(1.0, 6.0)
            if (field != coercedValue) {
                field = coercedValue
                if (field > hitselectMaxActivationDistance) hitselectMaxActivationDistance = field
                save()
            }
        }
    var hitselectMaxActivationDistance = 4.2
        set(value) {
            val coercedValue = value.coerceIn(1.5, 7.0)
            if (field != coercedValue) {
                field = coercedValue
                if (field < hitselectMinActivationDistance) hitselectMinActivationDistance = field
                save()
            }
        }
    var hitselectBaitDurationMin = 80
        set(value) {
            val coercedValue = value.coerceIn(30, 500)
            if (field != coercedValue) {
                field = coercedValue
                if (field > hitselectBaitDurationMax) hitselectBaitDurationMax = field
                save()
            }
        }
    var hitselectBaitDurationMax = 150
        set(value) {
            val coercedValue = value.coerceIn(50, 750)
            if (field != coercedValue) {
                field = coercedValue
                if (field < hitselectBaitDurationMin) hitselectBaitDurationMin = field
                save()
            }
        }
    var hitselectCooldown = 2000
        set(value) { if (field != value) { field = value.coerceIn(500, 10000); save() } }
    var hitselectStopSprintDuringBait = true
        set(value) { if (field != value) { field = value; save() } }
    var hitselectSTapDuringBait = true
        set(value) { if (field != value) { field = value; save() } }
    var hitselectSTapDuration = 60
        set(value) { if (field != value) { field = value.coerceIn(20, 200); save() } }

    var sendAutoGG = true
        set(value) { if (field != value) { field = value; save() } }
    var ggMessage = "gg"
        set(value) { if (field != value) { field = value; save() } }
    var ggDelay = 100
        set(value) { if (field != value) { field = value.coerceIn(0,2000); save() } }
    var sendStartMessage = false
        set(value) { if (field != value) { field = value; save() } }
    var startMessage = "GL HF!"
        set(value) { if (field != value) { field = value; save() } }
    var startMessageDelay = 100
        set(value) { if (field != value) { field = value.coerceIn(0,2000); save() } }
    var autoRqDelay = 2500
        set(value) { if (field != value) { field = value.coerceIn(0,5000); save() } }
    var rqNoGame = 30
        set(value) { if (field != value) { field = value.coerceIn(5,120); save() } }
    var paperRequeue = true
        set(value) { if (field != value) { field = value; save() } }
    var fastRequeue = true
        set(value) { if (field != value) { field = value; save() } }
    var sendWebhookMessages = false
        set(value) { if (field != value) { field = value; save() } }
    var webhookURL = ""
        set(value) { if (field != value) { field = value; save() } }
    var sendWebhookStats = false
        set(value) { if (field != value) { field = value; save() } }
    var sendWebhookDodge = false
        set(value) { if (field != value) { field = value; save() } }
    var boxingFish = false
        set(value) { if (field != value) { field = value; save() } }
    var sessionStatsHUD = true
        set(value) { if (field != value) { field = value; save() } }

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(wlr.configLocation)

    private data class ConfigData(
        var currentBot: Int, var lobbyMovement: Boolean,
        var selectedLobbyMovementTypeOrdinal: Int?,
        var disableChatMessages: Boolean,
        var throwAfterGames: Int, var disconnectAfterGames: Int, var disconnectAfterMinutes: Int,
        var enableDynamicBreaks: Boolean?,
        var playDurationHours: Int?,
        var breakDurationMinMinutes: Int?,
        var breakDurationMaxMinutes: Int?,

        var enableBoostingMode: Boolean, var selectedBoostingBotIndex: Int, var boostingRequeueDelay: Int,
        var enableCustomCamera: Boolean?, var cameraOffsetX: Float?, var cameraOffsetY: Float?,
        var cameraOffsetZ: Float?, var cameraPitch: Float?, var cameraYaw: Float?,
        var enableCameraZoom: Boolean?,
        var cameraZoomFovValue: Float?,
        var enableReplayClearingMode: Boolean?, var replayClearingMinDelay: Int?,
        var replayClearingMaxDelay: Int?, var replayClearingCommandCount: Int?,
        var minCPS: Int?, var maxCPS: Int?, var lookSpeedHorizontal: Int?, var lookSpeedVertical: Int?,
        var lookRand: Float?, var maxDistanceLook: Int?, var maxDistanceAttack: Int?,
        var enableComboResetByDistance: Boolean?, var comboResetDistance: Int?,
        var enableSumoDistanceJump: Boolean?, var enableSumoStrafing: Boolean?,
        var sumoStrafeIntensity: Int?,
        var enableHitselecting: Boolean?,
        var hitselectChance: Double?,
        var hitselectMinActivationDistance: Double?,
        var hitselectMaxActivationDistance: Double?,
        var hitselectBaitDurationMin: Int?,
        var hitselectBaitDurationMax: Int?,
        var hitselectCooldown: Int?,
        var hitselectStopSprintDuringBait: Boolean?,
        var hitselectSTapDuringBait: Boolean?,
        var hitselectSTapDuration: Int?,
        var sendAutoGG: Boolean?, var ggMessage: String?, var ggDelay: Int?,
        var sendStartMessage: Boolean?, var startMessage: String?, var startMessageDelay: Int?,
        var autoRqDelay: Int?, var rqNoGame: Int?, var paperRequeue: Boolean?, var fastRequeue: Boolean?,
        var sendWebhookMessages: Boolean?, var webhookURL: String?, var sendWebhookStats: Boolean?, var sendWebhookDodge: Boolean?,
        var boxingFish: Boolean?, var sessionStatsHUD: Boolean?
    )

    init {
        load()
    }
    private var isLoadingConfig = false

    fun load() {
        isLoadingConfig = true
        try {
            if (configFile.exists()) {
                val data = gson.fromJson(configFile.reader(), ConfigData::class.java)

                currentBot = data.currentBot.coerceIn(minRegularBotIndex, maxRegularBotIndex)
                lobbyMovement = data.lobbyMovement
                selectedLobbyMovementType = LobbyMovementType.fromOrdinal(data.selectedLobbyMovementTypeOrdinal ?: LobbyMovementType.RANDOM_MOVES.ordinal)
                disableChatMessages = data.disableChatMessages
                throwAfterGames = data.throwAfterGames.coerceIn(0, 1000)
                disconnectAfterGames = data.disconnectAfterGames.coerceIn(0, 10000)
                disconnectAfterMinutes = data.disconnectAfterMinutes.coerceIn(0, 500)
                enableDynamicBreaks = data.enableDynamicBreaks ?: false
                playDurationHours = data.playDurationHours?.coerceIn(0, 24) ?: 5
                breakDurationMinMinutes = data.breakDurationMinMinutes?.coerceIn(1, 120) ?: 20
                breakDurationMaxMinutes = data.breakDurationMaxMinutes?.coerceIn(1, 180) ?: 50


                enableBoostingMode = data.enableBoostingMode
                selectedBoostingBotIndex = data.selectedBoostingBotIndex.coerceIn(minBoostingBotIndex, maxBoostingBotIndex)
                boostingRequeueDelay = data.boostingRequeueDelay.coerceIn(0, 5000).let { kotlinMax(50, it) }

                enableCustomCamera = data.enableCustomCamera ?: false
                cameraOffsetX = data.cameraOffsetX ?: 0.5f
                cameraOffsetY = data.cameraOffsetY ?: -5.0f
                cameraOffsetZ = data.cameraOffsetZ ?: 5.0f
                cameraPitch = data.cameraPitch ?: 40.0f
                cameraYaw = data.cameraYaw ?: -180.0f
                enableCameraZoom = data.enableCameraZoom ?: false
                cameraZoomFovValue = data.cameraZoomFovValue ?: 70f
                enableReplayClearingMode = data.enableReplayClearingMode ?: false
                replayClearingMinDelay = data.replayClearingMinDelay ?: 2000
                replayClearingMaxDelay = data.replayClearingMaxDelay ?: 5000
                replayClearingCommandCount = data.replayClearingCommandCount ?: 500
                minCPS = data.minCPS ?: 10
                maxCPS = data.maxCPS ?: 14
                lookSpeedHorizontal = data.lookSpeedHorizontal ?: 10
                lookSpeedVertical = data.lookSpeedVertical ?: 5
                lookRand = data.lookRand ?: 0.3f
                maxDistanceLook = data.maxDistanceLook ?: 8
                maxDistanceAttack = data.maxDistanceAttack ?: 5
                enableComboResetByDistance = data.enableComboResetByDistance ?: true
                comboResetDistance = data.comboResetDistance ?: 5
                enableSumoDistanceJump = data.enableSumoDistanceJump ?: true
                enableSumoStrafing = data.enableSumoStrafing ?: true
                sumoStrafeIntensity = SumoStrafeIntensity.fromOrdinal(data.sumoStrafeIntensity ?: SumoStrafeIntensity.MEDIUM.ordinal)

                enableHitselecting = data.enableHitselecting ?: true
                hitselectChance = data.hitselectChance ?: 0.15
                hitselectMinActivationDistance = data.hitselectMinActivationDistance ?: 2.8
                hitselectMaxActivationDistance = data.hitselectMaxActivationDistance ?: 4.2
                hitselectBaitDurationMin = data.hitselectBaitDurationMin ?: 80
                hitselectBaitDurationMax = data.hitselectBaitDurationMax ?: 150
                hitselectCooldown = data.hitselectCooldown ?: 2000
                hitselectStopSprintDuringBait = data.hitselectStopSprintDuringBait ?: true
                hitselectSTapDuringBait = data.hitselectSTapDuringBait ?: true
                hitselectSTapDuration = data.hitselectSTapDuration ?: 60

                sendAutoGG = data.sendAutoGG ?: true
                ggMessage = data.ggMessage ?: "gg"
                ggDelay = data.ggDelay ?: 100
                sendStartMessage = data.sendStartMessage ?: false
                startMessage = data.startMessage ?: "GL HF!"
                startMessageDelay = data.startMessageDelay ?: 100
                autoRqDelay = data.autoRqDelay ?: 2500
                rqNoGame = data.rqNoGame ?: 30
                paperRequeue = data.paperRequeue ?: true
                fastRequeue = data.fastRequeue ?: true
                sendWebhookMessages = data.sendWebhookMessages ?: false
                webhookURL = data.webhookURL ?: ""
                sendWebhookStats = data.sendWebhookStats ?: false
                sendWebhookDodge = data.sendWebhookDodge ?: false
                boxingFish = data.boxingFish ?: false
                sessionStatsHUD = data.sessionStatsHUD ?: true
                validateMinMaxPairs()

            } else {
                isLoadingConfig = false
                resetToDefaultsAndSave()
                return
            }
        } catch (e: Exception) {
            System.err.println("Error loading WLR config: ${e.message}")
            e.printStackTrace()
            isLoadingConfig = false
            resetToDefaultsAndSave()
            return
        } finally {
            isLoadingConfig = false
            if (configFile.exists()) {
                save()
            }
        }
    }

    private fun validateMinMaxPairs() {
        var tempMinCPS = minCPS
        var tempMaxCPS = maxCPS
        if (tempMinCPS > tempMaxCPS) { tempMinCPS = tempMaxCPS }
        minCPS = tempMinCPS
        maxCPS = tempMaxCPS

        var tempReplayMin = replayClearingMinDelay
        var tempReplayMax = replayClearingMaxDelay
        if (tempReplayMin > tempReplayMax) { tempReplayMin = tempReplayMax }
        replayClearingMinDelay = tempReplayMin
        replayClearingMaxDelay = tempReplayMax

        var tempHitselectMinActivation = hitselectMinActivationDistance
        var tempHitselectMaxActivation = hitselectMaxActivationDistance
        if (tempHitselectMinActivation > tempHitselectMaxActivation) { tempHitselectMinActivation = tempHitselectMaxActivation }
        hitselectMinActivationDistance = tempHitselectMinActivation
        hitselectMaxActivationDistance = tempHitselectMaxActivation

        var tempHitselectMinBait = hitselectBaitDurationMin
        var tempHitselectMaxBait = hitselectBaitDurationMax
        if (tempHitselectMinBait > tempHitselectMaxBait) { tempHitselectMinBait = tempHitselectMaxBait }
        hitselectBaitDurationMin = tempHitselectMinBait
        hitselectBaitDurationMax = tempHitselectMaxBait

        var tempBreakMinMins = breakDurationMinMinutes
        var tempBreakMaxMins = breakDurationMaxMinutes
        if (tempBreakMinMins > tempBreakMaxMins) { tempBreakMinMins = tempBreakMaxMins }
        breakDurationMinMinutes = tempBreakMinMins
        breakDurationMaxMinutes = tempBreakMaxMins
    }


    private fun resetToDefaultsAndSave() {
        val oldIsLoading = isLoadingConfig
        isLoadingConfig = true

        currentBot = 0
        lobbyMovement = true
        selectedLobbyMovementType = LobbyMovementType.RANDOM_MOVES
        disableChatMessages = false
        throwAfterGames = 0
        disconnectAfterGames = 0
        disconnectAfterMinutes = 0
        enableDynamicBreaks = false
        playDurationHours = 5
        breakDurationMinMinutes = 20
        breakDurationMaxMinutes = 50

        enableBoostingMode = false
        selectedBoostingBotIndex = 0
        boostingRequeueDelay = 250
        enableCustomCamera = false
        cameraOffsetX = 0.0f
        cameraOffsetY = -2.2f
        cameraOffsetZ = 3.5f
        cameraPitch = 40.0f
        cameraYaw = -180.0f
        enableCameraZoom = false
        cameraZoomFovValue = 70f
        enableReplayClearingMode = false
        replayClearingMinDelay = 2000
        replayClearingMaxDelay = 5000
        replayClearingCommandCount = 490
        minCPS = 10
        maxCPS = 14
        lookSpeedHorizontal = 10
        lookSpeedVertical = 5
        lookRand = 0.3f
        maxDistanceLook = 8
        maxDistanceAttack = 5
        enableComboResetByDistance = true
        comboResetDistance = 5
        enableSumoDistanceJump = true
        enableSumoStrafing = true
        sumoStrafeIntensity = SumoStrafeIntensity.MEDIUM
        enableHitselecting = true
        hitselectChance = 0.15
        hitselectMinActivationDistance = 2.8
        hitselectMaxActivationDistance = 4.2
        hitselectBaitDurationMin = 80
        hitselectBaitDurationMax = 150
        hitselectCooldown = 2000
        hitselectStopSprintDuringBait = true
        hitselectSTapDuringBait = true
        hitselectSTapDuration = 60
        sendAutoGG = true
        ggMessage = "gg"
        ggDelay = 100
        sendStartMessage = false
        startMessage = "GL HF!"
        startMessageDelay = 100
        autoRqDelay = 2500
        rqNoGame = 30
        paperRequeue = true
        fastRequeue = true
        sendWebhookMessages = false
        webhookURL = ""
        sendWebhookStats = false
        sendWebhookDodge = false
        boxingFish = false
        sessionStatsHUD = true

        isLoadingConfig = oldIsLoading
        if (!isLoadingConfig) {
            validateMinMaxPairs()
            save()
        }
    }

    fun save() {
        if (isLoadingConfig) return
        validateMinMaxPairs()

        try {
            configFile.parentFile?.mkdirs()
            val data = ConfigData(
                currentBot, lobbyMovement,
                selectedLobbyMovementType.ordinal,
                disableChatMessages, throwAfterGames, disconnectAfterGames,
                disconnectAfterMinutes,
                enableDynamicBreaks, playDurationHours, breakDurationMinMinutes, breakDurationMaxMinutes,
                enableBoostingMode, selectedBoostingBotIndex, boostingRequeueDelay,
                enableCustomCamera, cameraOffsetX, cameraOffsetY, cameraOffsetZ, cameraPitch, cameraYaw,
                enableCameraZoom, cameraZoomFovValue,
                enableReplayClearingMode, replayClearingMinDelay, replayClearingMaxDelay, replayClearingCommandCount,
                minCPS, maxCPS, lookSpeedHorizontal, lookSpeedVertical, lookRand, maxDistanceLook,
                maxDistanceAttack, enableComboResetByDistance, comboResetDistance,
                enableSumoDistanceJump, enableSumoStrafing,
                sumoStrafeIntensity.ordinal,
                enableHitselecting, hitselectChance, hitselectMinActivationDistance, hitselectMaxActivationDistance,
                hitselectBaitDurationMin, hitselectBaitDurationMax, hitselectCooldown,
                hitselectStopSprintDuringBait, hitselectSTapDuringBait, hitselectSTapDuration,
                sendAutoGG, ggMessage, ggDelay, sendStartMessage, startMessage, startMessageDelay, autoRqDelay, rqNoGame,
                paperRequeue, fastRequeue, sendWebhookMessages, webhookURL, sendWebhookStats, sendWebhookDodge,
                boxingFish, sessionStatsHUD
            )
            configFile.writeText(gson.toJson(data))
        } catch (e: Exception) {
            System.err.println("Error saving WLR config: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setSelectedLobbyMovementType(type: LobbyMovementType) {
        if (selectedLobbyMovementType != type) {
            selectedLobbyMovementType = type
            save()
        }
    }
    fun setSelectedLobbyMovementType(index: Int) = setSelectedLobbyMovementType(LobbyMovementType.fromOrdinal(index))

    fun setCurrentBot(newBotIndex: Int) {
        val clampedIndex = newBotIndex.coerceIn(minRegularBotIndex, maxRegularBotIndex)
        if (enableBoostingMode || enableReplayClearingMode || currentBot != clampedIndex) {
            currentBot = clampedIndex
            enableBoostingMode = false
            enableReplayClearingMode = false
            save()
            wlr.updateActiveBot(false, false, currentBot, null)
        }
    }

    fun setEnableBoostingMode(enabled: Boolean) {
        if (enableBoostingMode != enabled) {
            enableBoostingMode = enabled
            if (enabled) enableReplayClearingMode = false
            save()
            wlr.updateActiveBot(if (enabled) false else null, enabled, null, if (enabled) selectedBoostingBotIndex else null)
        }
    }

    fun setSelectedBoostingBot(newBoostingIndex: Int) {
        val clampedIndex = newBoostingIndex.coerceIn(minBoostingBotIndex, maxBoostingBotIndex)
        if (selectedBoostingBotIndex != clampedIndex || !enableBoostingMode || enableReplayClearingMode) {
            selectedBoostingBotIndex = clampedIndex
            enableBoostingMode = true
            enableReplayClearingMode = false
            save()
            wlr.updateActiveBot(false, true, null, selectedBoostingBotIndex)
        }
    }

    fun setEnableReplayClearingMode(enabled: Boolean) {
        if (enableReplayClearingMode != enabled) {
            enableReplayClearingMode = enabled
            if (enabled) enableBoostingMode = false
            save()
            wlr.updateActiveBot(enabled, if (enabled) false else null, null, null)
        }
    }

    fun getActiveBoostingBotInstance(): BoostingBotBase? {
        return if (boostingBotInstances.isEmpty() || selectedBoostingBotIndex !in boostingBotInstances.indices) null
        else boostingBotInstances[selectedBoostingBotIndex]
    }
}