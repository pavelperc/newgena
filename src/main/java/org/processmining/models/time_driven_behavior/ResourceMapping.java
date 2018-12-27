package org.processmining.models.time_driven_behavior;

import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.organizational_extension.Role;

import java.util.*;

/**
 * @author Ivan Shugurov
 *         Created on 17.04.2014
 */
public class ResourceMapping
{
    private List<Group> selectedGroups = new ArrayList<Group>();
    private List<Role> selectedRoles = new ArrayList<Role>();
    private List<Resource> selectedResources = new ArrayList<Resource>();
    private List<Resource> selectedSimplifiedResources = new ArrayList<Resource>();

    public void addSelectedGroup(Group selectedGroup)
    {
        selectedGroups.add(selectedGroup);
    }

    public void removeSelectedGroup(Group selectedGroup)
    {
        selectedGroups.remove(selectedGroup);
        for (Role role : selectedGroup.getRoles())
        {
            removeSelectedRole(role);
        }
    }

    public void addSelectedRole(Role selectedRole)
    {
        selectedRoles.add(selectedRole);
    }

    public void removeSelectedRole(Role selectedRole)
    {
        boolean wasRemoved = selectedRoles.remove(selectedRole);
        if (wasRemoved)
        {
            for (Resource resource : selectedRole.getResources())
            {
                removeSelectedResources(resource);
            }
        }
    }

    private void removeAllSelectedRoles(Collection<Role> roles)
    {
        selectedRoles.removeAll(roles);
        for (Role role : roles)
        {
            removeAllSelectedResources(role.getResources());
        }
    }

    public void addSelectedResource(Resource selectedResource)
    {
        selectedResources.add(selectedResource);
    }

    public void removeSelectedResources(Resource selectedResource)
    {
        selectedResources.remove(selectedResource);
    }

    public void addSelectedSimplifiedResource(Resource selectedSimplifiedResource)
    {
        selectedSimplifiedResources.add(selectedSimplifiedResource);
    }

    public void removeSelectedSimplifiedResource(Resource simplifiedResource)
    {
        selectedSimplifiedResources.remove(simplifiedResource);
    }


    public List<Group> getSelectedGroups()
    {
        return Collections.unmodifiableList(selectedGroups);
    }

    public List<Role> getSelectedRoles()
    {
        return Collections.unmodifiableList(selectedRoles);
    }

    public List<Resource> getSelectedResources()
    {
        return Collections.unmodifiableList(selectedResources);
    }

    public List<Resource> getSelectedSimplifiedResources()
    {
        return Collections.unmodifiableList(selectedSimplifiedResources);
    }

    public void retainSelectedGroups(Collection<Group> groups)
    {
        Iterator<Group> groupIterator = selectedGroups.iterator();
        while (groupIterator.hasNext())
        {
            Group group = groupIterator.next();
            if (!groups.contains(group))
            {
                groupIterator.remove();
                removeAllSelectedRoles(group.getRoles());
            }
        }
    }

    private void removeAllSelectedResources(Collection<Resource> resources)
    {
        selectedResources.removeAll(resources);
        selectedSimplifiedResources.removeAll(resources);
    }

    public void retainSelectedRoles(Collection<Role> roles)
    {
        Iterator<Role> roleIterator = selectedRoles.iterator();
        while (roleIterator.hasNext())
        {
            Role role = roleIterator.next();
            if (!roles.contains(role))
            {
                roleIterator.remove();
                removeAllSelectedResources(role.getResources());
            }
        }
    }

    public void retainSelectedResources(Collection<Resource> resources)
    {
        selectedResources.retainAll(resources);
        selectedSimplifiedResources.retainAll(resources);
    }

}
