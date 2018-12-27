package org.processmining.models.descriptions;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan Shugurov on 30.12.2014.
 */
public class ModifiedBPMNGenerationDescription extends BasicBPMNGenerationDescription
{
    public static final int MIN_PREFERENCE = 0;
    public static final int DEFAULT_PREFERENCE = 50;
    public static final int MAX_PREFERENCE = 100;

    private Map<Gateway, Map<Flow, Integer>> gatewaysToPreferences = new HashMap<Gateway, Map<Flow, Integer>>();
    private boolean useResources;

    public ModifiedBPMNGenerationDescription(BPMNDiagram diagram)
    {
        this(diagram, false);
    }

    public ModifiedBPMNGenerationDescription(BPMNDiagram diagram, boolean useResources)
    {
        this.useResources = useResources;

        for (Gateway gateway : diagram.getGateways())
        {
            if (gateway.getGatewayType() == Gateway.GatewayType.DATABASED)
            {
                @SuppressWarnings("unchecked")
                Collection<Flow> outFlows = (Collection) diagram.getOutEdges(gateway);

                if (outFlows.size() > 1)
                {
                    Map<Flow, Integer> flowToPreference = new HashMap<Flow, Integer>();

                    for (Flow flow : outFlows)
                    {
                        flowToPreference.put(flow, DEFAULT_PREFERENCE);
                    }

                    gatewaysToPreferences.put(gateway, flowToPreference);
                }
            }
        }
    }

    public boolean contains(Gateway gateway)
    {
        return gatewaysToPreferences.containsKey(gateway);
    }

    public Map<Flow, Integer> getPreferences(Gateway gateway)
    {
        return gatewaysToPreferences.get(gateway);
    }

    @Override
    public boolean isUsingResources()
    {
        return useResources;
    }
}
