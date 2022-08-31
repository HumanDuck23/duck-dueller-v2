package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.utils.Extensions.scale
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

    /**
     * Returns the distance to the closest left edge, or 21 if there is none within 20 blocks
     */
    fun distanceToLeftEdge(player: EntityPlayer): Int {
        for (i in 1..20) {
            if (airOnLeft(player, i.toFloat())) {
                return i
            }
        }
        return 21
    }

    /**
     * Returns the distance to the closest right edge, or 21 if there is none within 20 blocks
     */
    fun distanceToRightEdge(player: EntityPlayer): Int {
        for (i in 1..20) {
            if (airOnRight(player, i.toFloat())) {
                return i
            }
        }
        return 21
    }

    fun distanceToRightBarrier(player: EntityPlayer): Int {
        for (i in 1..60) {
            if (barrierCheck(player, player.position.add(0.0, player.eyeHeight.toDouble(), 0.0), i.toFloat(), EntityUtils.get2dLookVec(player).rotateYaw(-90f))) {
                return i
            }
        }
        return -1
    }

    fun distanceToLeftBarrier(player: EntityPlayer): Int {
        for (i in 1..60) {
            if (barrierCheck(player, player.position.add(0.0, player.eyeHeight.toDouble(), 0.0), i.toFloat(), EntityUtils.get2dLookVec(player).rotateYaw(90f))) {
                return i
            }
        }
        return -1
    }

    fun entityOffEdge(player: EntityPlayer): Boolean {
        if (!player.onGround) {
            val pos = player.positionVector.subtract(Vec3(0.0, 4.0, 0.0))
            val positions = arrayListOf(pos.add(Vec3(0.2, 0.0, 0.0)), pos.add(Vec3(0.0, 0.0, 0.2)), pos.add(Vec3(-0.2, 0.0, 0.0)), pos.add(Vec3(0.2, 0.0, -0.2)))
            for (position in positions) {
                if (DuckDueller.mc.theWorld.getBlockState(BlockPos(position)).block != Blocks.air) {
                    return false
                }
            }
            return true
        }
        return false
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

    private fun barrierCheck(player: EntityPlayer, pos: BlockPos, distance: Float, lookVec: Vec3): Boolean {
        for (i in 1..distance.toInt()) {
            if (DuckDueller.mc.theWorld.getBlockState(BlockPos(pos.x + lookVec.xCoord * i, pos.y - (if (player.onGround) 0.2 else 1.4), pos.z + lookVec.zCoord * i)).block == Blocks.barrier) {
                return true
            }
        }
        return false
    }
    
}