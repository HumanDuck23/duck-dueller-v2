package best.spaghetcodes.duckdueller.bot.player

import best.spaghetcodes.duckdueller.DuckDueller

object Inventory {

    /**
     * Sets the players current item to the item passed
     */
    fun setInvItem(item: String): Boolean {
        val _item = item.lowercase()
        for (i in 0..8) {
            try {
                if (DuckDueller.mc.thePlayer.inventory.getCurrentItem().unlocalizedName.lowercase().contains(_item)
                ) {
                    return true
                } else {
                    DuckDueller.mc.thePlayer.inventory.changeCurrentItem(-1)
                }
            } catch (e: Exception) {
                DuckDueller.mc.thePlayer.inventory.changeCurrentItem(-1)
            }
        }
        return false
    }

    /**
     * Move the the passed inv slot
     */
    fun setInvSlot(slot: Int) {
        if (slot in 0..9) {
            for (i in 0..8) {
                if (DuckDueller.mc.thePlayer.inventory.currentItem > slot) {
                    DuckDueller.mc.thePlayer.inventory.changeCurrentItem(-1)
                }
            }
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