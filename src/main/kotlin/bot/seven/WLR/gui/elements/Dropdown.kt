package bot.seven.WLR.gui.elements

import bot.seven.WLR.gui.GuiColors
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Dropdown(
    id: Int, x: Int, y: Int, width: Int,
    height: Int = MODERN_DROPDOWN_HEIGHT,
    label: String,
    val options: List<String>,
    initialSelectedIndex: Int,
    val onSelectionChanged: (Int, String) -> Unit
) : GuiComponentBase(id, x, y, width, height, label) {

    var selectedIndex: Int = -1; private set
    var isOpen: Boolean = false
    val optionHeight = 22
    val maxDisplayableOptions = 5

    private var scrollYOptions: Float = 0f; private var maxScrollYOptions: Float = 0f
    private var isDraggingScrollbar: Boolean = false
    private val scrollbarWidth = 10
    private var needsScrollbar: Boolean = false
    private var lastMouseYForScrollDrag: Int = 0
    private val cornerRadius = MODERN_CORNER_RADIUS.toFloat()
    private val listCornerRadius = 3f
    private val textPaddingX = MODERN_ELEMENT_PADDING_X
    private val textPaddingY = (this.height - fontRenderer.FONT_HEIGHT) / 2 + 1

    init {
        val idx = if (options.isEmpty()) -1 else initialSelectedIndex.coerceIn(0, options.size - 1)
        setSelected(idx, false)
    }

    fun setSelected(index: Int, notify: Boolean = true) {
        val old = selectedIndex
        selectedIndex = if (options.isEmpty()) -1 else index.coerceIn(0, options.indices.lastOrNull() ?: -1)
        if (notify && old != selectedIndex && selectedIndex != -1 && selectedIndex < options.size) {
            onSelectionChanged(selectedIndex, options[selectedIndex])
        }
    }

    fun getSelectedOption(): String? = if (options.isNotEmpty() && selectedIndex != -1) options.getOrNull(selectedIndex) else null

    private fun getDisplayableOptionCount(): Int = min(options.size, maxDisplayableOptions)
    private fun getListVisibleHeight(): Int = getDisplayableOptionCount() * optionHeight
    private fun getTotalOptionsContentHeight(): Int = options.size * optionHeight

    override fun drawComponent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawComponent(mouseX, mouseY, partialTicks)
        if (!visible) return

        val mainBoxBg: Int
        val mainBoxBorder: Int
        val currentTextColor = if (enabled) GuiColors.TEXT_PRIMARY else GuiColors.TEXT_DISABLED
        val arrowColor = if (enabled) GuiColors.DROPDOWN_ARROW else GuiColors.TEXT_DISABLED

        when {
            !enabled -> {
                mainBoxBg = GuiColors.COMPONENT_BACKGROUND_DISABLED
                mainBoxBorder = GuiColors.MODERN_UI_ELEMENT_BORDER
            }
            isOpen -> {
                mainBoxBg = GuiColors.COMPONENT_BACKGROUND
                mainBoxBorder = GuiColors.PRIMARY_BLUE_BRIGHT
            }
            this.hovered -> {
                mainBoxBg = GuiColors.COMPONENT_BACKGROUND_HOVER
                mainBoxBorder = GuiColors.PRIMARY_BLUE
            }
            else -> {
                mainBoxBg = GuiColors.COMPONENT_BACKGROUND
                mainBoxBorder = GuiColors.MODERN_UI_ELEMENT_BORDER
            }
        }

        val borderThickness = MODERN_BORDER_THICKNESS.toFloat()

        drawRoundedRectUsingGL(
            x.toFloat() + SHADOW_OFFSET_X, y.toFloat() + SHADOW_OFFSET_Y,
            width.toFloat(), height.toFloat(),
            cornerRadius, GuiColors.SUBTLE_SHADOW_COLOR
        )

        drawRoundedRectUsingGL(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), cornerRadius, mainBoxBorder)

        drawRoundedRectUsingGL(
            x + borderThickness, y + borderThickness,
            width - borderThickness * 2, height - borderThickness * 2,
            (cornerRadius - borderThickness).coerceAtLeast(0f), mainBoxBg
        )

        val selectedText = getSelectedOption() ?: "Select..."
        fontRenderer.drawString(selectedText, x + textPaddingX, y + textPaddingY, currentTextColor)
        val arrow = if (isOpen) "▲" else "▼"
        fontRenderer.drawString(arrow, x + width - fontRenderer.getStringWidth(arrow) - textPaddingX, y + textPaddingY, arrowColor)

        drawTopLabel(yOffset = -3)

        if (isOpen && enabled) {
            val totalContentH = getTotalOptionsContentHeight()
            val listVisH = getListVisibleHeight()
            needsScrollbar = totalContentH > listVisH
            val listTopY = this.y + this.height
            val listDrawWidth = this.width

            drawRoundedRectUsingGL(
                x.toFloat() + SHADOW_OFFSET_X, listTopY.toFloat() + SHADOW_OFFSET_Y,
                width.toFloat(), listVisH.toFloat(),
                listCornerRadius, GuiColors.SUBTLE_SHADOW_COLOR
            )
            drawRoundedRectUsingGL(x.toFloat(), listTopY.toFloat(), width.toFloat(), listVisH.toFloat(), listCornerRadius, GuiColors.MODERN_UI_ELEMENT_BORDER)
            drawRoundedRectUsingGL(
                x + borderThickness, listTopY + borderThickness,
                width - borderThickness * 2, listVisH - borderThickness * 2,
                (listCornerRadius - borderThickness).coerceAtLeast(0f), GuiColors.DROPDOWN_BACKGROUND_OPEN
            )

            val sr = ScaledResolution(mc)
            val borderIntScissor = borderThickness.toInt()
            val scissorListDrawWidth = if (needsScrollbar) listDrawWidth - scrollbarWidth else listDrawWidth

            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            val scissorY = sr.scaledHeight - (listTopY + listVisH - borderIntScissor)
            GL11.glScissor(
                (x + borderIntScissor) * sr.scaleFactor,
                scissorY * sr.scaleFactor,
                (scissorListDrawWidth - 2 * borderIntScissor) * sr.scaleFactor,
                (listVisH - 2 * borderIntScissor) * sr.scaleFactor
            )

            for (i in options.indices) {
                val optTopAbs = i * optionHeight
                val optTopScreen = listTopY + optTopAbs - scrollYOptions.toInt()

                if (optTopScreen + optionHeight < listTopY || optTopScreen > listTopY + listVisH) continue

                val optTextY = optTopScreen + (optionHeight - fontRenderer.FONT_HEIGHT) / 2 + 1
                val isOptHover = mouseX >= x + borderIntScissor && mouseX < x + scissorListDrawWidth - borderIntScissor &&
                        mouseY >= max(listTopY + borderIntScissor, optTopScreen) &&
                        mouseY < min(listTopY + listVisH - borderIntScissor, optTopScreen + optionHeight)

                val optBg = when {
                    isOptHover -> GuiColors.DROPDOWN_ITEM_HOVER_BG
                    i == selectedIndex -> GuiColors.DROPDOWN_ITEM_SELECTED_BG
                    else -> 0
                }

                if (optBg != 0) {
                    drawRoundedRectUsingGL(
                        (x + borderIntScissor + 1f), optTopScreen.toFloat(),
                        (scissorListDrawWidth - 2 * borderIntScissor - 2f), optionHeight.toFloat(),
                        1f,
                        optBg
                    )
                }
                fontRenderer.drawString(options[i], x + textPaddingX, optTextY, GuiColors.DROPDOWN_ITEM_TEXT)
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST)

            if (needsScrollbar) {
                maxScrollYOptions = max(0f, (totalContentH - listVisH).toFloat())
                scrollYOptions = scrollYOptions.coerceIn(0f, maxScrollYOptions)
                val sbX = x + width - scrollbarWidth - borderIntScissor
                drawRoundedRectUsingGL(
                    sbX.toFloat(),
                    (listTopY + borderIntScissor).toFloat(),
                    scrollbarWidth.toFloat(),
                    (listVisH - 2 * borderIntScissor).toFloat(),
                    2f,
                    GuiColors.SCROLLBAR_BG
                )

                if (maxScrollYOptions > 0) {
                    val thumbHRatio = (listVisH.toFloat() / totalContentH.toFloat()).coerceIn(0.1f, 1f)
                    val thumbH = max(15, (listVisH * thumbHRatio).toInt())
                    val trackDrawableHeight = listVisH - 2 * borderIntScissor
                    val thumbActualY = (listTopY + borderIntScissor) + ((trackDrawableHeight - thumbH) * (scrollYOptions / maxScrollYOptions))

                    val thumbHover = mouseX >= sbX && mouseX < sbX + scrollbarWidth &&
                            mouseY >= thumbActualY && mouseY < thumbActualY + thumbH
                    drawRoundedRectUsingGL(
                        (sbX + 1f),
                        thumbActualY,
                        (scrollbarWidth - 2f),
                        thumbH.toFloat(),
                        2f,
                        if (thumbHover) GuiColors.MODERN_SCROLLBAR_THUMB_HOVER else GuiColors.SCROLLBAR_THUMB
                    )
                }
            } else {
                scrollYOptions = 0f
                maxScrollYOptions = 0f
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!enabled || !visible || mouseButton != 0) return false

        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            isOpen = !isOpen
            if (isOpen) {
                if (options.isNotEmpty() && selectedIndex != -1 && selectedIndex < options.size) {
                    val selTop = selectedIndex * optionHeight
                    val selBot = selTop + optionHeight
                    val visH = getListVisibleHeight()
                    if (selTop < scrollYOptions) scrollYOptions = selTop.toFloat()
                    else if (selBot > scrollYOptions + visH) scrollYOptions = (selBot - visH).toFloat()

                    if(needsScrollbar) {
                        maxScrollYOptions = max(0f, (getTotalOptionsContentHeight() - visH).toFloat())
                        scrollYOptions = scrollYOptions.coerceIn(0f, maxScrollYOptions)
                    } else {
                        scrollYOptions = 0f
                    }
                } else {
                    scrollYOptions = 0f
                }
            }
            mc.soundHandler.playSound(net.minecraft.client.audio.PositionedSoundRecord.create(net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F))
            return true
        }

        if (isOpen) {
            val listTopY = this.y + this.height
            val listVisibleHeight = getListVisibleHeight()
            val listBottomY = listTopY + listVisibleHeight
            val actualListBorderThicknessInt = MODERN_BORDER_THICKNESS.toInt()

            if (needsScrollbar) {
                val sbX = x + width - scrollbarWidth - actualListBorderThicknessInt
                if (mouseX >= sbX && mouseX < sbX + scrollbarWidth &&
                    mouseY >= listTopY && mouseY < listBottomY) {
                    isDraggingScrollbar = true
                    lastMouseYForScrollDrag = mouseY
                    val trackDrawableHeight = listVisibleHeight - 2 * actualListBorderThicknessInt
                    if (trackDrawableHeight > 0) {
                        val clickRatioInTrack = (mouseY - (listTopY + actualListBorderThicknessInt)).toFloat() / trackDrawableHeight.toFloat()
                        scrollYOptions = (maxScrollYOptions * clickRatioInTrack).coerceIn(0f, maxScrollYOptions)
                    }
                    return true
                }
            }

            val itemsAreaWidth = if (needsScrollbar) width - scrollbarWidth else width
            if (mouseX >= x + actualListBorderThicknessInt && mouseX < x + itemsAreaWidth - actualListBorderThicknessInt &&
                mouseY >= listTopY + actualListBorderThicknessInt && mouseY < listBottomY - actualListBorderThicknessInt) {

                val mouseYInListContent = mouseY - (listTopY + actualListBorderThicknessInt)
                val absoluteMouseYInOptions = mouseYInListContent + scrollYOptions
                val clickedOptionIndex = (absoluteMouseYInOptions / optionHeight).toInt()

                if (clickedOptionIndex >= 0 && clickedOptionIndex < options.size) {
                    setSelected(clickedOptionIndex)
                    isOpen = false
                    mc.soundHandler.playSound(net.minecraft.client.audio.PositionedSoundRecord.create(net.minecraft.util.ResourceLocation("gui.button.press"), 0.8F))
                    return true
                }
            }

            if (mouseX >= x && mouseX < x + width && mouseY >= listTopY && mouseY < listBottomY) {
                return true
            }
        }

        if (isOpen) {
            val totalDropdownHeight = this.height + if(isOpen) getListVisibleHeight() else 0
            if (!(mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + totalDropdownHeight)) {
                close()
            }
        }
        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) {
            isDraggingScrollbar = false
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDraggingScrollbar && clickedMouseButton == 0 && needsScrollbar && maxScrollYOptions > 0) {
            val dy = mouseY - lastMouseYForScrollDrag
            lastMouseYForScrollDrag = mouseY

            val trackDrawableHeight = getListVisibleHeight() - 2 * MODERN_BORDER_THICKNESS.toInt()
            if (trackDrawableHeight <= 0) return

            val thumbHRatio = (getListVisibleHeight().toFloat() / getTotalOptionsContentHeight().toFloat()).coerceIn(0.1f, 1f)
            val thumbH = max(15, (getListVisibleHeight() * thumbHRatio).toInt())
            val draggableTrackSpace = trackDrawableHeight - thumbH

            if (draggableTrackSpace <= 0) return

            val scrollAmount = dy * (maxScrollYOptions / draggableTrackSpace.toFloat())

            scrollYOptions = (scrollYOptions + scrollAmount).coerceIn(0f, maxScrollYOptions)
        }
    }

    fun handleMouseScroll(rawMouseX: Int, rawMouseY: Int, dWheel: Int): Boolean {
        if (isOpen && enabled && visible && options.isNotEmpty()) {
            val listTopY = y + height
            val listVisibleH = getListVisibleHeight()
            val listBottomY = listTopY + listVisibleH

            if (rawMouseX >= x && rawMouseX < x + width &&
                rawMouseY >= listTopY && rawMouseY < listBottomY) {

                if (getTotalOptionsContentHeight() <= listVisibleH) return false

                maxScrollYOptions = max(0f, (getTotalOptionsContentHeight() - listVisibleH).toFloat())
                val scrollAmountPerTick = optionHeight * 1.5f
                val scrollDelta = if (dWheel > 0) -scrollAmountPerTick else scrollAmountPerTick

                scrollYOptions = (scrollYOptions + scrollDelta).coerceIn(0f, maxScrollYOptions)
                return true
            }
        }
        return false
    }

    fun close() {
        if (isOpen) {
            isOpen = false
            isDraggingScrollbar = false
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