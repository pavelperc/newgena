package org.processmining.plugins.bpmn;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.elements.*;

/**
 * Created by Ivan on 05.05.2015.
 */
public class BPMNExampleWithMessageFlowsForArticle
{
    @Plugin(name = "BPMN example with message flows for the article", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram generateComplexModelForArticle(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("Example");

        Swimlane serverPool = diagram.addSwimlane("Server", null, SwimlaneType.POOL);
        Swimlane dbPool = diagram.addSwimlane("DB", null, SwimlaneType.POOL);

        Event serverStartEvent = diagram.addEvent("start", Event.EventType.START, Event.EventTrigger.NONE, null, serverPool, false, null);
        Event dbStartEvent = diagram.addEvent("start", Event.EventType.START, Event.EventTrigger.NONE, null, dbPool, false, null);

        Event serverEndEvent = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, serverPool, false, null);
        Event dbEndEvent = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, dbPool, false, null);

        Activity initiateProcessing = diagram.addActivity("Initiate processing", false, false, false, false, false, serverPool);
        diagram.addFlow(serverStartEvent, initiateProcessing, "");

        Activity terminateProcessing = diagram.addActivity("Terminate processing", false, false, false, false, false, serverPool);
        diagram.addFlow(terminateProcessing, serverEndEvent, "");

        Activity startTransaction = diagram.addActivity("Start transaction", false, false, false, false, false, dbPool);
        diagram.addFlow(dbStartEvent, startTransaction, "");

        Activity commitTransaction = diagram.addActivity("Commit transaction", false, false, false, false, false, dbPool);
        diagram.addFlow(commitTransaction, dbEndEvent, "");

        diagram.addMessageFlow(initiateProcessing, startTransaction, "");
        diagram.addMessageFlow(terminateProcessing, commitTransaction, "");

        Activity processing = diagram.addActivity("Processing", false, false, false, false, false, serverPool);
        Activity writeToDB = diagram.addActivity("Write to DB", false, false, false, false, false, dbPool);

        diagram.addFlow(startTransaction, writeToDB, "");
        diagram.addFlow(initiateProcessing, processing, "");
        diagram.addFlow(processing, terminateProcessing, "");
        diagram.addFlow(writeToDB, commitTransaction, "");

        diagram.addMessageFlow(startTransaction, processing, "");
        diagram.addMessageFlow(processing, writeToDB, "");

        return diagram;
    }


}
