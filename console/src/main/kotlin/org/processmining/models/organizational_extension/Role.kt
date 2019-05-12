package org.processmining.models.organizational_extension

/**
 * @author Ivan Shugurov
 * Created on 03.04.2014
 */
class Role(
        var name: String,
        var group: Group,
        resourcesCreator: (Role) -> List<Resource>
) : Comparable<Role>        //TODO неправильно рабоатет с одинаковыми именами ролей
{
    val resources = resourcesCreator(this)
    
    override fun toString() = name
    
    override fun compareTo(other: Role) = name.compareTo(other.name)
}
