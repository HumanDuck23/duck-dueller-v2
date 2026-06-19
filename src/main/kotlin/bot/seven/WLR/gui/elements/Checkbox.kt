package bot.seven.WLR.gui.elements

import bot.seven.WLR.gui.GuiColors
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class Checkbox(
    id: Int, x: Int, y: Int,
    private val boxSize: Int = MODERN_CHECKBOX_SIZE,
    label: String,
    initialValue: Boolean,
    val onValueChanged: (Boolean) -> Unit
) : GuiComponentBase(id, x, y, boxSize, boxSize, label) {

    var isChecked: Boolean = initialValue
        private set
    private val cornerRadius = 2f
    private val checkmarkInsetRatio = 0.25f

    override fun drawComponent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawComponent(mouseX, mouseY, partialTicks)
        if (!visible) return

        val boxActuallyHovered = this.hovered || (isLabelHovered(mouseX, mouseY) && enabled)

        val currentBgColor: Int
        val currentBorderColor: Int
        val currentCheckColor: Int

        when {
            !enabled -> {
                currentBgColor = GuiColors.COMPONENT_BACKGROUND_DISABLED
                currentBorderColor = GuiColors.MODERN_UI_ELEMENT_BORDER
                currentCheckColor = GuiColors.TEXT_DISABLED
            }
            else -> {
                currentBgColor = if (boxActuallyHovered) GuiColors.CHECKBOX_BOX_HOVER else GuiColors.CHECKBOX_BOX
                currentBorderColor = if (boxActuallyHovered || isChecked) GuiColors.PRIMARY_BLUE_BRIGHT else GuiColors.MODERN_UI_ELEMENT_BORDER
                currentCheckColor = GuiColors.CHECKBOX_CHECK
            }
        }

        drawRoundedRectUsingGL(
            this.x.toFloat() + SHADOW_OFFSET_X / 2f,
            this.y.toFloat() + SHADOW_OFFSET_Y / 2f,
            width.toFloat(),
            height.toFloat(),
            cornerRadius,
            GuiColors.SUBTLE_SHADOW_COLOR
        )

        val borderThickness = MODERN_BORDER_THICKNESS.toFloat()

        drawRoundedRectUsingGL(
            this.x.toFloat(), this.y.toFloat(),
            width.toFloat(), height.toFloat(),
            cornerRadius,
            currentBorderColor
        )

        drawRoundedRectUsingGL(
            this.x.toFloat() + borderThickness, this.y.toFloat() + borderThickness,
            width.toFloat() - borderThickness * 2, height.toFloat() - borderThickness * 2,
            (cornerRadius - borderThickness).coerceAtLeast(0f),
            currentBgColor
        )

        if (isChecked) {
            val inset = (width * checkmarkInsetRatio).toInt().coerceAtLeast(1)
            drawRoundedRectUsingGL(
                (this.x + inset).toFloat(), (this.y + inset).toFloat(),
                (width - 2 * inset).toFloat(), (height - 2 * inset).toFloat(),
                1f,
                currentCheckColor
            )
        }

        drawSideLabel(labelYOffset = (height - fontRenderer.FONT_HEIGHT) / 2 + 1, xOffset = 6)
    }

    private fun isLabelHovered(mouseX: Int, mouseY: Int): Boolean {
        if (label.isEmpty()) return false
        val labelStartX = x + width + 6
        val labelRenderY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1
        val labelTextWidth = fontRenderer.getStringWidth(label)
        return mouseX >= labelStartX && mouseX < labelStartX + labelTextWidth &&
                mouseY >= labelRenderY && mouseY < labelRenderY + fontRenderer.FONT_HEIGHT
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (enabled && visible && mouseButton == 0) {
            val boxClicked = mouseX >= this.x && mouseX < this.x + this.width &&
                    mouseY >= this.y && mouseY < this.y + this.height
            val labelClicked = isLabelHovered(mouseX, mouseY)

            if (boxClicked || labelClicked) {
                isChecked = !isChecked
                onValueChanged(isChecked)
                mc.soundHandler.playSound(net.minecraft.client.audio.PositionedSoundRecord.create(net.minecraft.util.ResourceLocation("gui.button.press"), 0.7F))
                return true
            }
        }
        return false
    }

    /**
     * Corrected drawing function that cooperates with Minecraft's GlStateManager.
     */
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
}