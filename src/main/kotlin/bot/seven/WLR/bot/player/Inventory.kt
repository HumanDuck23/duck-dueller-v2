package bot.seven.WLR.bot.player

import bot.seven.WLR.wlr

object Inventory {

    /**
     * Sets the players current item to the item passed
     */
    fun setInvItem(item: String): Boolean {
        val _item = item.lowercase()
        if (wlr.mc.thePlayer != null && wlr.mc.thePlayer.inventory != null) {
            for (i in 0..8) {
                val stack = wlr.mc.thePlayer.inventory.getStackInSlot(i)
                if (stack != null && stack.unlocalizedName.lowercase().contains(_item)) {
                    wlr.mc.thePlayer.inventory.currentItem = i
                    return true
                }
            }
        }
        return false
    }

    /**
     * Set the current inventory item (by itemDamage, use for potions etc)
     */
    fun setInvItemByDamage(itemDamage: Int): Boolean {
        if (wlr.mc.thePlayer != null && wlr.mc.thePlayer.inventory != null) {
            for (i in 0..8) {
                val stack = wlr.mc.thePlayer.inventory.getStackInSlot(i)
                if (stack != null && stack.itemDamage == itemDamage) {
                    wlr.mc.thePlayer.inventory.currentItem = i
                    return true
                }
            }
        }
        return false
    }

    /**
     * Move the the passed inv slot
     */
    fun setInvSlot(slot: Int) {
        if (wlr.mc.thePlayer != null && wlr.mc.thePlayer.inventory != null) {
            wlr.mc.thePlayer.inventory.currentItem = slot
        }
        // bruh
    }

    /**
     * Checks it the player has this item in their inventory
     */
    fun hasItem(item: String): Boolean {
        val _item = item.lowercase()
        if (wlr.mc.thePlayer != null) {
            for (itemStack in wlr.mc.thePlayer.getInventory()) {
                if (itemStack.unlocalizedName.lowercase().contains(_item)) {
                    return true
                }
            }
        }
        return false
    }

}