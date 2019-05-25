package org.processmining.models.time_driven_behavior

import org.processmining.models.organizational_extension.Group
import org.processmining.models.organizational_extension.Resource
import org.processmining.models.organizational_extension.Role

import java.util.*

/**
 * @author Ivan Shugurov
 * Created on 17.04.2014
 */
class ResourceMapping(
//        val selectedGroups: List<Group>,
//        val selectedRoles : List<Role>,
        val selectedResources : List<Resource> = emptyList(),
        val selectedSimplifiedResources : List<Resource> = emptyList()
) {
    init {
//        if (selectedResources.size + selectedSimplifiedResources.size < 1)
//            throw IllegalArgumentException("Transition should have at list one resource in ResourceMapping.")
    }
    
//    private val _selectedGroups = mutableListOf<Group>()
//    private val _selectedRoles = mutableListOf<Role>()
//    private val _selectedResources = mutableListOf<Resource>()
//    private val _selectedSimplifiedResources = mutableListOf<Resource>()
    
//    fun addSelectedGroup(selectedGroup: Group) {
//        _selectedGroups.add(selectedGroup)
//    }
//    
//    fun removeSelectedGroup(selectedGroup: Group) {
//        _selectedGroups.remove(selectedGroup)
//        for (role in selectedGroup.roles) {
//            removeSelectedRole(role)
//        }
//    }
//    
//    fun addSelectedRole(selectedRole: Role) {
//        _selectedRoles.add(selectedRole)
//    }
//    
//    fun removeSelectedRole(selectedRole: Role) {
//        val wasRemoved = _selectedRoles.remove(selectedRole)
//        if (wasRemoved) {
//            for (resource in selectedRole.resources) {
//                removeSelectedResources(resource)
//            }
//        }
//    }
//    
//    private fun removeAllSelectedRoles(roles: Collection<Role>) {
//        _selectedRoles.removeAll(roles)
//        for (role in roles) {
//            removeAllSelectedResources(role.resources)
//        }
//    }
//    
//    fun addSelectedResource(selectedResource: Resource) {
//        _selectedResources.add(selectedResource)
//    }
//    
//    fun removeSelectedResources(selectedResource: Resource) {
//        _selectedResources.remove(selectedResource)
//    }
//    
//    fun addSelectedSimplifiedResource(selectedSimplifiedResource: Resource) {
//        _selectedSimplifiedResources.add(selectedSimplifiedResource)
//    }
//    
//    fun removeSelectedSimplifiedResource(simplifiedResource: Resource) {
//        _selectedSimplifiedResources.remove(simplifiedResource)
//    }
//    
//    
//    val selectedGroups: List<Group>
//        get() = _selectedGroups
//    
//    val selectedRoles: List<Role>
//        get() = _selectedRoles
//    
//    val selectedResources: List<Resource>
//        get() = _selectedResources
//    
//    val selectedSimplifiedResources: List<Resource>
//        get() = _selectedSimplifiedResources
//    
//    fun retainSelectedGroups(groups: Collection<Group>) {
//        val groupIterator = _selectedGroups.iterator()
//        while (groupIterator.hasNext()) {
//            val group = groupIterator.next()
//            if (!groups.contains(group)) {
//                groupIterator.remove()
//                removeAllSelectedRoles(group.roles)
//            }
//        }
//    }
//    
//    private fun removeAllSelectedResources(resources: Collection<Resource>) {
//        _selectedResources.removeAll(resources)
//        _selectedSimplifiedResources.removeAll(resources)
//    }
//    
//    fun retainSelectedRoles(roles: Collection<Role>) {
//        _selectedRoles
//                .filter { it !in roles }
//                .forEach { removeAllSelectedResources(it.resources) }
//        
////        val roleIterator = _selectedRoles.iterator()
////        while (roleIterator.hasNext()) {
////            val role = roleIterator.next()
////            if (!roles.contains(role)) {
////                roleIterator.remove()
////                removeAllSelectedResources(role.resources)
////            }
////        }
//    }
//    
//    fun retainSelectedResources(resources: Collection<Resource>) {
//        _selectedResources.retainAll(resources)
//        _selectedSimplifiedResources.retainAll(resources)
//    }
    
}
