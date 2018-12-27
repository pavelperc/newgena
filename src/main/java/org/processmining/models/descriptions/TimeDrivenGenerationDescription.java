package org.processmining.models.descriptions;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.time_driven_behavior.GranularityTypes;
import org.processmining.models.time_driven_behavior.ResourceMapping;

import java.util.*;

/**
 * @author Ivan Shugurov
 *         Created on 11.02.2014
 */

public class TimeDrivenGenerationDescription extends GenerationDescriptionWithNoise
{
    private boolean separatingStartAndFinish = true;
    private boolean isUsingResources = true;
    private boolean isUsingComplexResourceSettings = true; //resources with groups and roles
    private boolean usingSynchronizationOnResources = true;
    private int minimumIntervalBetweenActions = 10;
    private int maximumIntervalBetweenActions = 20;
    private List<Resource> simplifiedResources = new ArrayList<Resource>();
    private NoiseDescription noiseDescription;  //TODO не нравится это - и в базовом и в наследнике храню одно и то же
    private Map<Transition, Pair<Long, Long>> time = new HashMap<Transition, Pair<Long, Long>>();
    private List<Group> resourceGroups = new ArrayList<Group>();
    private Map<Object, ResourceMapping> resourceMapping = new HashMap<Object, ResourceMapping>();
    private Calendar generationStart;

    public TimeDrivenGenerationDescription()
    {
        Calendar currentCalendar = Calendar.getInstance();
        noiseDescription = new NoiseDescription(this);
        generationStart = new GregorianCalendar(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH), currentCalendar.get(Calendar.HOUR_OF_DAY),
                currentCalendar.get(Calendar.MINUTE), currentCalendar.get(Calendar.SECOND));
    }

    public boolean isSeparatingStartAndFinish()
    {
        return separatingStartAndFinish;
    }

    public void setSeparatingStartAndFinish(boolean separatingStartAndFinish)
    {
        this.separatingStartAndFinish = separatingStartAndFinish;
    }

    public int getMinimumIntervalBetweenActions()
    {
        return minimumIntervalBetweenActions;
    }

    public void setMinimumIntervalBetweenActions(int minimumIntervalBetweenActions)//TODO стоит ли проверять разницу во времени?
    {
        if (minimumIntervalBetweenActions < 0)
        {
            throw new IllegalArgumentException("Time cannot be negative");
        }
        this.minimumIntervalBetweenActions = minimumIntervalBetweenActions;
    }

    public int getMaximumIntervalBetweenActions()
    {
        return maximumIntervalBetweenActions;
    }

    public void setMaximumIntervalBetweenActions(int maximumIntervalBetweenActions)  //TODO стоит ли проверять разницу во времени?
    {
        if (maximumIntervalBetweenActions < 0)
        {
            throw new IllegalArgumentException("Time cannot be negative");
        }
        this.maximumIntervalBetweenActions = maximumIntervalBetweenActions;
    }

    public NoiseDescription getNoiseDescription()
    {
        return noiseDescription;
    }

    public Map<Transition, Pair<Long, Long>> getTime()
    {
        return time;
    }

    public void setTime(Map<Transition, Pair<Long, Long>> time)
    {
        this.time = time;
    }

    @Override
    public boolean isUsingTime()
    {
        return true;
    }

    @Override
    public boolean isUsingResources()
    {
        return isUsingResources;
    }

    public void setUsingResources(boolean isUsingPerformers)
    {
        this.isUsingResources = isUsingPerformers;
    }

    @Override
    public boolean isUsingLifecycle()
    {
        return true;
    }

    public List<Group> getResourceGroups()
    {
        return resourceGroups;
    }

    public Map<Object, ResourceMapping> getResourceMapping()
    {
        return resourceMapping;
    }

    public boolean isUsingComplexResourceSettings()
    {
        return isUsingResources() && isUsingComplexResourceSettings;
    }

    public void setUsingComplexResourceSettings(boolean isUsingComplexResources)
    {
        this.isUsingComplexResourceSettings = isUsingComplexResources;
    }

    public List<Resource> getSimplifiedResources()
    {
        return simplifiedResources;
    }

    public Calendar getGenerationStart()
    {
        return generationStart;
    }

    public void setGenerationStart(Calendar generationStart)
    {
        this.generationStart = generationStart;
    }

    public boolean isUsingSynchronizationOnResources()
    {
        return isUsingResources() && usingSynchronizationOnResources;
    }

    public void setUsingSynchronizationOnResources(boolean synchronizationOnResources)
    {
        this.usingSynchronizationOnResources = synchronizationOnResources;
    }

    public static class NoiseDescription extends GenerationDescriptionWithNoise.NoiseDescription
    {
        private boolean usingTimestampNoise = true;
        private boolean usingLifecycleNoise = true;
        private int maxTimestampDeviation;
        private GranularityTypes granularityType = GranularityTypes.MINUTES_5;
        private boolean usingTimeGranularity = true;

        public NoiseDescription(TimeDrivenGenerationDescription description)
        {
            super(description);
        }

        public boolean isUsingTimestampNoise()
        {
            return getGenerationDescription().isUsingNoise() && usingTimestampNoise;
        }

        public void setUsingTimestampNoise(boolean usingTimestampNoise)
        {
            this.usingTimestampNoise = usingTimestampNoise;
        }

        public boolean isUsingLifecycleNoise()
        {
            return getGenerationDescription().isUsingNoise() && usingLifecycleNoise;
        }

        public void setUsingLifecycleNoise(boolean usingLifecycleNoise)
        {
            this.usingLifecycleNoise = usingLifecycleNoise;
        }

        public int getMaxTimestampDeviation()
        {
            return maxTimestampDeviation;
        }

        public void setMaxTimestampDeviation(int maxTimestampDeviation)
        {
            this.maxTimestampDeviation = maxTimestampDeviation;
        }

        public GranularityTypes getGranularityType()
        {
            return granularityType;
        }

        public void setGranularityType(GranularityTypes granularityType)
        {
            this.granularityType = granularityType;
        }

        public boolean isUsingTimeGranularity()
        {
            return getGenerationDescription().isUsingNoise() && usingTimeGranularity;
        }

        public void setUsingTimeGranularity(boolean usingTimeGranularity)
        {
            this.usingTimeGranularity = usingTimeGranularity;
        }
    }
}
