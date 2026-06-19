package bot.seven.WLR.gui.elements

import bot.seven.WLR.gui.GuiColors
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class Button(
    id: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int = MODERN_BUTTON_HEIGHT,
    buttonText: String,
    val onClick: () -> Unit
) : GuiComponentBase(id, x, y, width, height, buttonText) {

    private val cornerRadius = MODERN_CORNER_RADIUS.toFloat()

    override fun drawComponent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawComponent(mouseX, mouseY, partialTicks)
        if (!visible) return

        val currentBgColor: Int
        val currentTextColor: Int
        val currentBorderColor: Int
        var glowColor = 0

        when {
            !enabled -> {
                currentBgColor = GuiColors.COMPONENT_BACKGROUND_DISABLED
                currentTextColor = GuiColors.TEXT_DISABLED
                currentBorderColor = GuiColors.MODERN_UI_ELEMENT_BORDER
            }
            hovered -> {
                currentBgColor = GuiColors.BUTTON_MODERN_BACKGROUND_HOVER
                currentTextColor = GuiColors.BUTTON_MODERN_TEXT
                currentBorderColor = GuiColors.PRIMARY_BLUE_DARK
                glowColor = GuiColors.PRIMARY_BLUE_BRIGHT_GLOW_EFFECT
            }
            else -> {
                currentBgColor = GuiColors.BUTTON_MODERN_BACKGROUND
                currentTextColor = GuiColors.BUTTON_MODERN_TEXT
                currentBorderColor = GuiColors.PRIMARY_BLUE_DARK
            }
        }

        val borderThickness = MODERN_BORDER_THICKNESS.toFloat()

        if (glowColor != 0) {
            drawRoundedRectUsingGL(
                x.toFloat() - 1f, y.toFloat() - 1f,
                width.toFloat() + 2f, height.toFloat() + 2f,
                cornerRadius + 1f,
                glowColor
            )
        }

        drawRoundedRectUsingGL(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), cornerRadius, currentBorderColor)

        drawRoundedRectUsingGL(
            x.toFloat() + borderThickness, y.toFloat() + borderThickness,
            width.toFloat() - borderThickness * 2, height.toFloat() - borderThickness * 2,
            (cornerRadius - borderThickness).coerceAtLeast(0f),
            currentBgColor
        )

        val textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1
        drawCenteredString(
            Minecraft.getMinecraft().fontRendererObj,
            label,
            x + width / 2,
            textY,
            currentTextColor
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (enabled && visible && mouseButton == 0 &&
            mouseX >= this.x && mouseY >= this.y &&
            mouseX < this.x + this.width && mouseY < this.y + this.height) {
            mc.soundHandler.playSound(net.minecraft.client.audio.PositionedSoundRecord.create(net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F))
            onClick()
            return true
        }
        return false
    }

    private fun drawCenteredString(fontRenderer: FontRenderer, text: String, x: Int, y: Int, color: Int) {
        fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text) / 2, y, color)
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