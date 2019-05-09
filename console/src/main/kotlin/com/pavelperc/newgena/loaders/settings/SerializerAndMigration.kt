package com.pavelperc.newgena.loaders.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class SettingsInfo(
        val type: String,
        val version: String
)

//@UnstableDefault
val MyJson = Json(JsonConfiguration(
        prettyPrint = true
))


fun removeComments(jsonStr: String) = jsonStr.splitToSequence("\n", "\r\n")
        .map {
            // we don't remove empty lines to be able to compute error position!!
            val ind = it.indexOf("//")
            if (ind != -1)
                it.subSequence(0, ind)
            else it
        }.joinToString("\n")


fun getSettingsInfo(jsonElement: JsonElement) = jsonElement
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



fun getPetrinetMigrators(settingsJE: JsonElement): List<Migrator> {
    
    val info = getSettingsInfo(settingsJE)
    
    if (info.type != "petrinet") {
        throw IllegalArgumentException("Settings type should be `petrinet`.")
    }
    
    val versionsToMigrators = listOf(
            "0.1" to Migrator.empty
    )
    val versions = versionsToMigrators.map { it.first }
    
    if (info.version !in versions) {
        throw IllegalArgumentException("Unknown settings version: ${info.version}." +
                "Possible versions are $versions.")
    }
    
    return versionsToMigrators
            .dropWhile { it.first != info.version }
            .map { it.second }
            .dropLast(1)
}

