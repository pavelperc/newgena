package org.processmining.models.organizational_extension;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Ivan Shugurov
 *         Created on 02.04.2014
 */
public class Resource implements Comparable<Resource>        //TODO неправильно рабоатет с одинаковыми именами ресурсов
{
    public static final Random random = new Random();
    public static final long DEFAULT_MIN_DELAY_BETWEEN_ACTIONS = TimeUnit.MINUTES.toMillis(15);
    public static final long DEFAULT_MAX_DELAY_BETWEEN_ACTIONS = TimeUnit.MINUTES.toMillis(20);
    private long minDelayBetweenActions = DEFAULT_MIN_DELAY_BETWEEN_ACTIONS;
    private long maxDelayBetweenActions = DEFAULT_MAX_DELAY_BETWEEN_ACTIONS;
    private Group group;
    private Role role;
    private String name;
    private long willBeFreed;
    private boolean isIdle = true;

    public Resource(String name)
    {
        this(name, 0);
    }

    public Resource(String name, long willBeFreed)
    {
        setTime(willBeFreed);
        setName(name);
    }

    protected Resource(String name, Group group, Role role)
    {
        this(name, group, role, 0);
    }

    protected Resource(String name, Group group, Role role, long willBeFreed)
    {
        this(name, group, role, willBeFreed, DEFAULT_MIN_DELAY_BETWEEN_ACTIONS, DEFAULT_MAX_DELAY_BETWEEN_ACTIONS);
    }

    protected Resource(String name, Group group, Role role, long willBeFreed, long minDelayBetweenActions, long maxDelayBetweenActions)
    {
        checkTime(willBeFreed);
        if (!role.getGroup().equals(group))
        {
            throw new IllegalArgumentException("Precondition violated in Group.createResource(). Incorrect role");
        }
        setName(name);
        this.role = role;
        this.group = group;
        this.willBeFreed = willBeFreed;
        setDelayBetweenActions(minDelayBetweenActions, maxDelayBetweenActions);
    }

    public boolean isIdle()
    {
        return isIdle;
    }

    public void setIdle(boolean isIdle)
    {
        this.isIdle = isIdle;
    }

    public void setTime(long willBeFreed)//TODO подумать получше над названием
    {
        checkTime(willBeFreed);
        this.willBeFreed = willBeFreed;
        if (willBeFreed != 0)
        {
            addDelay();
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Precondition violated in Resource. Resource name cannot be null");
        }
        this.name = name;
    }

    public Group getGroup()
    {
        return group;
    }

    public Role getRole()
    {
        return role;
    }

    public long getMinDelayBetweenActions()
    {
        return minDelayBetweenActions;
    }

    public long getMaxDelayBetweenActions()
    {
        return maxDelayBetweenActions;
    }

    public void setDelayBetweenActions(long minDelayBetweenActions, long maxDelayBetweenActions)
    {
        checkTime(minDelayBetweenActions);
        checkTime(maxDelayBetweenActions);
        this.minDelayBetweenActions = minDelayBetweenActions;
        this.maxDelayBetweenActions = maxDelayBetweenActions;
    }

    private void checkTime(long delayBetweenActions)
    {
        if (delayBetweenActions < 0)
        {
            throw new IllegalArgumentException("Time cannot be negative");
        }
    }

    public void relocate(Group newGroup, Role newRole)
    {
        if (!newGroup.equals(newRole.getGroup()))
        {
            group.removeResource(this);
        }
        group = newGroup;
        role.removeResource(this);
        role = newRole;
        newRole.addResource(this);
    }

    private void addDelay()
    {
        long delay;
        long difference = maxDelayBetweenActions - minDelayBetweenActions;
        if (difference == 0)
        {
            delay = maxDelayBetweenActions;
        }
        else
        {
            delay = (random.nextLong() % (maxDelayBetweenActions - minDelayBetweenActions)) + minDelayBetweenActions;
        }
        willBeFreed += delay;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int compareTo(Resource o)
    {
        return name.compareTo(o.getName());
    }

    public void removeResource()
    {
        if (group != null)
        {
            group.removeResource(this);
        }
    }

    public long getWillBeFreed()
    {
        return willBeFreed;
    }
}
