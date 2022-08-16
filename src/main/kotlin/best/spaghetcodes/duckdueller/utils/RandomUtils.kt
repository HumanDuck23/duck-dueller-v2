package best.spaghetcodes.duckdueller.utils

import java.util.*
import java.util.concurrent.ThreadLocalRandom

object RandomUtils {

    /**
     * Get a random integer in a certain range
     * @param min
     * @param max
     * @return int
     */
    fun randomIntInRange(min: Int, max: Int): Int {
        return ThreadLocalRandom.current().nextInt(min, max + 1)
    }

    /**
     * Get a random double in a certain range
     * @param min
     * @param max
     * @return double
     */
    fun randomDoubleInRange(min: Double, max: Double): Double {
        val r = Random()
        return min + (max - min) * r.nextDouble()
    }

    /**
     * Get a random boolean value
     * @return bool
     */
    fun randomBool(): Boolean {
        val r = Random()
        return r.nextBoolean()
    }

}