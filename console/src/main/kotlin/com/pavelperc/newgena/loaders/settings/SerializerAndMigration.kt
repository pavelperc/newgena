package com.pavelperc.newgena.loaders.settings

import kotlinx.serialization.json.*
import java.io.File


val MyJson = Json(JsonConfiguration(
        prettyPrint = true,
        encodeDefaults = true
))


fun removeComments(jsonStr: String) = jsonStr.lineSequence()
        .map {
            // we don't remove empty lines to be able to compute error position!!
            val ind = it.indexOf("//")
            if (ind != -1)
                it.subSequence(0, ind)
            else it
        }.joinToString("\n")


fun getSettingsInfo(jsonElement: JsonElement) : SettingsInfo = jsonElement
        .jsonObject["settingsInfo"]?.let { MyJson.fromJson(SettingsInfo.serializer(), it) }
        ?: throw IllegalArgumentException("No field `settingsInfo` in settings.")


/** Returns updated field map in this element. Works only with [JsonObject].
 * The initial element (this) is not modified!! */
fun JsonElement.replace(replacer: (MutableMap<String, JsonElement>) -> Unit) =
        this.jsonObject.run {
            JsonObject(content.toMutableMap().also(replacer))
        }

class Migrator(val description: String, val migrate: (JsonObject) -> JsonObject) {
    operator fun invoke(settings: JsonObject) = migrate(settings)

//        operator fun plus(other: Migrator) = Migrator("$description\n${other.description}") {
//            other.migrate(migrate(it))
//        }

    companion object {
        val empty = Migrator("") { it }
    }
}


/** Updates version in json. Should be used from the root attribute map. */
fun MutableMap<String, JsonElement>.setVersion(version: String) {
    this["settingsInfo"] = getValue("settingsInfo")
            .replace { it.replace("version", JsonPrimitive(version)) }
}


const val LAST_SETTINGS_VERSION = "0.1"

fun selectPetrinetMigrators(version: String): List<Migrator> {

    val versionsToMigrators = listOf(
            LAST_SETTINGS_VERSION to Migrator.empty
    )
    val versions = versionsToMigrators.map { it.first }

    if (version !in versions) {
        throw IllegalArgumentException("Unknown settings version: $version. " +
                "Possible versions are $versions.")
    }

    return versionsToMigrators
            .dropWhile { it.first != version }
            .map { it.second }
            .dropLast(1)
}


fun JsonObject.applyMigrators(migrators: List<Migrator>, migrationCallBack: (String) -> Unit): JsonObject {
    val migrationMessage = StringBuilder("Applying migrators:")

    val migrated = migrators.fold(this) { old, migrator ->
        migrationMessage.append(migrator.description)

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

fun JsonSettings.Companion.parseJson(jsonStr: String, migrationCallBack: (String) -> Unit): JsonSettings {
    val noComments = removeComments(jsonStr)

    val jo = MyJson.parseJson(noComments).jsonObject

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


