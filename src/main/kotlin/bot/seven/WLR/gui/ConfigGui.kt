package bot.seven.WLR.gui

import bot.seven.WLR.core.Config
import bot.seven.WLR.core.ConfigSorter
import bot.seven.WLR.gui.elements.*
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

class ConfigGui : GuiScreen() {
    private var panelX = 0
    private var panelY = 0
    private var panelWidth = 0
    private var panelHeight = 0
    private val panelCornerRadius = 8f
    private val topBarHeight = 35
    private val tabBarButtonHeight = 28
    private val panelPadding = 15
    private val closeButtonSize = 18
    private var isCloseButtonHovered = false
    private data class Tab(
        val name: String,
        val id: String,
        val icon: ResourceLocation? = null,
        val components: MutableList<GuiComponentBase> = mutableListOf(),
        var scrollY: Float = 0f,
        var targetScrollY: Int = 0,
        var contentHeight: Int = 0,
        var maxScrollY: Int = 0
    )

    private val tabs = mutableListOf<Tab>()
    private var currentTabIndex = 0
    private fun currentTab(): Tab = tabs.getOrNull(currentTabIndex) ?: tabs.firstOrNull() ?: Tab("Error", "error_no_tabs")

    private val guiTitle = "WLR Settings"
    private val tabButtonWidth = 85

    private var tabScrollX: Float = 0f
    private var targetTabScrollX: Int = 0
    private var totalTabsWidthUnscrolled: Int = 0
    private var visibleTabBarAreaWidth: Int = 0
    private var maxTabScrollX: Int = 0
    private val tabButtonSpacing = 4
    private val tabBarScrollButtonWidth = 20

    private val scrollbarWidth = 8
    private val scrollbarMargin = 5
    private val SCROLL_SMOOTHING_FACTOR = 0.28f

    private var isDraggingContentScrollbar = false
    private var contentScrollbarMouseDragStartY = 0f
    private var contentScrollbarInitialScrollY = 0f
    private var openDropdown: Dropdown? = null
    private lateinit var enableDynamicBreaksCheckbox: Checkbox
    private lateinit var playDurationHoursSlider: Slider
    private lateinit var breakDurationMinMinutesSlider: Slider
    private lateinit var breakDurationMaxMinutesSlider: Slider
    private lateinit var currentBotDropdown: Dropdown
    private lateinit var lobbyMovementCheckbox: Checkbox
    private lateinit var lobbyMovementTypeDropdown: Dropdown
    private lateinit var disableChatMessagesCheckbox: Checkbox
    private lateinit var throwAfterGamesSlider: Slider
    private lateinit var disconnectAfterGamesSlider: Slider
    private lateinit var disconnectAfterMinutesSlider: Slider
    private lateinit var enableBoostingModeCheckbox: Checkbox
    private lateinit var selectedBoostingBotDropdown: Dropdown
    private lateinit var boostingRequeueDelaySlider: Slider
    private lateinit var enableCustomCameraCheckbox: Checkbox
    private lateinit var cameraOffsetXSlider: Slider
    private lateinit var cameraOffsetYSlider: Slider
    private lateinit var cameraOffsetZSlider: Slider
    private lateinit var cameraPitchSlider: Slider
    private lateinit var cameraYawSlider: Slider
    private lateinit var enableCameraZoomCheckbox: Checkbox
    private lateinit var cameraZoomFovSlider: Slider
    private lateinit var enableReplayClearingModeCheckbox: Checkbox
    private lateinit var replayClearingMinDelaySlider: Slider
    private lateinit var replayClearingMaxDelaySlider: Slider
    private lateinit var replayClearingCommandCountSlider: Slider
    private lateinit var minCPSSlider: Slider
    private lateinit var maxCPSSlider: Slider
    private lateinit var lookSpeedHorizontalSlider: Slider
    private lateinit var lookSpeedVerticalSlider: Slider
    private lateinit var lookRandSlider: Slider
    private lateinit var maxDistanceLookSlider: Slider
    private lateinit var maxDistanceAttackSlider: Slider
    private lateinit var enableComboResetByDistanceCheckbox: Checkbox
    private lateinit var comboResetDistanceSlider: Slider
    private lateinit var enableSumoDistanceJumpCheckbox: Checkbox
    private lateinit var enableSumoStrafingCheckbox: Checkbox
    private lateinit var sumoStrafeIntensityDropdown: Dropdown
    private lateinit var enableHitselectingCheckbox: Checkbox
    private lateinit var hitselectChanceSlider: Slider
    private lateinit var hitselectMinActivationDistanceSlider: Slider
    private lateinit var hitselectMaxActivationDistanceSlider: Slider
    private lateinit var hitselectBaitDurationMinSlider: Slider
    private lateinit var hitselectBaitDurationMaxSlider: Slider
    private lateinit var hitselectCooldownSlider: Slider
    private lateinit var hitselectStopSprintDuringBaitCheckbox: Checkbox
    private lateinit var hitselectSTapDuringBaitCheckbox: Checkbox
    private lateinit var hitselectSTapDurationSlider: Slider
    private lateinit var sendAutoGGCheckbox: Checkbox
    private lateinit var ggMessageTextField: Textfield
    private lateinit var ggDelaySlider: Slider
    private lateinit var sendStartMessageCheckbox: Checkbox
    private lateinit var startMessageTextField: Textfield
    private lateinit var startMessageDelaySlider: Slider
    private lateinit var autoRqDelaySlider: Slider
    private lateinit var rqNoGameSlider: Slider
    private lateinit var paperRequeueCheckbox: Checkbox
    private lateinit var fastRequeueCheckbox: Checkbox
    private lateinit var sendWebhookMessagesCheckbox: Checkbox
    private lateinit var webhookURLTextField: Textfield
    private lateinit var boxingFishCheckbox: Checkbox
    private lateinit var sessionStatsHUDCheckbox: Checkbox

    private var logicalCurrentY = 0
    private val interComponentSpacing = 12
    private val componentWidth = 240
    private var labelHeightAboveComponent: Int = 0
    private val contentPaddingTopForComponents = 15
    private val contentPaddingBottomForComponents = 15
    private val allPossibleTabsMap = mutableMapOf<String, Tab>()

    private var nextComponentId = 1
    private fun getNextId(): Int = nextComponentId++


    override fun initGui() {
        super.initGui()
        panelWidth = min(800, this.width - 60)
        panelHeight = min(550, this.height - 60)
        panelX = (this.width - panelWidth) / 2
        panelY = (this.height - panelHeight) / 2

        this.labelHeightAboveComponent = mc.fontRendererObj.FONT_HEIGHT + 3
        nextComponentId = 1
        Keyboard.enableRepeatEvents(true)
        openDropdown = null
        tabScrollX = 0f
        targetTabScrollX = 0
        isDraggingContentScrollbar = false

        try {
            defineAllPossibleTabs()
            orderAndPopulateTabs()

            if (this.tabs.isEmpty()) return
            if (currentTabIndex >= tabs.size) currentTabIndex = 0

            calculateTabScrolling()
            tabs.forEach { tab ->
                calculateContentScrollingForTab(tab)
                tab.scrollY = 0f
                tab.targetScrollY = 0
            }
            updateGuiElementStates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun defineAllPossibleTabs() {
        allPossibleTabsMap.clear()
        allPossibleTabsMap["General"] = Tab("General", "General")
        allPossibleTabsMap["Combat"] = Tab("Combat", "Combat")
        allPossibleTabsMap["Requeue"] = Tab("Requeue", "Requeue")
        allPossibleTabsMap["Messages"] = Tab("Messages", "Messages")
        allPossibleTabsMap["Boosting"] = Tab("Boosting", "Boosting")
        allPossibleTabsMap["Webhook"] = Tab("Webhook", "Webhook")
        allPossibleTabsMap["Replays"] = Tab("Replays", "Replays")
        allPossibleTabsMap["Camera"] = Tab("Camera", "Camera")
        allPossibleTabsMap["HUD"] = Tab("HUD", "HUD")
        allPossibleTabsMap["Misc"] = Tab("Misc", "Misc")
    }

    private fun orderAndPopulateTabs() {
        tabs.clear()
        ConfigSorter.WLR_TAB_ORDER.forEach { tabId ->
            allPossibleTabsMap[tabId]?.let {
                it.components.clear()
                populateComponentsForTab(it)
                tabs.add(it)
            }
        }
        allPossibleTabsMap.values.forEach { if (!tabs.contains(it)) { it.components.clear(); populateComponentsForTab(it); tabs.add(it) } }
    }


    private fun populateComponentsForTab(tab: Tab) {
        val contentAreaWidth = panelWidth - (panelPadding * 2)
        val actualComponentWidth = min(this.componentWidth, contentAreaWidth)
        val startX = panelX + panelPadding + (contentAreaWidth - actualComponentWidth) / 2

        logicalCurrentY = 0
        logicalCurrentY += contentPaddingTopForComponents

        when (tab.id) {
            "General" -> {
                currentBotDropdown = Dropdown(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Current Bot", options=Config.REGULAR_BOT_OPTIONS.toList(), initialSelectedIndex=Config.currentBot, onSelectionChanged={i,_->Config.setCurrentBot(i);updateGuiElementStates()})
                tab.components.add(currentBotDropdown)
                logicalCurrentY+=labelHeightAboveComponent+currentBotDropdown.height+interComponentSpacing
                lobbyMovementCheckbox = Checkbox(id=getNextId(), x=startX, y=logicalCurrentY, label="Lobby Movement", initialValue=Config.lobbyMovement, onValueChanged={v->Config.lobbyMovement=v;updateGuiElementStates()})
                tab.components.add(lobbyMovementCheckbox)
                logicalCurrentY+=lobbyMovementCheckbox.height+interComponentSpacing
                lobbyMovementTypeDropdown = Dropdown(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Movement Type", options=Config.LobbyMovementType.options, initialSelectedIndex=Config.selectedLobbyMovementType.ordinal, onSelectionChanged={i,_->Config.setSelectedLobbyMovementType(i)})
                tab.components.add(lobbyMovementTypeDropdown)
                logicalCurrentY+=labelHeightAboveComponent+lobbyMovementTypeDropdown.height+interComponentSpacing
                disableChatMessagesCheckbox = Checkbox(id=getNextId(), x=startX, y=logicalCurrentY, label="Disable Chat Messages", initialValue=Config.disableChatMessages, onValueChanged={v->Config.disableChatMessages=v;Config.save()})
                tab.components.add(disableChatMessagesCheckbox)
                logicalCurrentY+=disableChatMessagesCheckbox.height+interComponentSpacing
                throwAfterGamesSlider = Slider(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Throw After X Games", initialValue=Config.throwAfterGames.toFloat(), minValue=0f, maxValue=1000f, step=1f, displayFormat={v->if(v==0f)"Disabled" else "%.0f".format(v)}, onValueChanged={v->Config.throwAfterGames=v.toInt();Config.save()})
                tab.components.add(throwAfterGamesSlider)
                logicalCurrentY+=labelHeightAboveComponent+throwAfterGamesSlider.height+interComponentSpacing
                disconnectAfterGamesSlider = Slider(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Disconnect After X Games", initialValue=Config.disconnectAfterGames.toFloat(), minValue=0f, maxValue=10000f, step=10f, displayFormat={v->if(v==0f)"Disabled" else "%.0f".format(v)}, onValueChanged={v->Config.disconnectAfterGames=v.toInt();Config.save()})
                tab.components.add(disconnectAfterGamesSlider)
                logicalCurrentY+=labelHeightAboveComponent+disconnectAfterGamesSlider.height+interComponentSpacing
                disconnectAfterMinutesSlider = Slider(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Disconnect After X Mins", initialValue=Config.disconnectAfterMinutes.toFloat(), minValue=0f, maxValue=500f, step=5f, displayFormat={v->if(v==0f)"Disabled" else "%.0f".format(v)}, onValueChanged={v->Config.disconnectAfterMinutes=v.toInt();Config.save()})
                tab.components.add(disconnectAfterMinutesSlider)
                logicalCurrentY+=labelHeightAboveComponent+disconnectAfterMinutesSlider.height+interComponentSpacing
                enableDynamicBreaksCheckbox = Checkbox(id=getNextId(), x=startX, y=logicalCurrentY, label="Enable Dynamic Breaks", initialValue=Config.enableDynamicBreaks, onValueChanged={v->Config.enableDynamicBreaks=v;updateGuiElementStates()})
                tab.components.add(enableDynamicBreaksCheckbox)
                logicalCurrentY+=enableDynamicBreaksCheckbox.height+interComponentSpacing
                playDurationHoursSlider = Slider(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Play Duration (Hours)", initialValue=Config.playDurationHours.toFloat(), minValue=0f, maxValue=24f, step=1f, displayFormat={v->if(v==0f)"Until Manual Stop" else "%.0f h".format(v)}, onValueChanged={v->Config.playDurationHours=v.toInt()})
                tab.components.add(playDurationHoursSlider)
                logicalCurrentY+=labelHeightAboveComponent+playDurationHoursSlider.height+interComponentSpacing
                breakDurationMinMinutesSlider = Slider(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Min Break Duration (Mins)", initialValue=Config.breakDurationMinMinutes.toFloat(), minValue=1f, maxValue=120f, step=1f, displayFormat={v->"%.0f min".format(v)}, onValueChanged={v->Config.breakDurationMinMinutes=v.toInt()})
                tab.components.add(breakDurationMinMinutesSlider)
                logicalCurrentY+=labelHeightAboveComponent+breakDurationMinMinutesSlider.height+interComponentSpacing
                breakDurationMaxMinutesSlider = Slider(id=getNextId(), x=startX, y=logicalCurrentY+labelHeightAboveComponent, width=actualComponentWidth, label="Max Break Duration (Mins)", initialValue=Config.breakDurationMaxMinutes.toFloat(), minValue=1f, maxValue=180f, step=1f, displayFormat={v->"%.0f min".format(v)}, onValueChanged={v->Config.breakDurationMaxMinutes=v.toInt()})
                tab.components.add(breakDurationMaxMinutesSlider)
                logicalCurrentY+=labelHeightAboveComponent+breakDurationMaxMinutesSlider.height
            }
            "Boosting" -> {
                enableBoostingModeCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Enable Boosting Mode",initialValue=Config.enableBoostingMode,onValueChanged={v->Config.setEnableBoostingMode(v);updateGuiElementStates()})
                tab.components.add(enableBoostingModeCheckbox)
                logicalCurrentY+=enableBoostingModeCheckbox.height+interComponentSpacing
                selectedBoostingBotDropdown=Dropdown(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Selected Boosting Bot",options=Config.BOOSTING_BOT_OPTIONS.toList(),initialSelectedIndex=Config.selectedBoostingBotIndex,onSelectionChanged={i,_->Config.setSelectedBoostingBot(i);updateGuiElementStates()})
                tab.components.add(selectedBoostingBotDropdown)
                logicalCurrentY+=labelHeightAboveComponent+selectedBoostingBotDropdown.height+interComponentSpacing
                boostingRequeueDelaySlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Boosting Requeue Delay",initialValue=Config.boostingRequeueDelay.toFloat(),minValue=50f,maxValue=5000f,step=50f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.boostingRequeueDelay=v.toInt()})
                tab.components.add(boostingRequeueDelaySlider)
                logicalCurrentY+=labelHeightAboveComponent+boostingRequeueDelaySlider.height
            }
            "Camera" -> {
                enableCustomCameraCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Enable Custom Camera",initialValue=Config.enableCustomCamera,onValueChanged={v->Config.enableCustomCamera=v;updateGuiElementStates()})
                tab.components.add(enableCustomCameraCheckbox)
                logicalCurrentY+=enableCustomCameraCheckbox.height+interComponentSpacing
                cameraOffsetXSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Offset X",initialValue=Config.cameraOffsetX,minValue=-10f,maxValue=10f,step=0.1f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.cameraOffsetX=v})
                tab.components.add(cameraOffsetXSlider)
                logicalCurrentY+=labelHeightAboveComponent+cameraOffsetXSlider.height+interComponentSpacing
                cameraOffsetYSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Offset Y",initialValue=Config.cameraOffsetY,minValue=-10f,maxValue=10f,step=0.1f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.cameraOffsetY=v})
                tab.components.add(cameraOffsetYSlider)
                logicalCurrentY+=labelHeightAboveComponent+cameraOffsetYSlider.height+interComponentSpacing
                cameraOffsetZSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Offset Z",initialValue=Config.cameraOffsetZ,minValue=-15f,maxValue=15f,step=0.1f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.cameraOffsetZ=v})
                tab.components.add(cameraOffsetZSlider)
                logicalCurrentY+=labelHeightAboveComponent+cameraOffsetZSlider.height+interComponentSpacing
                cameraPitchSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Pitch",initialValue=Config.cameraPitch,minValue=-90f,maxValue=90f,step=0.5f,displayFormat={v->"%.1f°".format(v)},onValueChanged={v->Config.cameraPitch=v})
                tab.components.add(cameraPitchSlider)
                logicalCurrentY+=labelHeightAboveComponent+cameraPitchSlider.height+interComponentSpacing
                cameraYawSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Yaw",initialValue=Config.cameraYaw,minValue=-180f,maxValue=180f,step=0.5f,displayFormat={v->"%.1f°".format(v)},onValueChanged={v->Config.cameraYaw=v})
                tab.components.add(cameraYawSlider)
                logicalCurrentY+=labelHeightAboveComponent+cameraYawSlider.height+interComponentSpacing
                enableCameraZoomCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Enable Camera Zoom",initialValue=Config.enableCameraZoom,onValueChanged={v->Config.enableCameraZoom=v;updateGuiElementStates()})
                tab.components.add(enableCameraZoomCheckbox)
                logicalCurrentY+=enableCameraZoomCheckbox.height+interComponentSpacing
                cameraZoomFovSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Zoom FOV",initialValue=Config.cameraZoomFovValue,minValue=10f,maxValue=90f,step=1f,displayFormat={v->"%.0f".format(v)},onValueChanged={v->Config.cameraZoomFovValue=v})
                tab.components.add(cameraZoomFovSlider)
                logicalCurrentY+=labelHeightAboveComponent+cameraZoomFovSlider.height
            }
            "Replays" -> {
                enableReplayClearingModeCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Enable Replay Clearing",initialValue=Config.enableReplayClearingMode,onValueChanged={v->Config.setEnableReplayClearingMode(v);updateGuiElementStates()})
                tab.components.add(enableReplayClearingModeCheckbox)
                logicalCurrentY+=enableReplayClearingModeCheckbox.height+interComponentSpacing
                replayClearingMinDelaySlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Min Delay",initialValue=Config.replayClearingMinDelay.toFloat(),minValue=500f,maxValue=20000f,step=100f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.replayClearingMinDelay=v.toInt()})
                tab.components.add(replayClearingMinDelaySlider)
                logicalCurrentY+=labelHeightAboveComponent+replayClearingMinDelaySlider.height+interComponentSpacing
                replayClearingMaxDelaySlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Max Delay",initialValue=Config.replayClearingMaxDelay.toFloat(),minValue=500f,maxValue=20000f,step=100f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.replayClearingMaxDelay=v.toInt()})
                tab.components.add(replayClearingMaxDelaySlider)
                logicalCurrentY+=labelHeightAboveComponent+replayClearingMaxDelaySlider.height+interComponentSpacing
                replayClearingCommandCountSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Command Count",initialValue=Config.replayClearingCommandCount.toFloat(),minValue=1f,maxValue=600f,step=1f,displayFormat={v->"%.0f".format(v)},onValueChanged={v->Config.replayClearingCommandCount=v.toInt()})
                tab.components.add(replayClearingCommandCountSlider)
                logicalCurrentY+=labelHeightAboveComponent+replayClearingCommandCountSlider.height
            }
            "Combat" -> {
                minCPSSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Min CPS",initialValue=Config.minCPS.toFloat(),minValue=1f,maxValue=20f,step=0.5f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.minCPS=v.roundToInt()})
                tab.components.add(minCPSSlider)
                logicalCurrentY+=labelHeightAboveComponent+minCPSSlider.height+interComponentSpacing
                maxCPSSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Max CPS",initialValue=Config.maxCPS.toFloat(),minValue=5f,maxValue=25f,step=0.5f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.maxCPS=v.roundToInt()})
                tab.components.add(maxCPSSlider)
                logicalCurrentY+=labelHeightAboveComponent+maxCPSSlider.height+interComponentSpacing
                lookSpeedHorizontalSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Horizontal Look Speed",initialValue=Config.lookSpeedHorizontal.toFloat(),minValue=1f,maxValue=30f,step=1f,displayFormat={v->"%.0f".format(v)},onValueChanged={v->Config.lookSpeedHorizontal=v.toInt()})
                tab.components.add(lookSpeedHorizontalSlider)
                logicalCurrentY+=labelHeightAboveComponent+lookSpeedHorizontalSlider.height+interComponentSpacing
                lookSpeedVerticalSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Vertical Look Speed",initialValue=Config.lookSpeedVertical.toFloat(),minValue=1f,maxValue=30f,step=1f,displayFormat={v->"%.0f".format(v)},onValueChanged={v->Config.lookSpeedVertical=v.toInt()})
                tab.components.add(lookSpeedVerticalSlider)
                logicalCurrentY+=labelHeightAboveComponent+lookSpeedVerticalSlider.height+interComponentSpacing
                lookRandSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Look Randomization",initialValue=Config.lookRand,minValue=0f,maxValue=5f,step=0.1f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.lookRand=v})
                tab.components.add(lookRandSlider)
                logicalCurrentY+=labelHeightAboveComponent+lookRandSlider.height+interComponentSpacing
                maxDistanceLookSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Max Look Distance",initialValue=Config.maxDistanceLook.toFloat(),minValue=3f,maxValue=150f,step=1f,displayFormat={v->"%.0f".format(v)},onValueChanged={v->Config.maxDistanceLook=v.toInt()})
                tab.components.add(maxDistanceLookSlider)
                logicalCurrentY+=labelHeightAboveComponent+maxDistanceLookSlider.height+interComponentSpacing
                maxDistanceAttackSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Max Attack Distance",initialValue=Config.maxDistanceAttack.toFloat(),minValue=3f,maxValue=8f,step=0.1f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.maxDistanceAttack=v.toInt()})
                tab.components.add(maxDistanceAttackSlider)
                logicalCurrentY+=labelHeightAboveComponent+maxDistanceAttackSlider.height+interComponentSpacing
                enableComboResetByDistanceCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Combo Reset by Distance",initialValue=Config.enableComboResetByDistance,onValueChanged={v->Config.enableComboResetByDistance=v;updateGuiElementStates()})
                tab.components.add(enableComboResetByDistanceCheckbox)
                logicalCurrentY+=enableComboResetByDistanceCheckbox.height+interComponentSpacing
                comboResetDistanceSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Combo Reset Distance",initialValue=Config.comboResetDistance.toFloat(),minValue=1f,maxValue=10f,step=0.1f,displayFormat={v->"%.1f".format(v)},onValueChanged={v->Config.comboResetDistance=v.toInt()})
                tab.components.add(comboResetDistanceSlider)
                logicalCurrentY+=labelHeightAboveComponent+comboResetDistanceSlider.height+interComponentSpacing
                enableSumoDistanceJumpCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Sumo: Distance Jump",initialValue=Config.enableSumoDistanceJump,onValueChanged={v->Config.enableSumoDistanceJump=v})
                tab.components.add(enableSumoDistanceJumpCheckbox)
                logicalCurrentY+=enableSumoDistanceJumpCheckbox.height+interComponentSpacing
                enableSumoStrafingCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Sumo: Strafing",initialValue=Config.enableSumoStrafing,onValueChanged={v->Config.enableSumoStrafing=v;updateGuiElementStates()})
                tab.components.add(enableSumoStrafingCheckbox)
                logicalCurrentY+=enableSumoStrafingCheckbox.height+interComponentSpacing
                sumoStrafeIntensityDropdown=Dropdown(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Sumo: Strafe Intensity",options=Config.SumoStrafeIntensity.options,initialSelectedIndex=Config.sumoStrafeIntensity.ordinal,onSelectionChanged={i,_->Config.sumoStrafeIntensity=Config.SumoStrafeIntensity.fromOrdinal(i)})
                tab.components.add(sumoStrafeIntensityDropdown)
                logicalCurrentY+=labelHeightAboveComponent+sumoStrafeIntensityDropdown.height+interComponentSpacing
                enableHitselectingCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Sumo: Enable Hitselecting",initialValue=Config.enableHitselecting,onValueChanged={v->Config.enableHitselecting=v;updateGuiElementStates()})
                tab.components.add(enableHitselectingCheckbox)
                logicalCurrentY+=enableHitselectingCheckbox.height+interComponentSpacing
                hitselectChanceSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: Chance",initialValue=Config.hitselectChance.toFloat(),minValue=0.0f,maxValue=1.0f,step=0.01f,displayFormat={v->"%.0f%%".format(v*100)},onValueChanged={v->Config.hitselectChance=v.toDouble()})
                tab.components.add(hitselectChanceSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectChanceSlider.height+interComponentSpacing
                hitselectMinActivationDistanceSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: Min Activation Dist.",initialValue=Config.hitselectMinActivationDistance.toFloat(),minValue=1.0f,maxValue=6.0f,step=0.1f,displayFormat={v->"%.1fb".format(v)},onValueChanged={v->Config.hitselectMinActivationDistance=v.toDouble()})
                tab.components.add(hitselectMinActivationDistanceSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectMinActivationDistanceSlider.height+interComponentSpacing
                hitselectMaxActivationDistanceSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: Max Activation Dist.",initialValue=Config.hitselectMaxActivationDistance.toFloat(),minValue=1.5f,maxValue=7.0f,step=0.1f,displayFormat={v->"%.1fb".format(v)},onValueChanged={v->Config.hitselectMaxActivationDistance=v.toDouble()})
                tab.components.add(hitselectMaxActivationDistanceSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectMaxActivationDistanceSlider.height+interComponentSpacing
                hitselectBaitDurationMinSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: Min Bait Duration",initialValue=Config.hitselectBaitDurationMin.toFloat(),minValue=30f,maxValue=500f,step=5f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.hitselectBaitDurationMin=v.toInt()})
                tab.components.add(hitselectBaitDurationMinSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectBaitDurationMinSlider.height+interComponentSpacing
                hitselectBaitDurationMaxSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: Max Bait Duration",initialValue=Config.hitselectBaitDurationMax.toFloat(),minValue=50f,maxValue=750f,step=5f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.hitselectBaitDurationMax=v.toInt()})
                tab.components.add(hitselectBaitDurationMaxSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectBaitDurationMaxSlider.height+interComponentSpacing
                hitselectCooldownSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: Cooldown",initialValue=Config.hitselectCooldown.toFloat(),minValue=500f,maxValue=10000f,step=100f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.hitselectCooldown=v.toInt()})
                tab.components.add(hitselectCooldownSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectCooldownSlider.height+interComponentSpacing
                hitselectStopSprintDuringBaitCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Hitselect: Stop Sprint for Bait",initialValue=Config.hitselectStopSprintDuringBait,onValueChanged={v->Config.hitselectStopSprintDuringBait=v})
                tab.components.add(hitselectStopSprintDuringBaitCheckbox)
                logicalCurrentY+=hitselectStopSprintDuringBaitCheckbox.height+interComponentSpacing
                hitselectSTapDuringBaitCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Hitselect: S-Tap for Bait",initialValue=Config.hitselectSTapDuringBait,onValueChanged={v->Config.hitselectSTapDuringBait=v;updateGuiElementStates()})
                tab.components.add(hitselectSTapDuringBaitCheckbox)
                logicalCurrentY+=hitselectSTapDuringBaitCheckbox.height+interComponentSpacing
                hitselectSTapDurationSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Hitselect: S-Tap Duration",initialValue=Config.hitselectSTapDuration.toFloat(),minValue=20f,maxValue=200f,step=5f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.hitselectSTapDuration=v.toInt()})
                tab.components.add(hitselectSTapDurationSlider)
                logicalCurrentY+=labelHeightAboveComponent+hitselectSTapDurationSlider.height
            }
            "Messages" -> {
                sendAutoGGCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Enable AutoGG",initialValue=Config.sendAutoGG,onValueChanged={v->Config.sendAutoGG=v;updateGuiElementStates()})
                tab.components.add(sendAutoGGCheckbox)
                logicalCurrentY+=sendAutoGGCheckbox.height+interComponentSpacing
                ggMessageTextField=Textfield(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="AutoGG Message",initialText=Config.ggMessage,onTextChanged={t->Config.ggMessage=t},onFocusChanged={f->if(!f)Config.save()})
                tab.components.add(ggMessageTextField)
                logicalCurrentY+=labelHeightAboveComponent+ggMessageTextField.height+interComponentSpacing
                ggDelaySlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="AutoGG Delay",initialValue=Config.ggDelay.toFloat(),minValue=0f,maxValue=2000f,step=50f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.ggDelay=v.toInt()})
                tab.components.add(ggDelaySlider)
                logicalCurrentY+=labelHeightAboveComponent+ggDelaySlider.height+interComponentSpacing
                sendStartMessageCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Game Start Message",initialValue=Config.sendStartMessage,onValueChanged={v->Config.sendStartMessage=v;updateGuiElementStates()})
                tab.components.add(sendStartMessageCheckbox)
                logicalCurrentY+=sendStartMessageCheckbox.height+interComponentSpacing
                startMessageTextField=Textfield(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Start Message",initialText=Config.startMessage,onTextChanged={t->Config.startMessage=t},onFocusChanged={f->if(!f)Config.save()})
                tab.components.add(startMessageTextField)
                logicalCurrentY+=labelHeightAboveComponent+startMessageTextField.height+interComponentSpacing
                startMessageDelaySlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Start Message Delay",initialValue=Config.startMessageDelay.toFloat(),minValue=0f,maxValue=2000f,step=50f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.startMessageDelay=v.toInt()})
                tab.components.add(startMessageDelaySlider)
                logicalCurrentY+=labelHeightAboveComponent+startMessageDelaySlider.height
            }
            "Requeue" -> {
                autoRqDelaySlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Requeue Delay",initialValue=Config.autoRqDelay.toFloat(),minValue=0f,maxValue=5000f,step=50f,displayFormat={v->"%.0f ms".format(v)},onValueChanged={v->Config.autoRqDelay=v.toInt()})
                tab.components.add(autoRqDelaySlider)
                logicalCurrentY+=labelHeightAboveComponent+autoRqDelaySlider.height+interComponentSpacing
                rqNoGameSlider=Slider(id=getNextId(),x=startX,y=logicalCurrentY+labelHeightAboveComponent,width=actualComponentWidth,label="Requeue No Game Timer",initialValue=Config.rqNoGame.toFloat(),minValue=5f,maxValue=120f,step=1f,displayFormat={v->"%.0f s".format(v)},onValueChanged={v->Config.rqNoGame=v.toInt()})
                tab.components.add(rqNoGameSlider)
                logicalCurrentY+=labelHeightAboveComponent+rqNoGameSlider.height+interComponentSpacing
                paperRequeueCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Paper Requeue",initialValue=Config.paperRequeue,onValueChanged={v->Config.paperRequeue=v})
                tab.components.add(paperRequeueCheckbox)
                logicalCurrentY+=paperRequeueCheckbox.height+interComponentSpacing
                fastRequeueCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Fast Requeue",initialValue=Config.fastRequeue,onValueChanged={v->Config.fastRequeue=v})
                tab.components.add(fastRequeueCheckbox)
                logicalCurrentY+=fastRequeueCheckbox.height
            }
            "Webhook" -> {
                sendWebhookMessagesCheckbox = Checkbox(id = getNextId(), x = startX, y = logicalCurrentY, label = "Enable Webhook", initialValue = Config.sendWebhookMessages, onValueChanged = { newValue -> Config.sendWebhookMessages = newValue; updateGuiElementStates() })
                tab.components.add(sendWebhookMessagesCheckbox)
                logicalCurrentY += sendWebhookMessagesCheckbox.height + interComponentSpacing
                webhookURLTextField = Textfield(id = getNextId(), x = startX, y = logicalCurrentY + labelHeightAboveComponent, width = actualComponentWidth, label = "Webhook URL", initialText = Config.webhookURL, onTextChanged = { newText -> Config.webhookURL = newText }, onFocusChanged = { isFocused -> if (!isFocused) Config.save() })
                tab.components.add(webhookURLTextField)
                logicalCurrentY += labelHeightAboveComponent + webhookURLTextField.height + interComponentSpacing
            }
            "Misc" -> {
                boxingFishCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Boxing Fish (Visual)",initialValue=Config.boxingFish,onValueChanged={v->Config.boxingFish=v})
                tab.components.add(boxingFishCheckbox)
                logicalCurrentY+=boxingFishCheckbox.height
            }
            "HUD" -> {
                sessionStatsHUDCheckbox=Checkbox(id=getNextId(),x=startX,y=logicalCurrentY,label="Session Stats HUD",initialValue=Config.sessionStatsHUD,onValueChanged={v->Config.sessionStatsHUD=v})
                tab.components.add(sessionStatsHUDCheckbox)
                logicalCurrentY+=sessionStatsHUDCheckbox.height
            }
        }
        tab.contentHeight = (logicalCurrentY - contentPaddingTopForComponents) + contentPaddingBottomForComponents
        calculateContentScrollingForTab(tab)
    }

    private fun calculateTabScrolling() {
        if (tabs.isEmpty()) return
        totalTabsWidthUnscrolled = tabs.sumOf { tabButtonWidth + tabButtonSpacing } - tabButtonSpacing
        val tabBarContainerWidth = panelWidth - (panelPadding * 2)
        val needsScrolling = totalTabsWidthUnscrolled > tabBarContainerWidth && tabs.size > 1
        visibleTabBarAreaWidth = if (needsScrolling) tabBarContainerWidth - (tabBarScrollButtonWidth * 2 + tabButtonSpacing * 2) else tabBarContainerWidth
        maxTabScrollX = max(0, totalTabsWidthUnscrolled - visibleTabBarAreaWidth)
        targetTabScrollX = targetTabScrollX.coerceIn(0, maxTabScrollX)
        tabScrollX = tabScrollX.coerceIn(0f, maxTabScrollX.toFloat())
    }

    private fun calculateContentScrollingForTab(tab: Tab) {
        val tabBarYOffset = panelY + topBarHeight
        val tabBarInternalHeight = tabBarButtonHeight + 8
        val contentAreaMarginTop = tabBarYOffset + tabBarInternalHeight
        val contentAreaDrawableHeight = (panelY + panelHeight - panelPadding) - contentAreaMarginTop
        tab.maxScrollY = max(0, tab.contentHeight - contentAreaDrawableHeight)
        tab.targetScrollY = tab.targetScrollY.coerceIn(0, tab.maxScrollY)
        tab.scrollY = tab.scrollY.coerceIn(0f, tab.maxScrollY.toFloat())
    }

    fun updateGuiElementStates() {
        if (tabs.isEmpty() || !::currentBotDropdown.isInitialized) return
        val isLobbyMovementEnabled=Config.lobbyMovement
        val isSumoBotActive=Config.currentBot==Config.sumoBotIndex&&!Config.enableBoostingMode&&!Config.enableReplayClearingMode
        val isBoostingEnabled=Config.enableBoostingMode
        val isReplayClearingEnabled=Config.enableReplayClearingMode
        val isCustomCameraEnabled=Config.enableCustomCamera
        val isCameraZoomEnabled=Config.enableCameraZoom&&isCustomCameraEnabled
        val isAutoGGEnabled=Config.sendAutoGG
        val isStartMessageEnabled=Config.sendStartMessage
        val isWebhookEnabled=Config.sendWebhookMessages
        val isComboResetEnabled=Config.enableComboResetByDistance
        val isSumoStrafingEnabled=Config.enableSumoStrafing
        val isHitselectingEnabled=Config.enableHitselecting
        val isDynamicBreaksEnabled=Config.enableDynamicBreaks
        tabs.forEach{tab->tab.components.forEach{c->c.enabled=true
            if(::currentBotDropdown.isInitialized&&c==currentBotDropdown)c.enabled=!isBoostingEnabled&&!isReplayClearingEnabled
            else if(::lobbyMovementTypeDropdown.isInitialized&&c==lobbyMovementTypeDropdown)c.enabled=isLobbyMovementEnabled
            else if(::playDurationHoursSlider.isInitialized&&c==playDurationHoursSlider)c.enabled=isDynamicBreaksEnabled
            else if(::breakDurationMinMinutesSlider.isInitialized&&c==breakDurationMinMinutesSlider)c.enabled=isDynamicBreaksEnabled
            else if(::breakDurationMaxMinutesSlider.isInitialized&&c==breakDurationMaxMinutesSlider)c.enabled=isDynamicBreaksEnabled
            else if(::enableSumoDistanceJumpCheckbox.isInitialized&&c==enableSumoDistanceJumpCheckbox)c.enabled=isSumoBotActive
            else if(::enableSumoStrafingCheckbox.isInitialized&&c==enableSumoStrafingCheckbox)c.enabled=isSumoBotActive
            else if(::sumoStrafeIntensityDropdown.isInitialized&&c==sumoStrafeIntensityDropdown)c.enabled=isSumoBotActive&&isSumoStrafingEnabled
            else if(::enableHitselectingCheckbox.isInitialized&&c==enableHitselectingCheckbox)c.enabled=isSumoBotActive
            else if(::hitselectChanceSlider.isInitialized&&c==hitselectChanceSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectMinActivationDistanceSlider.isInitialized&&c==hitselectMinActivationDistanceSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectMaxActivationDistanceSlider.isInitialized&&c==hitselectMaxActivationDistanceSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectBaitDurationMinSlider.isInitialized&&c==hitselectBaitDurationMinSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectBaitDurationMaxSlider.isInitialized&&c==hitselectBaitDurationMaxSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectCooldownSlider.isInitialized&&c==hitselectCooldownSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectStopSprintDuringBaitCheckbox.isInitialized&&c==hitselectStopSprintDuringBaitCheckbox)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectSTapDuringBaitCheckbox.isInitialized&&c==hitselectSTapDuringBaitCheckbox)c.enabled=isSumoBotActive&&isHitselectingEnabled
            else if(::hitselectSTapDurationSlider.isInitialized&&c==hitselectSTapDurationSlider)c.enabled=isSumoBotActive&&isHitselectingEnabled&&Config.hitselectSTapDuringBait
            else if(::selectedBoostingBotDropdown.isInitialized&&c==selectedBoostingBotDropdown)c.enabled=isBoostingEnabled
            else if(::boostingRequeueDelaySlider.isInitialized&&c==boostingRequeueDelaySlider)c.enabled=isBoostingEnabled
            else if(::cameraOffsetXSlider.isInitialized&&c==cameraOffsetXSlider)c.enabled=isCustomCameraEnabled
            else if(::cameraOffsetYSlider.isInitialized&&c==cameraOffsetYSlider)c.enabled=isCustomCameraEnabled
            else if(::cameraOffsetZSlider.isInitialized&&c==cameraOffsetZSlider)c.enabled=isCustomCameraEnabled
            else if(::cameraPitchSlider.isInitialized&&c==cameraPitchSlider)c.enabled=isCustomCameraEnabled
            else if(::cameraYawSlider.isInitialized&&c==cameraYawSlider)c.enabled=isCustomCameraEnabled
            else if(::enableCameraZoomCheckbox.isInitialized&&c==enableCameraZoomCheckbox)c.enabled=isCustomCameraEnabled
            else if(::cameraZoomFovSlider.isInitialized&&c==cameraZoomFovSlider)c.enabled=isCameraZoomEnabled
            else if(::replayClearingMinDelaySlider.isInitialized&&c==replayClearingMinDelaySlider)c.enabled=isReplayClearingEnabled
            else if(::replayClearingMaxDelaySlider.isInitialized&&c==replayClearingMaxDelaySlider)c.enabled=isReplayClearingEnabled
            else if(::replayClearingCommandCountSlider.isInitialized&&c==replayClearingCommandCountSlider)c.enabled=isReplayClearingEnabled
            else if(::comboResetDistanceSlider.isInitialized&&c==comboResetDistanceSlider)c.enabled=isComboResetEnabled
            else if(::ggMessageTextField.isInitialized&&c==ggMessageTextField)c.enabled=isAutoGGEnabled
            else if(::ggDelaySlider.isInitialized&&c==ggDelaySlider)c.enabled=isAutoGGEnabled
            else if(::startMessageTextField.isInitialized&&c==startMessageTextField)c.enabled=isStartMessageEnabled
            else if(::startMessageDelaySlider.isInitialized&&c==startMessageDelaySlider)c.enabled=isStartMessageEnabled
            else if(::webhookURLTextField.isInitialized&&c==webhookURLTextField)c.enabled=isWebhookEnabled
        }}
        if (tabs.isNotEmpty()) calculateContentScrollingForTab(currentTab())
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val activeTab = currentTab()
        if (activeTab.id != "error_no_tabs") {
            if (!isDraggingContentScrollbar) {
                val scrollYDiff = activeTab.targetScrollY - activeTab.scrollY
                if (abs(scrollYDiff) > 0.1f) activeTab.scrollY += scrollYDiff * SCROLL_SMOOTHING_FACTOR else activeTab.scrollY = activeTab.targetScrollY.toFloat()
            }
            val tabScrollXDiff = targetTabScrollX - tabScrollX
            if (abs(tabScrollXDiff) > 0.1f) tabScrollX += tabScrollXDiff * SCROLL_SMOOTHING_FACTOR else tabScrollX = targetTabScrollX.toFloat()
        }

        drawRect(0, 0, this.width, this.height, Color(0, 0, 0, 170).rgb)

        drawRoundedRectUsingGL(panelX.toFloat(), panelY.toFloat(), panelWidth.toFloat(), panelHeight.toFloat(), panelCornerRadius, GuiColors.SCREEN_BACKGROUND)
        drawRoundedRectUsingGL(panelX.toFloat(), panelY.toFloat(), panelWidth.toFloat(), topBarHeight.toFloat(), panelCornerRadius, GuiColors.TITLE_BAR_BACKGROUND)
        drawRect((panelX + panelCornerRadius).toInt(), (panelY + topBarHeight - panelCornerRadius).toInt(), (panelX + panelWidth - panelCornerRadius).toInt(), panelY + topBarHeight, GuiColors.TITLE_BAR_BACKGROUND)

        drawCenteredString(fontRendererObj, guiTitle, this.width / 2, panelY + (topBarHeight - fontRendererObj.FONT_HEIGHT) / 2, GuiColors.TITLE_BAR_TEXT)
        val closeX = panelX + panelWidth - closeButtonSize - 10
        val closeY = panelY + (topBarHeight - closeButtonSize) / 2
        isCloseButtonHovered = mouseX >= closeX && mouseX <= closeX + closeButtonSize && mouseY >= closeY && mouseY <= closeY + closeButtonSize
        val closeColor = if (isCloseButtonHovered) Color(200, 50, 50, 220).rgb else Color(80, 80, 80, 180).rgb
        drawRoundedRectUsingGL(closeX.toFloat(), closeY.toFloat(), closeButtonSize.toFloat(), closeButtonSize.toFloat(), 3f, closeColor)
        drawCenteredString(fontRendererObj, "✕", closeX + closeButtonSize / 2, closeY + (closeButtonSize - fontRendererObj.FONT_HEIGHT) / 2 + 1, Color.WHITE.rgb)

        val tabBarYOffset = panelY + topBarHeight
        val tabBarInternalHeight = tabBarButtonHeight + 8
        drawRect(panelX, tabBarYOffset, panelX + panelWidth, tabBarYOffset + tabBarInternalHeight, GuiColors.TAB_BAR_BACKGROUND)
        Gui.drawRect(panelX, tabBarYOffset, panelX + panelWidth, tabBarYOffset + 1, GuiColors.TITLE_BAR_SEPARATOR)

        val tabsAreaX = panelX + panelPadding
        val tabsAreaWidth = panelWidth - panelPadding * 2
        var tabsViewportStartX = tabsAreaX
        var localVisibleTabBarAreaWidth = tabsAreaWidth
        val needsTabBarScrollButtons = totalTabsWidthUnscrolled > tabsAreaWidth

        if (needsTabBarScrollButtons) {
            val buttonY = tabBarYOffset + (tabBarInternalHeight - tabBarButtonHeight) / 2
            tabsViewportStartX += tabBarScrollButtonWidth + tabButtonSpacing
            localVisibleTabBarAreaWidth -= (tabBarScrollButtonWidth * 2 + tabButtonSpacing * 2)

            val scrollLeftX = tabsAreaX
            val scrollLeftHover = mouseX >= scrollLeftX && mouseX < scrollLeftX + tabBarScrollButtonWidth && mouseY >= buttonY && mouseY < buttonY + tabBarButtonHeight
            drawRoundedRectWithBorderUsingGL(scrollLeftX.toFloat(), buttonY.toFloat(), tabBarScrollButtonWidth.toFloat(), tabBarButtonHeight.toFloat(), 2f, if(scrollLeftHover) GuiColors.TAB_SCROLL_BUTTON_HOVER_BG else GuiColors.TAB_SCROLL_BUTTON_BG, GuiColors.COMPONENT_BORDER, 1f)
            drawCenteredString(fontRendererObj, "<", scrollLeftX + tabBarScrollButtonWidth / 2, buttonY + (tabBarButtonHeight - fontRendererObj.FONT_HEIGHT) / 2, if (tabScrollX > 0f) GuiColors.TAB_SCROLL_BUTTON_ARROW else GuiColors.TEXT_DISABLED)

            val scrollRightX = tabsAreaX + tabsAreaWidth - tabBarScrollButtonWidth
            val scrollRightHover = mouseX >= scrollRightX && mouseX < scrollRightX + tabBarScrollButtonWidth && mouseY >= buttonY && mouseY < buttonY + tabBarButtonHeight
            drawRoundedRectWithBorderUsingGL(scrollRightX.toFloat(), buttonY.toFloat(), tabBarScrollButtonWidth.toFloat(), tabBarButtonHeight.toFloat(), 2f, if(scrollRightHover) GuiColors.TAB_SCROLL_BUTTON_HOVER_BG else GuiColors.TAB_SCROLL_BUTTON_BG, GuiColors.COMPONENT_BORDER, 1f)
            drawCenteredString(fontRendererObj, ">", scrollRightX + tabBarScrollButtonWidth / 2, buttonY + (tabBarButtonHeight - fontRendererObj.FONT_HEIGHT) / 2, if (tabScrollX < maxTabScrollX) GuiColors.TAB_SCROLL_BUTTON_ARROW else GuiColors.TEXT_DISABLED)
        }

        val tabButtonVisualY = tabBarYOffset + (tabBarInternalHeight - tabBarButtonHeight) / 2
        startScissor(tabsViewportStartX, tabButtonVisualY, localVisibleTabBarAreaWidth, tabBarButtonHeight)
        var currentTabButtonVisualX = tabsViewportStartX - tabScrollX
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == currentTabIndex
            val tabHovered = mouseX >= currentTabButtonVisualX && mouseX < currentTabButtonVisualX + tabButtonWidth && mouseY >= tabButtonVisualY && mouseY < tabButtonVisualY + tabBarButtonHeight && mouseX >= tabsViewportStartX && mouseX < tabsViewportStartX + localVisibleTabBarAreaWidth
            val tabBgColor = when { isSelected -> GuiColors.TAB_BUTTON_BACKGROUND_ACTIVE; tabHovered -> GuiColors.TAB_BUTTON_BACKGROUND_HOVER; else -> GuiColors.TAB_BUTTON_BACKGROUND_INACTIVE }
            val textColor = when { isSelected -> GuiColors.TAB_BUTTON_TEXT_ACTIVE; tabHovered -> GuiColors.TAB_BUTTON_TEXT_HOVER; else -> GuiColors.TAB_BUTTON_TEXT_INACTIVE }

            drawRoundedRectWithBorderUsingGL(currentTabButtonVisualX.toFloat(), tabButtonVisualY.toFloat(), tabButtonWidth.toFloat(), tabBarButtonHeight.toFloat(), 3f, tabBgColor, GuiColors.TAB_BAR_BORDER, 1f)
            if (isSelected) Gui.drawRect(currentTabButtonVisualX.toInt() + 3, tabButtonVisualY + tabBarButtonHeight - 2, currentTabButtonVisualX.toInt() + tabButtonWidth - 3, tabButtonVisualY + tabBarButtonHeight - 1, GuiColors.PRIMARY_BLUE_BRIGHT)
            drawCenteredString(fontRendererObj, tab.name, currentTabButtonVisualX.toInt() + tabButtonWidth / 2, tabButtonVisualY + (tabBarButtonHeight - fontRendererObj.FONT_HEIGHT) / 2, textColor)
            currentTabButtonVisualX += tabButtonWidth + tabButtonSpacing
        }
        stopScissor()

        val contentAreaVisualTop = tabBarYOffset + tabBarInternalHeight
        val contentAreaVisualBottom = panelY + panelHeight - panelPadding
        val contentAreaDrawableHeight = contentAreaVisualBottom - contentAreaVisualTop

        Gui.drawRect(panelX, contentAreaVisualTop, panelX + panelWidth, contentAreaVisualTop + 1, GuiColors.TITLE_BAR_SEPARATOR)
        drawRoundedRectWithBorderUsingGL(
            (panelX + panelPadding).toFloat(), (contentAreaVisualTop + panelPadding).toFloat(),
            (panelWidth - panelPadding * 2).toFloat(), (contentAreaDrawableHeight - panelPadding * 2).toFloat(),
            3f, GuiColors.MODERN_SECONDARY_BACKGROUND, GuiColors.COMPONENT_BORDER, 1f
        )

        val contentAreaX = panelX + panelPadding + 1
        val contentAreaY = contentAreaVisualTop + panelPadding + 1
        val contentAreaHeight = contentAreaDrawableHeight - panelPadding * 2 - 2
        var contentAreaWidth = panelWidth - panelPadding * 2 - 2
        if (activeTab.maxScrollY > 0) contentAreaWidth -= (scrollbarWidth + scrollbarMargin)

        startScissor(contentAreaX, contentAreaY, contentAreaWidth, contentAreaHeight)

        if (activeTab.id != "error_no_tabs") {
            activeTab.components.forEach { component ->
                val originalLogicalY = component.y
                val componentScreenY = contentAreaY + originalLogicalY - activeTab.scrollY.toInt()
                if (componentScreenY + component.height >= contentAreaY && componentScreenY <= contentAreaY + contentAreaHeight) {
                    component.y = componentScreenY
                    if (!(component is Dropdown && component.isOpen)) {
                        component.drawComponent(mouseX, mouseY, partialTicks)
                    }
                    component.y = originalLogicalY
                }
            }
        }
        stopScissor()

        if (activeTab.maxScrollY > 0) {
            val scrollBarActualX = contentAreaX + contentAreaWidth + scrollbarMargin
            val scrollBarTrackY = contentAreaY
            val scrollBarTrackHeight = contentAreaHeight
            drawRoundedRectUsingGL(scrollBarActualX.toFloat(), scrollBarTrackY.toFloat(), scrollbarWidth.toFloat(), scrollBarTrackHeight.toFloat(), 3f, GuiColors.SCROLLBAR_BG)

            val thumbHeightRatio = (contentAreaHeight.toFloat() / activeTab.contentHeight.toFloat()).coerceIn(0.05f, 1f)
            val thumbHeight = max(20, (scrollBarTrackHeight * thumbHeightRatio).toInt())
            val thumbYRatio = if (activeTab.maxScrollY > 0) activeTab.scrollY / activeTab.maxScrollY.toFloat() else 0f
            val thumbYPos = scrollBarTrackY + ((scrollBarTrackHeight - thumbHeight) * thumbYRatio).toInt()
            val thumbHovered = (mouseX >= scrollBarActualX && mouseX < scrollBarActualX + scrollbarWidth && mouseY >= thumbYPos && mouseY < thumbYPos + thumbHeight) || isDraggingContentScrollbar

            drawRoundedRectUsingGL((scrollBarActualX + 1f), thumbYPos.toFloat().coerceIn(scrollBarTrackY.toFloat(), (scrollBarTrackY + scrollBarTrackHeight - thumbHeight).toFloat()), (scrollbarWidth - 2f), thumbHeight.toFloat(), 3f,
                if (thumbHovered) GuiColors.MODERN_SCROLLBAR_THUMB_HOVER else GuiColors.SCROLLBAR_THUMB)
        }

        openDropdown?.let { dd ->
            val originalLogicalY_dd = dd.y
            dd.y = contentAreaY + originalLogicalY_dd - activeTab.scrollY.toInt()
            dd.drawComponent(mouseX, mouseY, partialTicks)
            dd.y = originalLogicalY_dd
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        val closeX = panelX + panelWidth - closeButtonSize - 10
        val closeY = panelY + (topBarHeight - closeButtonSize) / 2
        if (mouseX >= closeX && mouseX <= closeX + closeButtonSize && mouseY >= closeY && mouseY <= closeY + closeButtonSize) {
            this.mc.displayGuiScreen(null)
            return
        }

        val tabBarYOffset = panelY + topBarHeight
        val tabBarInternalHeight = tabBarButtonHeight + 8
        val tabsAreaX = panelX + panelPadding
        val tabsAreaWidth = panelWidth - panelPadding * 2
        var tabsViewportStartX = tabsAreaX
        val needsTabBarScrollButtons = totalTabsWidthUnscrolled > tabsAreaWidth

        if (needsTabBarScrollButtons) {
            val buttonY = tabBarYOffset + (tabBarInternalHeight - tabBarButtonHeight) / 2
            val scrollLeftX = tabsAreaX
            if (mouseX >= scrollLeftX && mouseX < scrollLeftX + tabBarScrollButtonWidth && mouseY >= buttonY && mouseY < buttonY + tabBarButtonHeight) {
                targetTabScrollX = max(0, targetTabScrollX - (tabButtonWidth + tabButtonSpacing))
                return
            }
            val scrollRightX = tabsAreaX + tabsAreaWidth - tabBarScrollButtonWidth
            if (mouseX >= scrollRightX && mouseX < scrollRightX + tabBarScrollButtonWidth && mouseY >= buttonY && mouseY < buttonY + tabBarButtonHeight) {
                targetTabScrollX = min(maxTabScrollX, targetTabScrollX + (tabButtonWidth + tabButtonSpacing))
                return
            }
            tabsViewportStartX += tabBarScrollButtonWidth + tabButtonSpacing
        }

        val tabButtonVisualY = tabBarYOffset + (tabBarInternalHeight - tabBarButtonHeight) / 2
        val actualClickableTabBarWidth = if (needsTabBarScrollButtons) tabsAreaWidth - (tabBarScrollButtonWidth * 2 + tabButtonSpacing * 2) else tabsAreaWidth
        var currentTabButtonVisualX = tabsViewportStartX - tabScrollX.toInt()
        tabs.forEachIndexed { index, tab ->
            if (mouseX >= currentTabButtonVisualX && mouseX < currentTabButtonVisualX + tabButtonWidth && mouseY >= tabButtonVisualY && mouseY < tabButtonVisualY + tabBarButtonHeight && mouseX >= tabsViewportStartX && mouseX < tabsViewportStartX + actualClickableTabBarWidth) {
                if (currentTabIndex != index) {
                    currentTab().components.forEach { if (it is Dropdown) it.close(); if (it is Textfield) it.setFocused(false) }
                    openDropdown = null; isDraggingContentScrollbar = false
                    currentTabIndex = index
                    currentTab().targetScrollY = 0; currentTab().scrollY = 0f
                    updateGuiElementStates()
                }
                return
            }
            currentTabButtonVisualX += tabButtonWidth + tabButtonSpacing
        }

        val activeTab = currentTab()
        if (activeTab.id == "error_no_tabs") return

        val contentAreaVisualTop = tabBarYOffset + tabBarInternalHeight
        val contentAreaX = panelX + panelPadding + 1
        val contentAreaY = contentAreaVisualTop + panelPadding + 1
        var contentAreaWidth = panelWidth - panelPadding * 2 - 2

        if (this.openDropdown != null) {
            val dd = this.openDropdown!!
            val oY = dd.y
            dd.y = contentAreaY + oY - activeTab.scrollY.toInt()
            if (dd.mouseClicked(mouseX, mouseY, mouseButton)) {
                dd.y = oY
                if (!dd.isOpen) this.openDropdown = null
                return
            }
            dd.y = oY
            val listH = if (dd.isOpen) dd.options.take(dd.maxDisplayableOptions).size * dd.optionHeight else 0
            val clickInside = mouseX >= dd.x && mouseX < dd.x + dd.width && mouseY >= dd.y && mouseY < dd.y + dd.height + listH
            if (!clickInside) { dd.close(); this.openDropdown = null } else { return }
        }

        var clickedComponent = false
        if (mouseX > contentAreaX && mouseX < contentAreaX + contentAreaWidth) {
            for (component in activeTab.components.asReversed()) {
                val oY = component.y
                component.y = contentAreaY + oY - activeTab.scrollY.toInt()
                if (component.mouseClicked(mouseX, mouseY, mouseButton)) {
                    if (component is Dropdown) { if (component.isOpen) this.openDropdown = component }
                    else if (component is Textfield) { activeTab.components.filterIsInstance<Textfield>().filter { it != component }.forEach { it.setFocused(false) } }
                    clickedComponent = true
                }
                component.y = oY
                if (clickedComponent) break
            }
        }

        if (activeTab.maxScrollY > 0 && mouseX >= contentAreaX + contentAreaWidth + scrollbarMargin) {
            isDraggingContentScrollbar = true
            contentScrollbarMouseDragStartY = mouseY.toFloat()
            contentScrollbarInitialScrollY = activeTab.scrollY
        }

        if (!clickedComponent) { activeTab.components.filterIsInstance<Textfield>().forEach { it.setFocused(false) } }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) isDraggingContentScrollbar = false
        super.mouseReleased(mouseX, mouseY, state)
        val activeTab = currentTab()
        val contentAreaY = panelY + topBarHeight + (tabBarButtonHeight + 8) + panelPadding + 1
        activeTab.components.forEach { val oY = it.y; it.y = contentAreaY + oY - activeTab.scrollY.toInt(); it.mouseReleased(mouseX, mouseY, state); it.y = oY }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDraggingContentScrollbar) {
            val activeTab = currentTab()
            val contentAreaY = panelY + topBarHeight + (tabBarButtonHeight + 8) + panelPadding + 1
            val contentAreaH = (panelY + panelHeight - panelPadding) - contentAreaY
            val thumbH = max(20, (contentAreaH.toFloat() / activeTab.contentHeight.toFloat() * contentAreaH).toInt())
            val scrollablePixelRange = contentAreaH - thumbH

            val deltaY = mouseY - contentScrollbarMouseDragStartY
            val scrollChange = deltaY * (activeTab.maxScrollY.toFloat() / scrollablePixelRange.toFloat())
            activeTab.scrollY = (contentScrollbarInitialScrollY + scrollChange).coerceIn(0f, activeTab.maxScrollY.toFloat())
            activeTab.targetScrollY = activeTab.scrollY.roundToInt()
            return
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        val activeTab = currentTab()
        val contentAreaY = panelY + topBarHeight + (tabBarButtonHeight + 8) + panelPadding + 1
        activeTab.components.filterIsInstance<Slider>().forEach { val oY = it.y; it.y = contentAreaY + oY - activeTab.scrollY.toInt(); it.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick); it.y = oY }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val dWheel = Mouse.getDWheel()
        if (dWheel != 0) {
            val rawMouseX = Mouse.getEventX() * this.width / this.mc.displayWidth
            val rawMouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1

            if (rawMouseX > panelX && rawMouseX < panelX + panelWidth && rawMouseY > panelY && rawMouseY < panelY + panelHeight) {
                val activeTab = currentTab()
                val scrollAmount = if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) 90 else 45
                activeTab.targetScrollY = (activeTab.targetScrollY - dWheel / 120 * scrollAmount).coerceIn(0, activeTab.maxScrollY)
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == mc.gameSettings.keyBindInventory.keyCode) {
            if (openDropdown != null) { openDropdown?.close(); openDropdown = null; return }
            this.mc.displayGuiScreen(null)
            return
        }
        currentTab().components.forEach { if (it.keyTyped(typedChar, keyCode)) return }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        Keyboard.enableRepeatEvents(false)
        Config.save()
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun startScissor(x: Int, y: Int, width: Int, height: Int) {
        val sr = ScaledResolution(mc)
        val scale = sr.scaleFactor
        if (width <= 0 || height <= 0) return
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor((x * scale), (sr.scaledHeight * scale) - ((y + height) * scale), (width * scale), (height * scale))
    }

    private fun stopScissor() { GL11.glDisable(GL11.GL_SCISSOR_TEST) }

    private fun drawRoundedRectUsingGL(x: Float, y: Float, width: Float, height: Float, radius: Float, colorInt: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        val awtColor = Color(colorInt, true)
        GlStateManager.color(awtColor.red / 255.0f, awtColor.green / 255.0f, awtColor.blue / 255.0f, awtColor.alpha / 255.0f)

        GL11.glBegin(GL11.GL_POLYGON)
        val segments = 20
        val pi = Math.PI.toFloat()
        for (i in 0..segments) { val angle = (i.toFloat() / segments) * (pi / 2f); GL11.glVertex2f(x + width - radius + cos(angle) * radius, y + height - radius + sin(angle) * radius) }
        for (i in 0..segments) { val angle = (pi / 2f) + (i.toFloat() / segments) * (pi / 2f); GL11.glVertex2f(x + radius + cos(angle) * radius, y + height - radius + sin(angle) * radius) }
        for (i in 0..segments) { val angle = pi + (i.toFloat() / segments) * (pi / 2f); GL11.glVertex2f(x + radius + cos(angle) * radius, y + radius + sin(angle) * radius) }
        for (i in 0..segments) { val angle = (1.5f * pi) + (i.toFloat() / segments) * (pi / 2f); GL11.glVertex2f(x + width - radius + cos(angle) * radius, y + radius + sin(angle) * radius) }
        GL11.glEnd()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun drawRoundedRectWithBorderUsingGL(x: Float, y: Float, width: Float, height: Float, radius: Float, bgColor: Int, borderColor: Int, borderWidth: Float) {
        drawRoundedRectUsingGL(x, y, width, height, radius, borderColor)
        drawRoundedRectUsingGL(x + borderWidth, y + borderWidth, width - borderWidth * 2, height - borderWidth * 2, (radius - borderWidth).coerceAtLeast(0f), bgColor)
    }
}