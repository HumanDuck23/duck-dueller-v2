package bot.seven.WLR.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object WebHook {

    fun sendEmbed(webhookUrlString: String, embed: JsonObject) {
        val body = JsonObject()
        body.addProperty("content", "")
        body.addProperty("username", "WLR")
        body.addProperty("avatar_url", "https://raw.githubusercontent.com/7wlr/logos/refs/heads/main/wlr.png")

        val arr = JsonArray()
        arr.add(embed)
        body.add("embeds", arr)

        var conn: HttpsURLConnection? = null
        try {
            val targetUrl = URL(webhookUrlString)
            conn = targetUrl.openConnection() as HttpsURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            conn.addRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.addRequestProperty("User-Agent", "WLR-Bot-Webhook")
            conn.doOutput = true
            conn.requestMethod = "POST"

            DataOutputStream(conn.outputStream).use {
                val bytes = body.toString().toByteArray(Charsets.UTF_8)
                it.write(bytes)
                it.flush()
            }

            val responseCode = conn.responseCode

            val inputStream = if (responseCode < 400) conn.inputStream else conn.errorStream
            if (inputStream != null) {
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { bf ->
                    var line: String?
                    while (bf.readLine().also { line = it } != null) {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (conn != null) {
                try {
                    conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use {
                        println("Error Stream Content:")
                        it.lines().forEach { line -> println(line) }
                    } ?: println("Error stream is null.")
                } catch (connEx: Exception) {
                }
            }
        } finally {
            conn?.disconnect()
        }
    }

    fun buildEmbed(title: String, description: String, fields: JsonArray, footer: JsonObject, author: JsonObject, thumbnail: JsonObject, color: Int): JsonObject {
        val obj = JsonObject()
        obj.addProperty("title", title)
        if (description.isNotBlank())
            obj.addProperty("description", description)
        obj.addProperty("color", color)

        if (footer.entrySet().isNotEmpty()) obj.add("footer", footer)
        if (author.entrySet().isNotEmpty()) obj.add("author", author)
        if (thumbnail.entrySet().isNotEmpty()) obj.add("thumbnail", thumbnail)
        if (fields.size() > 0) obj.add("fields", fields)
        return obj
    }

    fun buildFields(fields: ArrayList<Map<String, String>>): JsonArray {
        val arr = JsonArray()
        for (field in fields) {
            val obj = JsonObject()
            field["name"]?.let { obj.addProperty("name", it) }
            field["value"]?.let { obj.addProperty("value", it) }
            obj.addProperty("inline", field["inline"] == "true")
            arr.add(obj)
        }
        return arr
    }

    fun buildAuthor(name: String, icon: String): JsonObject {
        val obj = JsonObject()
        if (name.isNotBlank()) obj.addProperty("name", name)
        if (icon.isNotBlank()) obj.addProperty("icon_url", icon)
        return obj
    }

    fun buildThumbnail(url: String): JsonObject {
        val obj = JsonObject()
        if (url.isNotBlank()) obj.addProperty("url", url)
        return obj
    }

    fun buildFooter(text: String, icon: String): JsonObject {
        val obj = JsonObject()
        if (text.isNotBlank()) obj.addProperty("text", text)
        if (icon.isNotBlank()) obj.addProperty("icon_url", icon)
        return obj
    }
}