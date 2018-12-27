package org.processmining.models.descriptions;

import org.processmining.models.bpmn_with_data.LoggableDataObject;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ivan on 22.02.2016.
 */
public interface DescriptionWithDataObjects
{
    Collection<LoggableDataObject> getDataObjects();
    void setDataObjectScriptPaths();
    Set<BPMNNode> getNodesWithOutputDataObjects();
    String getActivityScriptPath(Activity activity);
    Collection<LoggableStringDataObject> getOutputDataObjects(BPMNNode node);
    Set<Gateway> getExclusiveGatewaysWithInputDataObjects();
    Map<Gateway, String> getGatewaysToScriptPaths();
    Collection<LoggableStringDataObject> getInputDataObjects(BPMNNode node);
}
