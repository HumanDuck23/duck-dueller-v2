package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object WorldUtils {

    fun blockInFront(player: EntityPlayer, distance: Float, yMod: Float = 0f): Block { // yMod = 0 -> feet, 1 -> 1 above feet etc
        val vec = Vec3(player.lookVec.xCoord * distance, 0.0, player.lookVec.zCoord * distance)
        return DuckDueller.mc.theWorld.getBlockState(player.position.add(vec.xCoord, -0.2 + yMod, vec.zCoord)).block
    }

    fun airInFront(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player, player.position, distance, EntityUtils.get2dLookVec(player))
    }

    fun airInBack(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player, player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(180f))
    }

    fun airOnLeft(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player, player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(90f))
        //return circleAirCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(90f), 2, 2)
    }

    fun airOnRight(player: EntityPlayer, distance: Float): Boolean {
        return airCheck(player, player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(-90f))
        //return circleAirCheck(player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(-90f), 2, 2)
    }

    fun airCheckAngle(player: EntityPlayer, distance: Float, angleMin: Float, angleMax: Float): Boolean {
        if (angleMax < angleMin) {
            for (i in angleMin.toInt() downTo angleMax.toInt()) {
                if (airCheck(player, player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(i.toFloat()))) {
                    return true
                }
            }
        } else {
            for (i in angleMin.toInt()..angleMax.toInt()) {
                if (airCheck(player, player.position, distance, EntityUtils.get2dLookVec(player).rotateYaw(i.toFloat()))) {
                    return true
                }
            }
        }
        return false
    }

    private fun airCheck(player: EntityPlayer, pos: BlockPos, distance: Float, lookVec: Vec3): Boolean {
        for (i in 1..distance.toInt()) {
            if (DuckDueller.mc.theWorld.getBlockState(BlockPos(pos.x + lookVec.xCoord * i, pos.y - (if (player.onGround) 0.2 else 1.4), pos.z + lookVec.zCoord * i)).block == Blocks.air) {
                return true
            }
        }
        return false
    }
    
}