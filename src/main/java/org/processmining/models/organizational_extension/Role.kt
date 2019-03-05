package org.processmining.models.organizational_extension

/**
 * @author Ivan Shugurov
 * Created on 03.04.2014
 */
class Role(
        var name: String,
        var group: Group,
        resourcesCreator: (Group, Role) -> List<Resource>
) : Comparable<Role>        //TODO неправильно рабоатет с одинаковыми именами ролей
{
    val resources = resourcesCreator(group, this)
    
    override fun toString() = "Role($name)"
    
    override fun compareTo(other: Role) = name.compareTo(other.name)
}
