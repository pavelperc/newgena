package org.processmining.models.organizational_extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Ivan Shugurov
 *         Created on 03.04.2014
 */
public class Role implements Comparable<Role>        //TODO неправильно рабоатет с одинаковыми именами ролей
{
    private final Group group;
    private String name;
    private Collection<Resource> resources;

    protected Role(String name, Group group)
    {
        setName(name);
        if (group == null)
        {
            throw new IllegalArgumentException("Precondition violated in Role. Group cannot be null");
        }
        this.group = group;
        resources = new ArrayList<Resource>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Precondition violated in Role. Role name cannot be null");
        }
        this.name = name;
    }


    public Group getGroup()
    {
        return group;
    }

    @Override
    public String toString()
    {
        return name;
    }

    void addResource(Resource resource)
    {
        resources.add(resource);
    }

    void removeResource(Resource resource)
    {
        resources.remove(resource);
    }

    public Collection<Resource> getResources()
    {
        return Collections.unmodifiableCollection(resources);
    }

    @Override
    public int compareTo(Role o)
    {
        return name.compareTo(o.getName());
    }
}
