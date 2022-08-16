package best.spaghetcodes.duckdueller.utils

import net.minecraft.entity.Entity
import net.minecraft.util.Vec3

object Extensions {

    fun Vec3.scale (x: Double): Vec3 {
        return Vec3(this.xCoord * x, this.yCoord * x, this.zCoord * x)
    }

    // i found entity.motionXYZ to be unreliable, so i made my own
    fun Entity.getVelocity(): Vec3 {
        return Vec3(this.posX - this.prevPosX, this.posY - this.prevPosY, this.posZ - this.prevPosZ)
    }

}