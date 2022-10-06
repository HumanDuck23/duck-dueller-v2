package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import best.spaghetcodes.duckdueller.bot.player.Mouse
import best.spaghetcodes.duckdueller.utils.Extensions.getVelocity
import best.spaghetcodes.duckdueller.utils.Extensions.scale
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.acos
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
                DuckDueller.mc.thePlayer.getDistanceToEntity(entity) <= 64.0f
            } else {
                false
            }
        } else {
            false
        }
    }

    /**
     * Get the rotations needed to look at an entity
     *
     * @param target target entity
     * @param raw If true, only returns difference in yaw and pitch instead of values needed
     * @param center If true, returns values to look at the player's face, if false, returns the values to look at the closest point in the hitbox
     * @return float[] - {yaw, pitch}
     */
    fun getRotations(player: EntityPlayer?, target: Entity?, raw: Boolean, center: Boolean = false): FloatArray? {
        return if (target == null || player == null) {
            null
        } else {
            val pos: Vec3?
            if (center) {
                pos = Vec3(target.posX, target.posY + target.eyeHeight, target.posZ)
            } else {
                if (!Mouse.isUsingProjectile()) {
                    val box = target.entityBoundingBox

                    // get the four corners of the hitbox
                    var yPos = player.posY + player.eyeHeight

                    if (!player.onGround) {
                        yPos = target.posY + target.eyeHeight
                    } else if (abs(target.posY - player.posY) > player.eyeHeight) {
                        yPos = target.posY + target.eyeHeight / 2f
                    } else if (player.posY - target.posY > 0.3) {
                        yPos = target.posY + target.eyeHeight
                    }

                    val corner1 = Vec3(box.minX, yPos, box.minZ)
                    val corner2 = Vec3(box.maxX, yPos, box.minZ)
                    val corner3 = Vec3(box.minX, yPos, box.maxZ)
                    val corner4 = Vec3(box.maxX, yPos, box.maxZ)

                    // get the closest 2 corners
                    val closest = getClosestCorner(corner1, corner2, corner3, corner4)
                    var a = closest[0]
                    var b = closest[1]

                    val p = Vec3(player.posX, player.posY + player.eyeHeight, player.posZ)

                    // since the two corners are either always on the same X or same Z position, we don't need complicated math
                    if (a.zCoord == b.zCoord) {
                        if (a.xCoord > b.xCoord) {
                            val temp = a
                            a = b
                            b = temp
                        }
                        if (p.xCoord < a.xCoord) {
                            pos = a
                        } else if (p.xCoord > b.xCoord) {
                            pos = b
                        } else {
                            pos = Vec3(p.xCoord, a.yCoord, a.zCoord)
                        }
                    } else {
                        if (a.zCoord > b.zCoord) {
                            val temp = a
                            a = b
                            b = temp
                        }
                        if (p.zCoord < a.zCoord) {
                            pos = a
                        } else if (p.zCoord > b.zCoord) {
                            pos = b
                        } else {
                            pos = Vec3(a.xCoord, a.yCoord, p.zCoord)
                        }
                    }
                } else {
                    val dist = getDistanceNoY(player, target)
                    val tickPredict = when (dist) {
                        in 0f..8f -> dist.toDouble()
                        in 8f..15f -> 15.0
                        else -> 20.0
                    } * if (player.isPotionActive(Potion.moveSpeed)) 1.3 else 1.0
                    val velocity = target.getVelocity().scale(tickPredict)
                    val flatVelo = Vec3(velocity.xCoord, 0.0, velocity.zCoord)
                    val height = when (dist) {
                        in 0f..8f -> target.eyeHeight / 2
                        in 8f..15f -> target.eyeHeight
                        in 15f..25f -> target.eyeHeight * 1.45
                        else -> target.eyeHeight * 1.7
                    }
                    pos = target.positionVector.add(flatVelo).add(Vec3(0.0, height.toDouble(), 0.0)) ?: Vec3(target.posX, target.posY + target.eyeHeight, target.posZ)
                }
            }

            // Not originally my code, but I forgot where I found it

            val diffX = pos.xCoord - player.posX
            val diffY: Double = pos.yCoord - (player.posY + player.getEyeHeight().toDouble())
            val diffZ = pos.zCoord - player.posZ
            val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
            val yaw = (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793)).toFloat()

            if ((crossHairDistance(yaw, pitch, player) > 6 || dist in 2.5..4.0) || Mouse.isUsingProjectile() || Mouse.isUsingPotion()) {
                if (raw) {
                    floatArrayOf(
                        MathHelper.wrapAngleTo180_float(yaw - player.rotationYaw),
                        MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)
                    )
                } else floatArrayOf(
                    player.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - player.rotationYaw),
                    player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)
                )
            } else {
                if (raw) {
                    floatArrayOf(
                        0F, 0F
                    )
                } else {
                    floatArrayOf(
                        player.rotationYaw,
                        player.rotationPitch
                    )
                }
            }
        }
    }

    fun crossHairDistance(yaw: Float, pitch: Float, player: EntityPlayer): Float {
        val nYaw = abs(player.rotationYaw - yaw)
        val nPitch = abs(player.rotationPitch - pitch)
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

    fun entityMovingLeft(entity: Entity, target: Entity): Boolean {
        var lookVec = get2dLookVec(entity).rotateYaw(90f)
        var entityVec = target.getVelocity()

        val angle = acos((lookVec.xCoord * entityVec.xCoord + lookVec.zCoord * entityVec.zCoord) / (lookVec.lengthVector() * entityVec.lengthVector())) * 180 / Math.PI
        return angle > 90
    }

    fun entityMovingRight(entity: Entity, target: Entity): Boolean {
        var lookVec = get2dLookVec(entity).rotateYaw(90f)
        var entityVec = target.getVelocity()

        val angle = acos((lookVec.xCoord * entityVec.xCoord + lookVec.zCoord * entityVec.zCoord) / (lookVec.lengthVector() * entityVec.lengthVector())) * 180 / Math.PI
        return angle < 90
    }

    fun entityFacingAway(entity: Entity, target: Entity): Boolean {
        var vec1 = get2dLookVec(entity)
        var vec2 = get2dLookVec(target)

        val angle = acos((vec1.xCoord * vec2.xCoord + vec1.zCoord * vec2.zCoord) / (vec1.lengthVector() * vec2.lengthVector())) * 180 / Math.PI
        return angle in 20f..70f
    }

    private fun getClosestCorner(corner1: Vec3, corner2: Vec3, corner3: Vec3, corner4: Vec3): ArrayList<Vec3> {
        val pos = Vec3(DuckDueller.mc.thePlayer.posX, DuckDueller.mc.thePlayer.posY + DuckDueller.mc.thePlayer.eyeHeight, DuckDueller.mc.thePlayer.posZ)

        val smallest = arrayListOf(corner1, corner2, corner3, corner4)
        smallest.sortBy { abs(pos.distanceTo(it)) }

        return arrayListOf(smallest[0], smallest[1])
    }

}