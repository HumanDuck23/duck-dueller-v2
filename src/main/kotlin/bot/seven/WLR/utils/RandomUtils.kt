package bot.seven.WLR.utils

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
        if (min > max) {
            return max
        }
        if (min == max) {
            return min
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1)
    }

    /**
     * Get a random double in a certain range
     * @param min
     * @param max
     * @return double
     */
    fun randomDoubleInRange(min: Double, max: Double): Double {
        if (min > max) {
            return max
        }
        if (min == max) {
            return min
        }
        return min + (max - min) * Random().nextDouble()
    }

    fun randomDouble(): Double {
        return Random().nextDouble()
    }

    /**
     * Get a random boolean value
     * @return bool
     */
    fun randomBool(): Boolean {
        return Random().nextBoolean()
    }

}