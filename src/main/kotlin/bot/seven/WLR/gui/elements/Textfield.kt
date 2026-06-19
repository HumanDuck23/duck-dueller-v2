package bot.seven.WLR.gui.elements

import bot.seven.WLR.gui.GuiColors
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class Textfield(
    id: Int, x: Int, y: Int, width: Int,
    height: Int = MODERN_TEXT_INPUT_HEIGHT,
    label: String,
    initialText: String,
    private val validator: (String) -> Boolean = { true },
    val onTextChanged: (String) -> Unit,
    val onFocusChanged: (Boolean) -> Unit = {}
) : GuiComponentBase(id, x, y, width, height, label) {

    private val horizontalTextPadding = MODERN_ELEMENT_PADDING_X
    private val verticalTextPadding = (this.height - fontRenderer.FONT_HEIGHT) / 2 + 1

    val textField: GuiTextField
    private var lastText: String = initialText
    private var hasInitialFocusCallbackFired = false
    private val cornerRadius = MODERN_CORNER_RADIUS.toFloat()

    init {
        textField = GuiTextField(
            id,
            fontRenderer,
            this.x + horizontalTextPadding,
            this.y + verticalTextPadding,
            this.width - (2 * horizontalTextPadding),
            fontRenderer.FONT_HEIGHT
        )
        textField.text = initialText
        textField.maxStringLength = 256
        textField.enableBackgroundDrawing = false
        textField.setTextColor(GuiColors.TEXTFIELD_TEXT)
        textField.setDisabledTextColour(GuiColors.TEXT_DISABLED)
        textField.isFocused = false
    }

    fun getText(): String = textField.text

    fun setText(newText: String, notify: Boolean = true) {
        val oldText = textField.text
        if (validator(newText)) {
            textField.text = newText
            if (notify && newText != oldText) {
                onTextChanged(newText)
            }
            lastText = newText
        }
    }

    override fun drawComponent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawComponent(mouseX, mouseY, partialTicks)
        if (!visible) return

        textField.setEnabled(this.enabled)
        textField.xPosition = this.x + horizontalTextPadding
        textField.yPosition = this.y + verticalTextPadding
        textField.width = this.width - (2 * horizontalTextPadding)

        val currentBgColor: Int
        val currentOuterBorderColor: Int

        when {
            !enabled -> {
                currentBgColor = GuiColors.COMPONENT_BACKGROUND_DISABLED
                currentOuterBorderColor = GuiColors.MODERN_UI_ELEMENT_BORDER
            }
            textField.isFocused -> {
                currentBgColor = GuiColors.TEXTFIELD_BACKGROUND
                currentOuterBorderColor = GuiColors.TEXTFIELD_BORDER_FOCUSED
            }
            else -> {
                currentBgColor = GuiColors.TEXTFIELD_BACKGROUND
                currentOuterBorderColor = GuiColors.TEXTFIELD_BORDER
            }
        }

        if (textField.isFocused && enabled) {
            drawRoundedRectUsingGL(
                x.toFloat() - 1f, y.toFloat() - 1f,
                width.toFloat() + 2f, height.toFloat() + 2f,
                cornerRadius + 1f,
                GuiColors.PRIMARY_BLUE_BRIGHT_GLOW_EFFECT
            )
        }

        val borderThickness = MODERN_BORDER_THICKNESS.toFloat()

        drawRoundedRectUsingGL(
            x.toFloat(), y.toFloat(),
            width.toFloat(), height.toFloat(),
            cornerRadius,
            currentOuterBorderColor
        )

        drawRoundedRectUsingGL(
            x.toFloat() + borderThickness, y.toFloat() + borderThickness,
            width.toFloat() - 2 * borderThickness, height.toFloat() - 2 * borderThickness,
            (cornerRadius - borderThickness).coerceAtLeast(0f),
            currentBgColor
        )
        textField.setTextColor(if (enabled) GuiColors.TEXTFIELD_TEXT else GuiColors.TEXT_DISABLED)
        textField.drawTextBox()

        drawTopLabel(yOffset = -3)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) {
            if (textField.isFocused) setFocused(false)
            return false
        }

        val wasFocused = textField.isFocused
        val clickedOnThisComponent = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height

        if (clickedOnThisComponent) {
            if (enabled) {
                textField.mouseClicked(mouseX, mouseY, mouseButton)
            }
        } else {
            if (textField.isFocused) {
                setFocused(false)
            }
        }

        if (enabled && textField.isFocused != wasFocused) {
            onFocusChanged(textField.isFocused)
        }

        return enabled && clickedOnThisComponent
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!enabled || !visible || !textField.isFocused) return false

        val previousText = textField.text
        val handled = textField.textboxKeyTyped(typedChar, keyCode)

        if (handled && textField.text != previousText) {
            if (validator(textField.text)) {
                onTextChanged(textField.text)
                lastText = textField.text
            } else {
                textField.text = previousText
            }
        }
        return handled
    }

    fun setFocused(isFocused: Boolean) {
        if (!enabled && isFocused) return

        val oldFocusState = textField.isFocused
        textField.isFocused = isFocused

        if (oldFocusState != isFocused || !hasInitialFocusCallbackFired) {
            onFocusChanged(isFocused)
            hasInitialFocusCallbackFired = true
        }
    }

    fun unfocusIfNeeded() {
        if (textField.isFocused) {
            setFocused(false)
        }
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