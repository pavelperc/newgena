package org.processmining.models.bpmn_data_time;

import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.bpmn_with_time.GatewayWithTimeBuilder;
import org.processmining.models.bpmn_with_time.SubProcessWithTime;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Ivan on 24.02.2016.
 */
public class GatewayWithTimeAndDataBuilder extends GatewayWithTimeBuilder
{
    private String scriptPath;
    private Collection<LoggableStringDataObject> inputDataObjects = new ArrayList<>();

    public GatewayWithTimeAndDataBuilder(Gateway actualNode, String scriptPath)
    {
        super(actualNode);
        this.scriptPath = scriptPath;
    }

    public void inputDataObject(LoggableStringDataObject dataObject)
    {
        inputDataObjects.add(dataObject);
    }

    @Override
    public org.processmining.models.base_bpmn.Gateway build()
    {
        return new ExclusiveGatewayWithTimeAndData(actualNode, scriptPath, (SubProcessWithTime) parentSubProcess, inputSequenceFlows, outputSequenceFlows, inputDataObjects);
    }
}
