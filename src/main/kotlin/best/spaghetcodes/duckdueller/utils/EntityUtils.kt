package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.entity.player.EntityPlayer

object EntityUtils {

    fun getOpponentEntity(): EntityPlayer? {
        if (DuckDueller.mc.theWorld != null) {
            for (entity in DuckDueller.mc.theWorld.playerEntities) {
                if (entity.displayName != DuckDueller.mc.thePlayer.displayName && shouldTarget(entity)) {
                    return entity
                }
            }
        }
        return null
    }

    private fun shouldTarget(entity: EntityPlayer?): Boolean {
        return if (entity == null) {
            false
        } else if (DuckDueller.mc.thePlayer.isEntityAlive && entity.isEntityAlive) {
            if (!entity.isInvisible /*&& !entity.isInvisibleToPlayer(mc.thePlayer)*/) {
                if (DuckDueller.mc.thePlayer.getDistanceToEntity(entity) > 64.0f) {
                    false
                } else {
                    DuckDueller.mc.thePlayer.canEntityBeSeen(entity)
                }
            } else {
                false
            }
        } else {
            false
        }
    }

}