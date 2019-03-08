package org.processmining.plugins.bpmn;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.elements.*;

/**
 * Created by Ivan on 11.08.2015.
 */
public class BPMNExamples
{
    @Plugin(name = "BPMN example with data 1", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram anotherExample(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("test");

        Event start = diagram.addEvent("Start", Event.EventType.START, Event.EventTrigger.NONE, null, true, null);
        Event end = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, true, null);

        Activity a = diagram.addActivity("A", false, false, false, false, false);
        Activity b = diagram.addActivity("B", false, false, false, false, false);
        Activity c = diagram.addActivity("C", false, false, false, false, false);
        Activity a1 = diagram.addActivity("A1", false, false, false, false, false);
        Activity a2 = diagram.addActivity("A2", false, false, false, false, false);
        Activity a3 = diagram.addActivity("A3", false, false, false, false, false);
        Gateway gateway1 = diagram.addGateway("data choice gateway1", Gateway.GatewayType.DATABASED);
        Gateway gateway2 = diagram.addGateway("data choice gateway2", Gateway.GatewayType.DATABASED);

        diagram.addFlow(start, a, null);
        diagram.addFlow(a, b, null);
        diagram.addFlow(a, c, null);
        diagram.addFlow(gateway1, a2, null);
        diagram.addFlow(gateway1, a1, null);
        diagram.addFlow(gateway1, a3, null);
        diagram.addFlow(a1, end, null);
        diagram.addFlow(c, gateway1, null);
        diagram.addFlow(b, gateway2, null);
        diagram.addFlow(gateway2, a1, null);
        diagram.addFlow(gateway2, a2, null);
        diagram.addFlow(gateway2, a3, null);
        diagram.addFlow(a2, end, null);
        diagram.addFlow(a3, end, null);

        DataObject dataObject1 = diagram.addDataObject(" d3");
        diagram.addDataAssociation(a, dataObject1, "");
        diagram.addDataAssociation(dataObject1, gateway1, "");
        diagram.addDataAssociation(dataObject1, gateway2, null);

        DataObject dataObject2 = diagram.addDataObject(" d2");
        diagram.addDataAssociation(dataObject2, a, "");

        DataObject dataObject3 = diagram.addDataObject(" d1");
        diagram.addDataAssociation(dataObject3, a, "");

        DataObject dataObject4 = diagram.addDataObject(" d4");
        diagram.addDataAssociation(dataObject4, gateway1, "");
        diagram.addDataAssociation(a, dataObject4, "");
        diagram.addDataAssociation(dataObject4, gateway2, null);

        DataObject dataObject5 = diagram.addDataObject(" d5");
        diagram.addDataAssociation(dataObject5, gateway1, "");
        diagram.addDataAssociation(a, dataObject5, "");
        diagram.addDataAssociation(dataObject5, gateway2, null);

        return diagram;
    }

    @Plugin(name = "BPMN example with data 2", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram anotherExampleForArticle(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("test");

        Event start = diagram.addEvent("Start", Event.EventType.START, Event.EventTrigger.NONE, null, true, null);
        Event end = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, true, null);

        Activity a = diagram.addActivity("A", false, false, false, false, false);
        Activity a1 = diagram.addActivity("A1", false, false, false, false, false);
        Activity a2 = diagram.addActivity("A2", false, false, false, false, false);
        Activity a3 = diagram.addActivity("A3", false, false, false, false, false);
        Gateway gateway1 = diagram.addGateway("data choice gateway1", Gateway.GatewayType.DATABASED);

        diagram.addFlow(start, a, null);
        diagram.addFlow(gateway1, a2, null);
        diagram.addFlow(gateway1, a1, null);
        diagram.addFlow(gateway1, a3, null);
        diagram.addFlow(a1, end, null);
        diagram.addFlow(a2, end, null);
        diagram.addFlow(a, gateway1, null);

        DataObject dataObject1 = diagram.addDataObject(" d1");
        diagram.addDataAssociation(a, dataObject1, "");
        diagram.addDataAssociation(dataObject1, gateway1, "");

        DataObject dataObject5 = diagram.addDataObject(" d2");
        diagram.addDataAssociation(dataObject5, gateway1, "");
        diagram.addDataAssociation(a, dataObject5, "");

        return diagram;
    }

    @Plugin(name = "BPMN Example with sub process and data objects", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram generateDiagramWithCancelEvent1(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("Example");
        Event startEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, true, null);
        Event endEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, true, null);

        Activity activity1 = diagram.addActivity("A1", false, false, false, false, false);
        diagram.addFlow(startEvent, activity1, "1");

        Gateway gateway1 = diagram.addGateway("g1", Gateway.GatewayType.PARALLEL);
        diagram.addFlow(activity1, gateway1, "1");

        Activity activity2 = diagram.addActivity("A2", false, false, false, false, false);
        Activity activity3 = diagram.addActivity("A3", false, false, false, false, false);

        diagram.addFlow(gateway1, activity2, "1");
        diagram.addFlow(activity2, activity3, "1");

        Gateway gateway2 = diagram.addGateway("g2", Gateway.GatewayType.PARALLEL);

        diagram.addFlow(activity3, gateway2, "1");
        diagram.addFlow(gateway2, endEvent, "1");

        SubProcess subProcess = diagram.addSubProcess("subprocess", false, false, false, false, false);

        Event subProcessStartEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, subProcess, true, null);
        Event subProcessEndEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, subProcess, true, null);

        Gateway gateway3 = diagram.addGateway("g3", Gateway.GatewayType.DATABASED, subProcess);
        Gateway gateway4 = diagram.addGateway("g4", Gateway.GatewayType.DATABASED, subProcess);

        Activity subProcessActivity1 = diagram.addActivity("SA1", false, false, false, false, false, subProcess);
        Activity subProcessActivity2 = diagram.addActivity("SA2", false, false, false, false, false, subProcess);

        diagram.addFlow(subProcessStartEvent, gateway3, "");
        diagram.addFlow(gateway3, subProcessActivity1, "");
        diagram.addFlow(subProcessActivity1, gateway4, "");
        diagram.addFlow(gateway4, subProcessActivity2, "");
        diagram.addFlow(subProcessActivity2, gateway3, "");
        diagram.addFlow(gateway4, subProcessEndEvent, "");
        diagram.addFlow(gateway1, subProcess, "");

        diagram.addFlow(subProcess, gateway2, "");

        DataObject subProcessOutputDataObject = diagram.addDataObject("final data object");
        diagram.addDataAssociation(subProcess, subProcessOutputDataObject, "");

        DataObject tempDataObject1 = diagram.addDataObject("temp1");
        diagram.addDataAssociation(tempDataObject1, subProcess, "");

        DataObject tempDataObject2 = diagram.addDataObject("temp2");
        diagram.addDataAssociation(tempDataObject2, subProcess, "");

        diagram.addDataAssociation(activity1, tempDataObject1, "");

        return diagram;
    }

    @Plugin(name = "Example BPMN", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram generateDiagram(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("Example");
        Event startEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, true, null);
        Event endEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, true, null);

        Activity activity1 = diagram.addActivity("A1", false, false, false, false, false);
        diagram.addFlow(startEvent, activity1, "1");

        Gateway gateway1 = diagram.addGateway("g1", Gateway.GatewayType.PARALLEL);
        diagram.addFlow(activity1, gateway1, "1");

        Activity activity2 = diagram.addActivity("A2", false, false, false, false, false);
        Activity activity3 = diagram.addActivity("A3", false, false, false, false, false);

        diagram.addFlow(gateway1, activity2, "1");
        diagram.addFlow(activity2, activity3, "1");

        Gateway gateway2 = diagram.addGateway("g2", Gateway.GatewayType.PARALLEL);

        diagram.addFlow(activity3, gateway2, "1");
        diagram.addFlow(gateway2, endEvent, "1");

        SubProcess subProcess = diagram.addSubProcess("subprocess", false, false, false, false, false);


        Event subprocessStartEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, subProcess, true, null);
        Event subprocessEndEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, subProcess, true, null);

        Gateway gateway3 = diagram.addGateway("g3", Gateway.GatewayType.DATABASED, subProcess);
        Gateway gateway4 = diagram.addGateway("g4", Gateway.GatewayType.DATABASED, subProcess);

        Activity subprocessActivity1 = diagram.addActivity("SA1", false, false, false, false, false, subProcess);
        Activity subprocessActivity2 = diagram.addActivity("SA2", false, false, false, false, false, subProcess);

        diagram.addFlow(subprocessStartEvent, gateway3, "1");
        diagram.addFlow(gateway3, subprocessActivity1, "1");
        diagram.addFlow(subprocessActivity1, gateway4, "1");
        diagram.addFlow(gateway4, subprocessActivity2, "1");
        diagram.addFlow(subprocessActivity2, gateway3, "1");
        diagram.addFlow(gateway4, subprocessEndEvent, "to sub-process end event");

        diagram.addFlow(gateway1, subProcess, "2");
        diagram.addFlow(subProcess, gateway2, "2");

        return diagram;
    }

    @Plugin(name = "Another example BPMN", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram anotherExample1(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("test");

        Event start = diagram.addEvent("Start", Event.EventType.START, Event.EventTrigger.NONE, null, true, null);
        Event end = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, true, null);

        Activity A = diagram.addActivity("A", false, false, false, false, false);
        Activity E = diagram.addActivity("E", false, false, false, false, false);

        SubProcess subProcess = diagram.addSubProcess("", false, false, false, false, false);
        A.setParentSubprocess(subProcess);
        E.setParentSubprocess(subProcess);

        diagram.addFlow(start, A, null);
        diagram.addFlow(A, E, null);
        diagram.addFlow(E, end, null);

        return diagram;
    }

    @Plugin(name = "example with several inputs", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram exampleWithSeveralInputs(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("Example");
        Event startEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, true, null);
        Event endEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, true, null);

        Activity activity1 = diagram.addActivity("A1", false, false, false, false, false);
        diagram.addFlow(startEvent, activity1, "");

        Gateway gateway1 = diagram.addGateway("g1", Gateway.GatewayType.PARALLEL);
        diagram.addFlow(activity1, gateway1, "");

        Activity activity2 = diagram.addActivity("A2", false, false, false, false, false);
        Activity activity3 = diagram.addActivity("A3", false, false, false, false, false);
        Activity endActivity = diagram.addActivity("end activity", false, false, false, false, false);

        Activity parallelActivity1 = diagram.addActivity("P1", false, false, false, false, false);
        Activity parallelActivity2 = diagram.addActivity("P2", false, false, false, false, false);

        diagram.addFlow(gateway1, activity2, "");
        diagram.addFlow(gateway1, parallelActivity1, "to parallel 1");
        diagram.addFlow(gateway1, parallelActivity2, "to parallel 2");


        Gateway gateway2 = diagram.addGateway("g2", Gateway.GatewayType.DATABASED);

        diagram.addFlow(activity3, gateway2, "");
        diagram.addFlow(endActivity, endEvent, "");
        diagram.addFlow(gateway2, endActivity, "");


        SubProcess subProcess = diagram.addSubProcess("subprocess", false, false, false, false, false);
        Activity activityAfterSubProcess = diagram.addActivity("After", false, false, false, false, false);


        Event subProcessStartEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, subProcess, true, null);
        Event subProcessEndEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, subProcess, true, null);

        Gateway gateway3 = diagram.addGateway("g3", Gateway.GatewayType.DATABASED, subProcess);
        Gateway gateway4 = diagram.addGateway("g4", Gateway.GatewayType.DATABASED, subProcess);

        Activity subProcessActivity1 = diagram.addActivity("SA1", false, false, false, false, false, subProcess);
        Activity subProcessActivity2 = diagram.addActivity("SA2", false, false, false, false, false, subProcess);

        diagram.addFlow(subProcessStartEvent, gateway3, "");
        diagram.addFlow(gateway3, subProcessActivity1, "");
        diagram.addFlow(subProcessActivity1, gateway4, "");
        diagram.addFlow(gateway4, subProcessActivity2, "");
        diagram.addFlow(subProcessActivity2, gateway3, "");
        diagram.addFlow(gateway4, subProcessEndEvent, "to sub-process end event");

        diagram.addFlow(gateway1, subProcess, "");
        diagram.addFlow(subProcess, activityAfterSubProcess, "");
        diagram.addFlow(activityAfterSubProcess, gateway2, "");
        diagram.addFlow(activity2, activity3, "");

        diagram.addFlow(parallelActivity1, subProcess, "");
        diagram.addFlow(parallelActivity2, subProcess, "");

        return diagram;
    }

    @Plugin(name = "BPMN Example with a cancel event", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram generateDiagramWithCancelEvent(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("Example");
        Event startEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, true, null);
        Event endEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, true, null);

        Activity activity1 = diagram.addActivity("A1", false, false, false, false, false);
        diagram.addFlow(startEvent, activity1, "1");

        Gateway gateway1 = diagram.addGateway("g1", Gateway.GatewayType.PARALLEL);
        diagram.addFlow(activity1, gateway1, "1");

        Activity activity2 = diagram.addActivity("A2", false, false, false, false, false);
        Activity activity3 = diagram.addActivity("A3", false, false, false, false, false);

        diagram.addFlow(gateway1, activity2, "1");
        diagram.addFlow(activity2, activity3, "1");

        Gateway gateway2 = diagram.addGateway("g2", Gateway.GatewayType.PARALLEL);

        diagram.addFlow(activity3, gateway2, "1");
        diagram.addFlow(gateway2, endEvent, "1");

        SubProcess subProcess = diagram.addSubProcess("subprocess", false, false, false, false, false);

        Event subProcessStartEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, subProcess, true, null);
        Event subProcessEndEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, subProcess, true, null);

        Gateway gateway3 = diagram.addGateway("g3", Gateway.GatewayType.DATABASED, subProcess);
        Gateway gateway4 = diagram.addGateway("g4", Gateway.GatewayType.DATABASED, subProcess);

        Activity subProcessActivity1 = diagram.addActivity("SA1", false, false, false, false, false, subProcess);
        Activity subProcessActivity2 = diagram.addActivity("SA2", false, false, false, false, false, subProcess);

        diagram.addFlow(subProcessStartEvent, gateway3, "1");
        diagram.addFlow(gateway3, subProcessActivity1, "1");
        diagram.addFlow(subProcessActivity1, gateway4, "1");
        diagram.addFlow(gateway4, subProcessActivity2, "1");
        diagram.addFlow(subProcessActivity2, gateway3, "1");
        diagram.addFlow(gateway4, subProcessEndEvent, "to sub-process end event");
        diagram.addFlow(gateway1, subProcess, "2");


        //cancel event
        Event cancelEvent = diagram.addEvent("intermediate cancel", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.CATCH, subProcess, true, subProcess);  //catch
        Event endCancelEvent = diagram.addEvent("end cancel", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, subProcess, true, null);

        Activity cancelActivity = diagram.addActivity("Cancellation activity", false, false, false, false, false);
        Gateway mergeGateway = diagram.addGateway("merge sub process result", Gateway.GatewayType.DATABASED);

        diagram.addFlow(gateway3, endCancelEvent, "");
        diagram.addFlow(cancelEvent, cancelActivity, "");
        diagram.addFlow(cancelActivity, mergeGateway, "");
        diagram.addFlow(mergeGateway, gateway2, "");
        diagram.addFlow(subProcess, mergeGateway, "");

        return diagram;
    }

    @Plugin(name = "BPMN Example with pools", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram exampleWithPools(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("example with a pool");

        Swimlane supplierPool = diagram.addSwimlane("Supplier", null, SwimlaneType.POOL);


        Swimlane salesLane = diagram.addSwimlane("Sales", supplierPool, SwimlaneType.LANE);
        Swimlane distributionLane = diagram.addSwimlane("Distribution", supplierPool, SwimlaneType.LANE);

        Event startEvent = diagram.addEvent("start", Event.EventType.START, null, null, salesLane, false, null);
        Event endEvent = diagram.addEvent("end", Event.EventType.END, null, null, distributionLane, false, null);

        Activity authorizePayment = diagram.addActivity("Authorize Payment", false, false, false, false, false, salesLane);
        Activity processOrder = diagram.addActivity("Process order", false, false, false, false, false, salesLane);

        Activity packGoods = diagram.addActivity("Pack Goods", false, false, false, false, false, distributionLane);
        Activity shipGoods = diagram.addActivity("Ship goods", false, false, false, false, false, distributionLane);

        diagram.addFlow(startEvent, authorizePayment, "");
        diagram.addFlow(authorizePayment, processOrder, "");
        diagram.addFlow(processOrder, packGoods, "");
        diagram.addFlow(packGoods, shipGoods, "");
        diagram.addFlow(shipGoods, endEvent, "");

        Swimlane financialPool = diagram.addSwimlane("Financial institution", null, SwimlaneType.POOL);

        Event financialStartEvent = diagram.addEvent("Start financial", Event.EventType.START, null, null, financialPool, false, null);
        Event financialEndEvent = diagram.addEvent("End financial", Event.EventType.END, null, null, financialPool, false, null);

        Activity cardAuthorizationActivity = diagram.addActivity("Credit card authorization", false, false, false, false, false, financialPool);
        diagram.addFlow(financialStartEvent, cardAuthorizationActivity, "");
        diagram.addFlow(cardAuthorizationActivity, financialEndEvent, "");

        diagram.addMessageFlow(authorizePayment, cardAuthorizationActivity, "");
        diagram.addMessageFlow(cardAuthorizationActivity, processOrder, "");

        return diagram;
    }

    @Plugin(name = "student conference", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public static BPMNDiagram generateStudentDiagram(PluginContext context)
    {
          BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Event start = diagram.addEvent("", Event.EventType.START, null, null, (Swimlane) null, true, null);
        Event end =  diagram.addEvent("", Event.EventType.END, null, null, (Swimlane) null, true, null);

        Activity a = diagram.addActivity("A", false, false, false, false, false);
        diagram.addFlow(start, a, "");


        Activity b = diagram.addActivity("B", false, false, false, false, false);
        Activity c = diagram.addActivity("C", false, false, false, false, false);
        Activity d = diagram.addActivity("D", false, false, false, false, false);

        Gateway split = diagram.addGateway("", Gateway.GatewayType.DATABASED);
        Gateway join = diagram.addGateway("", Gateway.GatewayType.DATABASED);

        diagram.addFlow(a, split, "");
        diagram.addFlow(split, b, "");
        diagram.addFlow(split, d, "");

        diagram.addFlow(b, c, "");
        diagram.addFlow(c, join, "");
        diagram.addFlow(d, join, "");

        diagram.addFlow(join, end, "");

        return diagram;
    }
}
