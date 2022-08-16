package best.spaghetcodes.duckdueller.utils

import best.spaghetcodes.duckdueller.DuckDueller
import com.google.gson.JsonObject
import java.net.URL


object HttpUtils {

    fun getPlayerStats(uuid: String): JsonObject? {
        val key = DuckDueller.config?.apiKey
        if (key != null && key != "") {
            val url = URL("https://api.hypixel.net/player?key=${key}&uuid=$uuid")
            val json = url.readText()
            return DuckDueller.gson.fromJson(json, JsonObject::class.java)
        } else {
            return null
        }
    }

    fun usernameToUUID(username: String): String? {
        val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
        val json = url.readText()
        if (json == "") { // invalid username
            return null
        }
        val obj = DuckDueller.gson.fromJson(json, JsonObject::class.java)
        return obj.get("id").asString
    }

}