package org.processmining.models.descriptions;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.utils.distribution.ConfiguredLongDistribution;
import org.processmining.utils.distribution.UniformDistribution;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ivan on 10.08.2015.
 */
public class BPMNWithTimeGenerationDescription extends BasicBPMNGenerationDescription
{
    private Calendar generationStart;
    private Map<Activity, ConfiguredLongDistribution> activitiesToExecutionTimeDistributions = new HashMap<>();
    private Map<Activity, String> activitiesToTimeScriptPaths = new HashMap<>();
    private boolean separatingStartAndFinish;
    private Map<Activity, Boolean> useTimeScripts = new HashMap<>();

    public BPMNWithTimeGenerationDescription(BPMNDiagram diagram)
    {
        generationStart = Calendar.getInstance();

        for (Activity activity : diagram.getActivities())
        {
            activitiesToExecutionTimeDistributions.put(activity,
                    new ConfiguredLongDistribution(new UniformDistribution(), TimeUnit.HOURS.toSeconds(1), TimeUnit.HOURS.toSeconds(2)));
        }

        boolean useResources = !diagram.getSwimlanes().isEmpty() || !diagram.getPools().isEmpty();
        setUsingResources(useResources);
    }

    public Calendar getGenerationStart()
    {
        return generationStart;
    }

    public void setGenerationStart(Calendar generationStart)
    {
        this.generationStart = generationStart;
    }

    public ConfiguredLongDistribution getExecutionTimeDistribution(Activity activity)
    {
        return activitiesToExecutionTimeDistributions.get(activity);
    }

    public void setExecutionTimeDistribution(Activity activity, ConfiguredLongDistribution distribution)
    {
        if (activitiesToExecutionTimeDistributions.containsKey(activity))
        {
            activitiesToExecutionTimeDistributions.put(activity, distribution);
        }
        else
        {
            throw new IllegalArgumentException("BPMN diagram does not contain the provided activity");
        }
    }

    public String getTimeScriptPath(Activity activity)
    {
        return activitiesToTimeScriptPaths.get(activity);
    }

    public void setTimeScriptPath(Activity activity, String path)
    {
        activitiesToTimeScriptPaths.put(activity, path);
    }

    public void removeTimeScriptPath(Activity activity)
    {
        activitiesToTimeScriptPaths.remove(activity);
    }

    public boolean isUsingTimeScript(Activity activity)
    {
        Boolean usingScript = useTimeScripts.get(activity);

        if (usingScript == null)
        {
            return false;
        }

        return usingScript;
    }

    public void setUsingTimeScript(Activity activity, boolean usingScript)
    {
        useTimeScripts.put(activity, usingScript);
    }

    @Override
    public boolean isUsingTime()
    {
        return true;
    }

    public boolean isSeparatingStartAndFinish()
    {
        return separatingStartAndFinish;
    }

    public void setSeparatingStartAndFinish(boolean separatingStartAndFinish)
    {
        this.separatingStartAndFinish = separatingStartAndFinish;
    }
}
