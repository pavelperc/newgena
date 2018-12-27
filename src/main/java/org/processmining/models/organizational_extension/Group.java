package org.processmining.models.organizational_extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ivan Shugurov
 *         Created on 03.04.2014
 */
public class Group implements Comparable<Group>
{
    private String name;
    private List<Role> roles = new ArrayList<Role>();
    private List<Resource> resources = new ArrayList<Resource>();

    public Group(String name)
    {
        setName(name);
    }


    public Role createRole(String roleName)
    {
        Role role = new Role(roleName, this);
        roles.add(role);
        return role;
    }

    public Resource createResource(String resourceName, Role role)
    {
        Resource resource = new Resource(resourceName, this, role);
        resources.add(resource);
        role.addResource(resource);
        return resource;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Precondition violated in Group. Group name cannot be null");
        }
        this.name = name;
    }

    public List<Role> getRoles()
    {
        return Collections.unmodifiableList(roles);
    }

    public List<Resource> getResources()
    {
        return Collections.unmodifiableList(resources);
    }

    public void removeRole(Role role)
    {
        if (!role.getGroup().equals(this))
        {
            throw new IllegalArgumentException("Precondition violated in Group.removeRole(). Role does not math the group");
        }
        boolean wasRemoved = roles.remove(role);
        if (wasRemoved)
        {
            resources.removeAll(role.getResources());
        }
    }

    public void removeResource(Resource resource)
    {
        if (!resource.getGroup().equals(this))
        {
            throw new IllegalArgumentException("Precondition violated in Group.removeResource(). Role does not math the group");
        }
        Role role = resource.getRole();
        role.removeResource(resource);
        resources.remove(resource);
    }

    public boolean returnResource(Resource resource)
    {
        return resources.remove(resource);
    }


    @Override
    public String toString()
    {
        return name.toString();
    }

    @Override
    public int compareTo(Group o)
    {
        return name.compareTo(o.getName());
    }
}
