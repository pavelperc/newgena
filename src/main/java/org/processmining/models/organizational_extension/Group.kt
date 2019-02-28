package org.processmining.models.organizational_extension

/**
 * @author Ivan Shugurov
 * Created on 03.04.2014
 */
//@kotlin.ExperimentalUnsignedTypes
class Group(
        var name: String
) : Comparable<Group> {
    private val _roles = mutableListOf<Role>()
    private val _resources = mutableListOf<Resource>()
    
    
    fun createRole(roleName: String): Role {
        val role = Role(roleName, this)
        _roles.add(role)
        return role
    }
    
    fun createResource(resourceName: String, role: Role): Resource {
        val resource = Resource(resourceName, group = this, role = role)
        _resources.add(resource)
        role.resources.add(resource)
        return resource
    }
    
    val roles: List<Role>
        get() = _roles
    
    
    val resources: List<Resource>
        get() = _resources
    
    
    fun removeRole(role: Role) {
        if (role.group != this) {
            throw IllegalArgumentException("Precondition violated in Group.removeRole(). Role does not match the group")
        }
        val wasRemoved = _roles.remove(role)
        if (wasRemoved) {
            _resources.removeAll(role.resources)
        }
    }
    
    fun removeResource(resource: Resource) {
        if (resource.group != this) {
            throw IllegalArgumentException("Precondition violated in Group.removeResource(). Role does not match the group")
        }
        val role = resource.role
        role?.resources?.remove(resource)
        _resources.remove(resource)
    }
    
    override fun toString() = "Group($name)"
    override fun compareTo(other: Group) = this.name.compareTo(other.name)
}
