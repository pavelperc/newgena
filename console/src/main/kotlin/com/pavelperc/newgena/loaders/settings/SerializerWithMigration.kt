package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import com.pavelperc.newgena.loaders.settings.jsonSettings.SettingsInfo
import com.pavelperc.newgena.loaders.settings.migration.Migration.selectPetrinetMigrators
import com.pavelperc.newgena.loaders.settings.migration.Migrator
import kotlinx.serialization.json.*
import java.io.File


val MyJson = Json(JsonConfiguration(
        prettyPrint = true,
        encodeDefaults = true,
        indent = "  "
))


fun removeComments(jsonStr: String) = jsonStr.lineSequence()
        .map {
            // we don't remove empty lines to be able to compute error position!!
            val ind = it.indexOf("//")
            if (ind != -1)
                it.subSequence(0, ind)
            else it
        }.joinToString("\n")


fun getSettingsInfo(jsonElement: JsonElement): SettingsInfo = jsonElement
        .jsonObject["settingsInfo"]?.let { MyJson.fromJson(SettingsInfo.serializer(), it) }
        ?: throw IllegalArgumentException("No field `settingsInfo` in settings.")


fun JsonObject.applyMigrators(migrators: List<Migrator>, migrationCallBack: (String) -> Unit): JsonObject {
    val migrationMessage = StringBuilder("Applying migrators:\n")
    
    val migrated = migrators.fold(this) { old, migrator ->
        migrationMessage.append(migrator.description + "\n")
        
        try {
            migrator(old) // return to fold
        } catch (e: Exception) {
            throw IllegalStateException("Error in settings migration: ${e.message}\n" +
                    "Migration description:\n" +
                    migrator.description)
        }
    }
    
    migrationCallBack(migrationMessage.toString())
    return migrated
}

/** Parses json with more clear exception. */
fun Json.parseJsonClear(str: String) = try {
    this.parseJson(str)
} catch (e: JsonParsingException) {
    val regex = Regex("Invalid JSON at (\\d+)")
    val position = regex.find(e.message ?: "")?.groupValues?.get(1)?.toIntOrNull() ?: -1
    
    if (position == -1) {
        throw e
    } else {
        val errorLine = str.substring(0, position).lines().last()
        throw IllegalStateException("${e.message}\nError line: $errorLine.")
    }
}

fun JsonSettings.Companion.fromJson(
        jsonStr: String,
        migrationCallBack: (String) -> Unit = { println(it) }
): JsonSettings {
    val noComments = removeComments(jsonStr)
    
    val jo = MyJson.parseJsonClear(noComments).jsonObject
    
    val info = getSettingsInfo(jo)
    
    if (info.type != "petrinet") {
        throw IllegalArgumentException("Settings type should be `petrinet`.")
    }
    
    val migrators = selectPetrinetMigrators(info.version)
    
    val migrated = if (migrators.isEmpty())
        jo
    else jo.applyMigrators(migrators, migrationCallBack)
    
    
    return MyJson.fromJson(JsonSettings.serializer(), migrated)
}


fun JsonSettings.Companion.fromFilePath(
        filePath: String,
        migrationCallBack: (String) -> Unit = { println(it) }
): JsonSettings {
    val jsonSettingsStr = File(filePath).readText()
    return JsonSettings.fromJson(jsonSettingsStr, migrationCallBack)
}

fun JsonSettings.toJson() = MyJson.stringify(JsonSettings.serializer(), this)


