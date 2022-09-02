package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller

object Inventory {

    /**
     * Sets the players current item to the item passed
     */
    fun setInvItem(item: String): Boolean {
        val _item = item.lowercase()
        if (DuckDueller.mc.thePlayer != null && DuckDueller.mc.thePlayer.inventory != null) {
            for (i in 0..8) {
                val stack = DuckDueller.mc.thePlayer.inventory.getStackInSlot(i)
                if (stack != null && stack.unlocalizedName.lowercase().contains(_item)) {
                    DuckDueller.mc.thePlayer.inventory.currentItem = i
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
        if (DuckDueller.mc.thePlayer != null && DuckDueller.mc.thePlayer.inventory != null) {
            for (i in 0..8) {
                val stack = DuckDueller.mc.thePlayer.inventory.getStackInSlot(i)
                if (stack != null && stack.itemDamage == itemDamage) {
                    DuckDueller.mc.thePlayer.inventory.currentItem = i
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
        if (DuckDueller.mc.thePlayer != null && DuckDueller.mc.thePlayer.inventory != null) {
            DuckDueller.mc.thePlayer.inventory.currentItem = slot
        }
        // bruh
    }

    /**
     * Checks it the player has this item in their inventory
     */
    fun hasItem(item: String): Boolean {
        val _item = item.lowercase()
        if (DuckDueller.mc.thePlayer != null) {
            for (itemStack in DuckDueller.mc.thePlayer.getInventory()) {
                if (itemStack.unlocalizedName.lowercase().contains(_item)) {
                    return true
                }
            }
        }
        return false
    }

}