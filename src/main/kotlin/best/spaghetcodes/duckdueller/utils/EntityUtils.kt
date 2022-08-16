package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

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

    fun crossHairDistance(yaw: Float, pitch: Float, player: EntityPlayer): Float {
        val nYaw = yaw - player.rotationYaw - yaw
        val nPitch = pitch - player.rotationPitch - pitch
        return MathHelper.sqrt_float(nYaw * nYaw + nPitch * nPitch)
    }

    fun getDistanceNoY(player: EntityPlayer?, target: Entity?): Float {
        return if (target == null || player == null) {
            0f
        } else {
            val diffX = player.posX - target.posX
            val diffZ = player.posZ - target.posZ
            MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toFloat()
        }
    }

    fun get2dLookVec(entity: Entity): Vec3 {
        val yaw = ((entity.rotationYaw + 90)  * Math.PI) / 180
        return Vec3(cos(yaw), 0.0, sin(yaw))
    }

}