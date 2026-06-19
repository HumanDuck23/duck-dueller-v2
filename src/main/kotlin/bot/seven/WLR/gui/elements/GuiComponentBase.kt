package bot.seven.WLR.gui.elements

import bot.seven.WLR.gui.GuiColors
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer

const val DEFAULT_CORNER_RADIUS = 4f
const val DEFAULT_BORDER_THICKNESS = 1f
const val MODERN_CORNER_RADIUS = 5f
const val MODERN_BORDER_THICKNESS = 1f
const val MODERN_ELEMENT_PADDING_X = 10
const val MODERN_ELEMENT_PADDING_Y_RATIO = 0.3f
const val MODERN_TEXT_INPUT_HEIGHT = 24
const val MODERN_BUTTON_HEIGHT = 26
const val MODERN_DROPDOWN_HEIGHT = 24
const val MODERN_CHECKBOX_SIZE = 14
const val MODERN_SLIDER_HEIGHT = 18
const val MODERN_SLIDER_KNOB_RADIUS = 7f
const val MODERN_SLIDER_TRACK_HEIGHT = 5f

const val SHADOW_OFFSET_X = 1f
const val SHADOW_OFFSET_Y = 1f

abstract class GuiComponentBase(
    var id: Int,
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
    var label: String = ""
) {
    protected val mc: Minecraft = Minecraft.getMinecraft()
    protected val fontRenderer: FontRenderer = mc.fontRendererObj
    var enabled: Boolean = true
    var visible: Boolean = true
    var hovered: Boolean = false

    open fun drawComponent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        this.hovered = enabled &&
                mouseX >= this.x && mouseY >= this.y &&
                mouseX < this.x + this.width && mouseY < this.y + this.height
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!enabled || !visible) return false
        return mouseX >= this.x && mouseY >= this.y &&
                mouseX < this.x + this.width && mouseY < this.y + this.height
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {}
    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }

    protected fun drawSideLabel(labelYOffset: Int = (height - fontRenderer.FONT_HEIGHT) / 2, xOffset: Int = 8) {
        if (label.isNotEmpty()) {
            val labelColor = if (enabled) GuiColors.TEXT_PRIMARY else GuiColors.TEXT_DISABLED
            fontRenderer.drawString(label, x + width + xOffset, y + labelYOffset, labelColor)
        }
    }

    protected fun drawTopLabel(yOffset: Int = -4) {
        if (label.isNotEmpty()) {
            val labelColor = if (enabled) GuiColors.TEXT_PRIMARY else GuiColors.TEXT_DISABLED
            fontRenderer.drawString(label, x, y - fontRenderer.FONT_HEIGHT + yOffset, labelColor)
        }
    }
}