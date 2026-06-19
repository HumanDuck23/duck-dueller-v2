package bot.seven.WLR.gui.elements

import bot.seven.WLR.gui.GuiColors
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

class Slider(
    id: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int = MODERN_SLIDER_HEIGHT,
    label: String,
    initialValue: Float,
    private val minValue: Float,
    private val maxValue: Float,
    private val step: Float = 0.1f,
    private val displayFormat: (Float) -> String = { "%.2f".format(it) },
    val onValueChanged: (Float) -> Unit
) : GuiComponentBase(id, x, y, width, height, label) {

    private var currentValue: Float = initialValue
    private var isDragging: Boolean = false

    private val knobVisualRadius: Float = 6f
    private val trackHeightToUse: Float = MODERN_SLIDER_TRACK_HEIGHT
    private val trackCornerRadius = trackHeightToUse / 2f

    private var visualKnobCenterX: Float
    private var targetKnobCenterX: Float
    private val KNOB_SMOOTH_FACTOR = 0.25f

    init {
        internalSetValue(initialValue, false)
        targetKnobCenterX = calculateKnobCenterX(this.currentValue)
        visualKnobCenterX = targetKnobCenterX
    }

    private fun calculateKnobCenterX(value: Float): Float {
        val progress = if (maxValue - minValue == 0f) 0f else (value - minValue) / (maxValue - minValue)
        val travelWidth = this.width - (2 * knobVisualRadius)
        return this.x + knobVisualRadius + (if (travelWidth > 0) travelWidth * progress else 0f)
    }

    override fun drawComponent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawComponent(mouseX, mouseY, partialTicks)
        if (!visible) return

        val diff = targetKnobCenterX - visualKnobCenterX
        visualKnobCenterX = if (abs(diff) > 0.001f) visualKnobCenterX + diff * KNOB_SMOOTH_FACTOR else targetKnobCenterX

        val currentKnobRenderCenterX = visualKnobCenterX.coerceIn(x + knobVisualRadius, x + width - knobVisualRadius)
        val knobRenderY = y + height / 2f

        drawTopLabel(yOffset = -3)
        val valueText = displayFormat(currentValue)
        val valueColor = if (enabled) GuiColors.TEXT_ACCENT else GuiColors.TEXT_DISABLED
        val valueTextWidth = fontRenderer.getStringWidth(valueText)
        fontRenderer.drawString(
            valueText,
            x + width - valueTextWidth,
            y - fontRenderer.FONT_HEIGHT - 7,
            valueColor
        )

        val trackActualY = y + (height - trackHeightToUse) / 2f
        val trackColor = if (enabled) GuiColors.SLIDER_TRACK else GuiColors.COMPONENT_BACKGROUND_DISABLED
        drawRoundedRectUsingGL(
            x.toFloat(), trackActualY, width.toFloat(), trackHeightToUse,
            trackCornerRadius, trackColor
        )

        val filledWidth = currentKnobRenderCenterX - x
        if (filledWidth > 0f) {
            val filledTrackColor = if (enabled) GuiColors.SLIDER_TRACK_FILLED else GuiColors.PRIMARY_BLUE_DARK
            drawRoundedRectUsingGL(
                x.toFloat(), trackActualY, filledWidth.coerceAtMost(width.toFloat()), trackHeightToUse,
                trackCornerRadius, filledTrackColor
            )
        }

        if (knobVisualRadius <= 0f) return

        val isHoveringKnob = mouseX >= currentKnobRenderCenterX - knobVisualRadius &&
                mouseX <= currentKnobRenderCenterX + knobVisualRadius &&
                mouseY >= knobRenderY - knobVisualRadius &&
                mouseY <= knobRenderY + knobVisualRadius

        val knobColor = when {
            !enabled -> GuiColors.PRIMARY_BLUE_DARK
            isDragging || isHoveringKnob -> GuiColors.PRIMARY_BLUE_BRIGHT
            else -> GuiColors.PRIMARY_BLUE
        }

        drawCircleUsingGL(currentKnobRenderCenterX, knobRenderY, knobVisualRadius, knobColor)
    }

    private fun internalSetValue(newValue: Float, notify: Boolean) {
        val oldValue = this.currentValue
        var tempValue = newValue.coerceIn(minValue, maxValue)
        if (step > 0f) {
            val decimalPlaces = getDecimalPlaces(step)
            tempValue = (tempValue / step).roundToInt() * step
            tempValue = String.format("%.${decimalPlaces}f", tempValue).replace(',', '.').toFloat()
        }
        this.currentValue = tempValue.coerceIn(minValue, maxValue)
        this.targetKnobCenterX = calculateKnobCenterX(this.currentValue)
        if (notify && abs(oldValue - this.currentValue) > (step / 2.0f).coerceAtMost(0.00001f)) {
            onValueChanged(this.currentValue)
        }
    }

    fun setValue(newValue: Float) {
        internalSetValue(newValue, true)
        visualKnobCenterX = calculateKnobCenterX(this.currentValue)
        targetKnobCenterX = visualKnobCenterX
    }

    private fun getDecimalPlaces(value: Float): Int {
        val s = value.toString().replace(',', '.')
        val dot = s.indexOf('.')
        return if (dot < 0) 0 else s.length - dot - 1
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!enabled || !visible || !super.mouseClicked(mouseX, mouseY, mouseButton)) return false

        if (mouseButton == 0) {
            isDragging = true
            updateValueFromMouse(mouseX, true)
            visualKnobCenterX = calculateKnobCenterX(this.currentValue)
            targetKnobCenterX = visualKnobCenterX
            mc.soundHandler.playSound(net.minecraft.client.audio.PositionedSoundRecord.create(net.minecraft.util.ResourceLocation("gui.button.press"), 0.6F))
            return true
        }
        return false
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDragging && clickedMouseButton == 0 && enabled) {
            updateValueFromMouse(mouseX, true)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0 && isDragging) {
            isDragging = false
        }
    }

    private fun updateValueFromMouse(mouseX: Int, notify: Boolean) {
        if (!enabled) return
        val travelWidth = this.width - (2 * knobVisualRadius)
        if (travelWidth <= 0f) return
        val relativeMouseX = mouseX - (this.x + knobVisualRadius)
        val ratio = (relativeMouseX / travelWidth).coerceIn(0f, 1f)
        val newValue = minValue + (maxValue - minValue) * ratio
        internalSetValue(newValue, notify)
    }

    private fun drawRoundedRectUsingGL(x: Float, y: Float, width: Float, height: Float, radius: Float, colorInt: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        val awtColor = Color(colorInt, true)
        GlStateManager.color(
            awtColor.red / 255.0f,
            awtColor.green / 255.0f,
            awtColor.blue / 255.0f,
            awtColor.alpha / 255.0f
        )

        GL11.glBegin(GL11.GL_POLYGON)
        val segments = 20
        val pi = Math.PI.toFloat()
        for (i in 0..segments) {
            val angle = (i.toFloat() / segments) * (pi / 2f)
            GL11.glVertex2f(x + width - radius + cos(angle) * radius, y + height - radius + sin(angle) * radius)
        }
        for (i in 0..segments) {
            val angle = (pi / 2f) + (i.toFloat() / segments) * (pi / 2f)
            GL11.glVertex2f(x + radius + cos(angle) * radius, y + height - radius + sin(angle) * radius)
        }
        for (i in 0..segments) {
            val angle = pi + (i.toFloat() / segments) * (pi / 2f)
            GL11.glVertex2f(x + radius + cos(angle) * radius, y + radius + sin(angle) * radius)
        }
        for (i in 0..segments) {
            val angle = (1.5f * pi) + (i.toFloat() / segments) * (pi / 2f)
            GL11.glVertex2f(x + width - radius + cos(angle) * radius, y + radius + sin(angle) * radius)
        }
        GL11.glEnd()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun drawCircleUsingGL(x: Float, y: Float, radius: Float, colorInt: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        val awtColor = Color(colorInt, true)
        GlStateManager.color(
            awtColor.red / 255.0f,
            awtColor.green / 255.0f,
            awtColor.blue / 255.0f,
            awtColor.alpha / 255.0f
        )

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glVertex2f(x, y)
        val segments = 30
        for (i in 0..segments) {
            val angle = (i.toFloat() / segments) * (Math.PI * 2.0).toFloat()
            GL11.glVertex2f(x + cos(angle) * radius, y + sin(angle) * radius)
        }
        GL11.glEnd()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }
}