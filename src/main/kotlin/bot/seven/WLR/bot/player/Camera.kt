package bot.seven.WLR.bot.player

import bot.seven.WLR.bot.StateManager
import bot.seven.WLR.core.Config
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.jetbrains.annotations.NotNull

object Camera {

    private var isManuallyEnabled: Boolean = false
    private var prevConfigCustomCameraEnabledState: Boolean = Config.enableCustomCamera
    private var originalFov: Float? = null

    fun enable() {
        if (Config.enableCustomCamera) {
            isManuallyEnabled = true
            val mc = Minecraft.getMinecraft()
            if (mc.thePlayer != null && StateManager.state != StateManager.States.LOBBY) {
                if (mc.gameSettings.thirdPersonView != 1) {
                    mc.gameSettings.thirdPersonView = 1
                }
            }
        } else {
            isManuallyEnabled = false
        }
    }

    fun disable() {
        isManuallyEnabled = false
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) {
            return
        }

        val mc = Minecraft.getMinecraft()
        if (mc.thePlayer == null) {
            if (isManuallyEnabled) {
                isManuallyEnabled = false
            }
            restoreOriginalFovAndClearIfNecessary(mc)
            prevConfigCustomCameraEnabledState = Config.enableCustomCamera
            return
        }

        val currentConfigEnableCustomCamera = Config.enableCustomCamera

        if (StateManager.state == StateManager.States.LOBBY) {
            if (mc.gameSettings.thirdPersonView != 0) {
                mc.gameSettings.thirdPersonView = 0
            }
            if (isManuallyEnabled) {
                isManuallyEnabled = false
            }
        } else {
            if (currentConfigEnableCustomCamera) {
                if (isManuallyEnabled) {
                    if (mc.gameSettings.thirdPersonView != 1) {
                        mc.gameSettings.thirdPersonView = 1
                    }
                } else {
                    if (mc.gameSettings.thirdPersonView != 0) {
                        mc.gameSettings.thirdPersonView = 0
                    }
                }
            } else {
                if (mc.gameSettings.thirdPersonView != 0) {
                    mc.gameSettings.thirdPersonView = 0
                }
                if (isManuallyEnabled) {
                    isManuallyEnabled = false
                }
            }
        }

        if (currentConfigEnableCustomCamera != prevConfigCustomCameraEnabledState) {
            if (currentConfigEnableCustomCamera) {
                if (isManuallyEnabled && StateManager.state != StateManager.States.LOBBY) {
                    if (mc.gameSettings.thirdPersonView != 1) {
                        mc.gameSettings.thirdPersonView = 1
                    }
                }
            } else {
                if (isManuallyEnabled) {
                    isManuallyEnabled = false
                }
                if (mc.gameSettings.thirdPersonView != 0) {
                    mc.gameSettings.thirdPersonView = 0
                }
            }
            prevConfigCustomCameraEnabledState = currentConfigEnableCustomCamera
        }

        val customCameraIsEffectivelyOn = currentConfigEnableCustomCamera && isManuallyEnabled && StateManager.state != StateManager.States.LOBBY

        if (customCameraIsEffectivelyOn && Config.enableCameraZoom) {
            if (originalFov == null) {
                originalFov = mc.gameSettings.fovSetting
            }
            if (mc.gameSettings.fovSetting != Config.cameraZoomFovValue) {
                mc.gameSettings.fovSetting = Config.cameraZoomFovValue
            }
        } else {
            restoreOriginalFovAndClearIfNecessary(mc)
        }
    }

    private fun restoreOriginalFovAndClearIfNecessary(mc: Minecraft) {
        if (originalFov != null) {
            if (mc.gameSettings != null && mc.gameSettings.fovSetting != originalFov) {
                mc.gameSettings.fovSetting = originalFov!!
            }
            originalFov = null
        }
    }

    @SubscribeEvent
    fun onCameraSetup(@NotNull event: CameraSetup) {
        if (!Config.enableCustomCamera || !isManuallyEnabled || StateManager.state == StateManager.States.LOBBY) {
            return
        }

        val translateX = -Config.cameraOffsetX
        val translateY = -Config.cameraOffsetY
        val translateZ = -Config.cameraOffsetZ

        GlStateManager.translate(translateX, translateY, translateZ)

        event.pitch = Config.cameraPitch
        event.yaw = Config.cameraYaw
    }
}