package bot.seven.WLR.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.IOException
import java.net.URL

object HttpUtils {

    private val gson = Gson()

    fun usernameToUUID(username: String): String? {
        var jsonResponse: String? = null
        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$username")
            jsonResponse = url.readText()

            if (jsonResponse.isBlank()) {
                return null
            }
            val obj = this.gson.fromJson(jsonResponse, JsonObject::class.java)

            if (obj.has("id") && !obj.get("id").isJsonNull) {
                obj.get("id").asString
            } else {
                null
            }
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }

}