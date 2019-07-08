package com.pavelperc.newgena.loaders.settings.migration

import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


class Migrator(val description: String, val migrate: (JsonObject) -> JsonObject) {
    operator fun invoke(settings: JsonObject) = migrate(settings)

//        operator fun plus(other: Migrator) = Migrator("$description\n${other.description}") {
//            other.migrate(migrate(it))
//        }
    
    companion object {
        val empty = Migrator("") { it }
    }
}

object Migration {
    
    /** Returns updated field map in this element. Works only with [JsonObject].
     * The initial element (this) is not modified!! */
    fun JsonElement.updated(replacer: MutableMap<String, JsonElement>.() -> Unit) =
            this.jsonObject.run {
                JsonObject(content.toMutableMap().also(replacer))
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
    
    fun noField(fieldName: String) = IllegalStateException("Migration fail: no field $fieldName.")
    
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
    
    val migrator_0_3__0_4 = Migrator("""
    0.3 -> 0.4
    Added a setting in petrinetSetup section: irArcsFromPnml.
    It is true by default, but in old settings is set as false.
    Removed maxPriority from static priorities.
""".trimIndent()) { jo ->
        jo.updated {
            setVersion("0.4")
            updateObject("petrinetSetup") {
                this["irArcsFromPnml"] = JsonPrimitive(false)
            }
            updateObject("staticPriorities", required = false) {
                remove("maxPriority")
            }
            
        }
    }
    
    
    fun selectPetrinetMigrators(version: String): List<Migrator> {
        
        val versionsToMigrators = listOf(
                "0.1" to migrator_0_1__0_2,
                "0.2" to migrator_0_2__0_3,
                "0.3" to migrator_0_3__0_4,
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
}