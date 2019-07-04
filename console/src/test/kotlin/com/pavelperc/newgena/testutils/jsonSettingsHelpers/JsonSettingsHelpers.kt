package com.pavelperc.newgena.testutils.jsonSettingsHelpers

import com.pavelperc.newgena.loaders.settings.JsonResources
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonTimeDescription


fun JsonTimeDescription.delayNoDeviation(vararg delays: Pair<String, Long>) =
        delays.toMap().mapValues { JsonTimeDescription.DelayWithDeviation(it.value, 0L) }.toMutableMap()


/** Converts groups, roles, res separated by colon to complex resources: "g1:r1:res1". */
fun JsonTimeDescription.fastGroups(vararg fullRes: String): MutableList<JsonResources.Group> {
    val groups = mutableListOf<JsonResources.Group>()
    
    fullRes.forEach { oneFullRes ->
        val (groupName, roleName, resName) = oneFullRes.split(":")
        
        val group = groups.firstOrNull { it.name == groupName }
                ?: JsonResources.Group(groupName).also { groups.add(it) }
        
        val role = group.roles.firstOrNull { it.name == roleName }
                ?: JsonResources.Role(roleName).also { group.roles.add(it) }
        
        role.resources.add(JsonResources.Resource(resName, 0, 0))
    }
    return groups
    
}

fun JsonTimeDescription.complexResourceMapping(vararg res: String) =
        JsonResources.JsonResourceMapping(complexResourceNames = res.toMutableList())