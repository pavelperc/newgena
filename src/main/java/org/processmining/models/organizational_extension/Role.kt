package org.processmining.models.organizational_extension

/**
 * @author Ivan Shugurov
 * Created on 03.04.2014
 */
class Role(
        var name: String,
        var group: Group
) : Comparable<Role>        //TODO неправильно рабоатет с одинаковыми именами ролей
{
    val resources = mutableListOf<Resource>()
    
    
    override fun toString() = "Role($name)"
    
    override fun compareTo(other: Role) = name.compareTo(other.name)
}
