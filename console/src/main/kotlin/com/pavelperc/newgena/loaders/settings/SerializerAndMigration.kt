package com.pavelperc.newgena.loaders.settings

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


/** Returns updated field map in this element. Works only with [JsonObject].
 * The initial element (this) is not modified!! */
fun JsonElement.updated(replacer: MutableMap<String, JsonElement>.() -> Unit) =
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


/** Update JsonElement in a map.
 * @param required Update JsonElement if it exists. */
fun MutableMap<String, JsonElement>.updateElement(fieldName: String, required: Boolean = true, replacer: (element: JsonElement) -> JsonElement) {
    val field = get(fieldName)
    if (field != null) {
        this[fieldName] = replacer(field)
    } else if (required) {
        throw IllegalStateException("Not found field $fieldName.")
    }
}

/** Update jsonObject in a map. */
fun MutableMap<String, JsonElement>.updateObject(objectName: String, required: Boolean = true, replacer: MutableMap<String, JsonElement>.() -> Unit) {
    updateElement(objectName, required) { element -> element.updated { replacer() } }
}

/** Update jsonObject in a map. */
fun MutableMap<String, JsonElement>.updateObjectArray(arrayName: String, required: Boolean = true, elementReplacer: MutableMap<String, JsonElement>.() -> Unit) {
    updateElement(arrayName, required) { element ->
        JsonArray(element.jsonArray.map {
            it.updated { elementReplacer() }
        })
    }
}

/** Update all jsonObject values equally. (Like mapValues.) */
fun MutableMap<String, JsonElement>.updateObjectValues(mapName: String, required: Boolean = true, elementReplacer: MutableMap<String, JsonElement>.() -> Unit) {
    updateObject(mapName, required) {
        forEach { (k, _) ->
            updateObject(k) {
                elementReplacer()
            }
        }
    }
}


/** Updates version in json settings. Should be used from the root attribute map. */
fun MutableMap<String, JsonElement>.setVersion(version: String) {
    updateObject("settingsInfo") {
        // function from MutableMap
        replace("version", JsonPrimitive(version))
    }
}


val migrator_0_1__0_2 = Migrator("""
    0.1 -> 0.2
    Removed willBeFreed field from resources.
""".trimIndent()) { jo ->
    jo.updated {
        setVersion("0.2")
        updateObject("timeDescription", required = false) {
            updateObjectArray("resourceGroups") {
                updateObjectArray("roles") {
                    updateObjectArray("resources") {
                        remove("willBeFreed")
                    }
                }
            }
        }
        
    }
}

fun noField(fieldName: String) = IllegalStateException("no field $fieldName.")

fun <V> Map<String, V>.getSafe(fieldName: String) = get(fieldName) ?: throw noField(fieldName)

val migrator_0_2__0_3 = Migrator("""
    0.2 -> 0.3
    Resource mapping now has 4 lists:
    simplifiedResourceNames, complexResourceNames, resourceGroups, resourceRoles 
""".trimIndent()) { jo ->
    jo.updated {
        setVersion("0.3")
        updateObject("timeDescription", required = false) {
            // like mapValues
            updateObjectValues("transitionIdsToResources") {
                val oldComplexNames = getSafe("fullResourceNames").jsonArray
                        .map { it.jsonObject.getSafe("resourceName") }
                remove("fullResourceNames")
                
                this["complexResourceNames"] = JsonArray(oldComplexNames)
                this["resourceGroups"] = JsonArray(listOf())
                this["resourceRoles"] = JsonArray(listOf())
            }
        }
        
    }
}


fun selectPetrinetMigrators(version: String): List<Migrator> {
    
    val versionsToMigrators = listOf(
            "0.1" to migrator_0_1__0_2,
            "0.2" to migrator_0_2__0_3,
            JsonSettings.LAST_SETTINGS_VERSION to Migrator.empty
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


