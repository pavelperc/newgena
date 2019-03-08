package org.processmining.plugins.bpmn;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.elements.*;

/**
 * Created by Ivan on 12.03.2016.
 */
public class ExamplesForAnna
{
    @Plugin(name = "Subprocesses.bpmnN", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example1(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Activity F = diagram.addActivity("F", false, false, false, false, false, (SubProcess) null);


        SubProcess sp1 = diagram.addSubProcess("c", false, false, false, false, false);
        SubProcess a = diagram.addSubProcess("a", false, false, false, false, false, sp1);
        SubProcess b = diagram.addSubProcess("b", false, false, false, false, false, sp1);


        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        Event sp1StartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, sp1, true, null);
        Event sp1EndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, sp1, true, null);

        Activity A = diagram.addActivity("A", false, false, false, false, false, sp1);

        Gateway aGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, sp1);

        diagram.addFlow(startEvent, sp1, "");
        diagram.addFlow(sp1, F, "");
        diagram.addFlow(F, endEvent, "");

        diagram.addFlow(sp1StartEvent, A, "");
        diagram.addFlow(A, aGateway, "");
        diagram.addFlow(aGateway, b, "");
        diagram.addFlow(aGateway, a, "");

        diagram.addFlow(a, sp1EndEvent, "");
        diagram.addFlow(b, sp1EndEvent, "");


        Event aStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, a, true, null);
        Event aEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, a, true, null);

        Event bStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, b, true, null);
        Event bEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, b, true, null);

        Activity B = diagram.addActivity("B", false, false, false, false, false, a);
        Activity C = diagram.addActivity("C", false, false, false, false, false, a);

        diagram.addFlow(aStartEvent, B, "");
        diagram.addFlow(B, C, "");
        diagram.addFlow(C, aEndEvent, "");

        Activity D = diagram.addActivity("D", false, false, false, false, false, b);
        Activity E = diagram.addActivity("E", false, false, false, false, false, b);

        Gateway split = diagram.addGateway("", Gateway.GatewayType.PARALLEL, b);
        Gateway join = diagram.addGateway("", Gateway.GatewayType.PARALLEL, b);

        diagram.addFlow(bStartEvent, split, "");
        diagram.addFlow(split, D, "");
        diagram.addFlow(split, E, "");

        diagram.addFlow(D, join, "");
        diagram.addFlow(E, join, "");

        diagram.addFlow(join, bEndEvent, "");

        return diagram;
    }


    @Plugin(name = "Subprocesses2.bpmnN", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example2(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Activity F = diagram.addActivity("F", false, false, false, false, false, (SubProcess) null);


        SubProcess sp1 = diagram.addSubProcess("c", false, false, false, false, false);
        SubProcess a = diagram.addSubProcess("a", false, false, false, false, false, sp1);

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        Event sp1StartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, sp1, true, null);
        Event sp1EndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, sp1, true, null);

        Activity A = diagram.addActivity("A", false, false, false, false, false, sp1);

        Gateway aGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, sp1);

        diagram.addFlow(startEvent, sp1, "");
        diagram.addFlow(sp1, F, "");
        diagram.addFlow(F, endEvent, "");

        diagram.addFlow(sp1StartEvent, A, "");
        diagram.addFlow(A, aGateway, "");
        diagram.addFlow(aGateway, a, "");

        diagram.addFlow(a, sp1EndEvent, "");

        Event aStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, a, true, null);
        Event aEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, a, true, null);

        Activity B = diagram.addActivity("B", false, false, false, false, false, a);
        Activity C = diagram.addActivity("C", false, false, false, false, false, a);

        diagram.addFlow(aStartEvent, B, "");
        diagram.addFlow(B, C, "");
        diagram.addFlow(C, aEndEvent, "");

        Activity D = diagram.addActivity("D", false, false, false, false, false, sp1);

        diagram.addFlow(aGateway, D, "");
        diagram.addFlow(D, aGateway, "");

        return diagram;
    }

    @Plugin(name = "Subprocesses3.bpmnN", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example3(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Activity F = diagram.addActivity("F", false, false, false, false, false, (SubProcess) null);


        SubProcess sp1 = diagram.addSubProcess("c", false, false, false, false, false);
        SubProcess a = diagram.addSubProcess("a", false, false, false, false, false, sp1);
        SubProcess b = diagram.addSubProcess("b", false, false, false, false, false, sp1);


        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        Event sp1StartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, sp1, true, null);
        Event sp1EndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, sp1, true, null);

        Activity A = diagram.addActivity("A", false, false, false, false, false, sp1);

        Gateway aGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, sp1);

        diagram.addFlow(startEvent, sp1, "");
        diagram.addFlow(sp1, F, "");
        diagram.addFlow(F, endEvent, "");

        diagram.addFlow(sp1StartEvent, A, "");
        diagram.addFlow(A, aGateway, "");
        diagram.addFlow(aGateway, b, "");
        diagram.addFlow(aGateway, a, "");

        diagram.addFlow(a, sp1EndEvent, "");
        diagram.addFlow(b, sp1EndEvent, "");


        Event aStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, a, true, null);
        Event aEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, a, true, null);

        Event bStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, b, true, null);
        Event bEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, b, true, null);

        Activity B = diagram.addActivity("B", false, false, false, false, false, a);
        Activity C = diagram.addActivity("C", false, false, false, false, false, a);

        diagram.addFlow(aStartEvent, B, "");
        diagram.addFlow(B, C, "");
        diagram.addFlow(C, aEndEvent, "");

        Activity D = diagram.addActivity("D", false, false, false, false, false, b);
        Activity E = diagram.addActivity("E", false, false, false, false, false, b);

        Gateway split = diagram.addGateway("", Gateway.GatewayType.PARALLEL, b);
        Gateway join = diagram.addGateway("", Gateway.GatewayType.PARALLEL, b);

        diagram.addFlow(bStartEvent, split, "");
        diagram.addFlow(split, D, "");
        diagram.addFlow(split, E, "");

        diagram.addFlow(D, join, "");
        diagram.addFlow(E, join, "");

        diagram.addFlow(join, bEndEvent, "");

        return diagram;
    }

    @Plugin(name = "booking with cancellation", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example4(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        Activity start = diagram.addActivity("Start", false, false, false, false, false);
        Activity register = diagram.addActivity("register", false, false, false, false, false);

        SubProcess booking = diagram.addSubProcess("Booking", false, false, false, false, false);

        Activity bookCar = diagram.addActivity("Book car", false, false, false, false, false, booking);
        Activity bookHotel = diagram.addActivity("Book hotel", false, false, false, false, false, booking);
        Activity bookFlight = diagram.addActivity("Book flight", false, false, false, false, false, booking);

        Gateway split = diagram.addGateway("", Gateway.GatewayType.PARALLEL, booking);
        Gateway join = diagram.addGateway("", Gateway.GatewayType.PARALLEL, booking);

        Event bookingStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, booking, true, null);
        Event bookingEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, booking, true, null);

        diagram.addFlow(startEvent, start, "");
        diagram.addFlow(start, register, "");
        diagram.addFlow(register, booking, "");


        diagram.addFlow(bookingStartEvent, split, "");
        diagram.addFlow(split, bookCar, "");
        diagram.addFlow(split, bookHotel, "");
        diagram.addFlow(split, bookFlight, "");

        diagram.addFlow(bookCar, join, "");
        diagram.addFlow(bookHotel, join, "");
        diagram.addFlow(bookFlight, join, "");

        Gateway xorSplit = diagram.addGateway("", Gateway.GatewayType.DATABASED, booking);

        diagram.addFlow(join, xorSplit, "");

        Activity saveBooking = diagram.addActivity("Save booking", false, false, false, false, false, booking);
        Activity endBooking = diagram.addActivity("End booking", false, false, false, false, false, booking);
        Activity bookOk = diagram.addActivity("Book chotel NOK", false, false, false, false, false, booking);


        diagram.addFlow(xorSplit, saveBooking, "");
        diagram.addFlow(saveBooking, endBooking, "");
        diagram.addFlow(xorSplit, bookOk, "");

        diagram.addFlow(endBooking, bookingEndEvent, "");


        Activity end = diagram.addActivity("End", false, false, false, false, false);
        Activity cancel = diagram.addActivity("Cancel", false, false, false, false, false);
        Activity pay = diagram.addActivity("Pay", false, false, false, false, false);

        Gateway xor = diagram.addGateway("", Gateway.GatewayType.DATABASED);

        diagram.addFlow(cancel, xor, "");
        diagram.addFlow(pay, xor, "");
        diagram.addFlow(xor, end, "");
        diagram.addFlow(end, endEvent, "");

        Event endCancel = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, booking, true, null);
        Event intermediateCancel = diagram.addEvent("", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.CATCH, booking, true, booking);

        diagram.addFlow(bookOk, endCancel, "");

        diagram.addFlow(intermediateCancel, cancel, "");

        diagram.addFlow(booking, pay, "");

        return diagram;
    }


    @Plugin(name = "with cancellation", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example5(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        Activity j = diagram.addActivity("J", false, false, false, false, false);
        Activity I = diagram.addActivity("I", false, false, false, false, false);
        Activity h = diagram.addActivity("H", false, false, false, false, false);

        diagram.addFlow(startEvent, j, "");
        diagram.addFlow(I, endEvent, "");
        diagram.addFlow(h, endEvent, "");

        SubProcess b = diagram.addSubProcess("b", false, false, false, false, false);
        SubProcess a = diagram.addSubProcess("a", false, false, false, false, false, b);

        diagram.addFlow(j, b, "");
        diagram.addFlow(b, I, "");

        Event bStatEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, b, true, null);
        Event bEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, b, true, null);
        Event aStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, a, true, null);
        Event aEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, a, true, null);


        Activity A = diagram.addActivity("A", false, false, false, false, false, b);
        Activity B = diagram.addActivity("B", false, false, false, false, false, b);

        diagram.addFlow(bStatEvent, A, "");
        diagram.addFlow(A, B, "");
        diagram.addFlow(B, a, "");

        Activity C = diagram.addActivity("C", false, false, false, false, false, a);
        Activity D = diagram.addActivity("D", false, false, false, false, false, a);
        Activity E = diagram.addActivity("E", false, false, false, false, false, a);

        Gateway gateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, a);

        diagram.addFlow(aStartEvent, C, "");
        diagram.addFlow(C, gateway, "");
        diagram.addFlow(gateway, D, "");
        diagram.addFlow(gateway, E, "");
        diagram.addFlow(D, aEndEvent, "");

        Activity F = diagram.addActivity("F", false, false, false, false, false, b);
        Activity G = diagram.addActivity("G", false, false, false, false, false, b);

        Event aEndCancelEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, a, true, null);
        Event bEndCancelEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, b, true, null);

        Event aIntermediateCancelEvent = diagram.addEvent("", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.THROW, a, true, a);
        Event bIntermediateCancelEvent = diagram.addEvent("", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.THROW, b, true, b);

        diagram.addFlow(aIntermediateCancelEvent, G, "");
        diagram.addFlow(bIntermediateCancelEvent, h, "");

        diagram.addFlow(a, F, "");
        diagram.addFlow(F, bEndEvent, "");

        diagram.addFlow(G, bEndCancelEvent, "");

        diagram.addFlow(E, aEndCancelEvent, "");

        return diagram;
    }

    @Plugin(name = "picture 1", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example6(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        SubProcess a = diagram.addSubProcess("A", false, false, false, false, false);

        Activity e = diagram.addActivity("E", false, false, false, false, false, a);
        Activity f = diagram.addActivity("F", false, false, false, false, false, a);

        Activity c = diagram.addActivity("C", false, false, false, false, false);

        Event aStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, a, true, null);
        Event aEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, a, true, null);

        diagram.addFlow(aStartEvent, e, "");
        diagram.addFlow(e, f, "");
        diagram.addFlow(f, aEndEvent, "");

        diagram.addFlow(startEvent, a, "");

        diagram.addFlow(a, c, "");
        diagram.addFlow(c, endEvent, "");

        DataObject b = diagram.addDataObject("B");
        DataObject d = diagram.addDataObject("D");

        diagram.addDataAssociation(a, b, "");
        diagram.addDataAssociation(b, c, "");
        diagram.addDataAssociation(c, d, "");

        return diagram;
    }

    @Plugin(name = "picture 2", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram example7(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("");

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, (Swimlane) null, true, null);

        SubProcess c = diagram.addSubProcess("c", false, false, false, false, false);
        SubProcess a = diagram.addSubProcess("a", false, false, false, false, false, c);
        SubProcess b = diagram.addSubProcess("b", false, false, false, false, false, c);


        diagram.addFlow(startEvent, c, "");


        Activity A = diagram.addActivity("A", false, false, false, false, false, c);
        Activity B = diagram.addActivity("B", false, false, false, false, false, a);
        Activity C = diagram.addActivity("C", false, false, false, false, false, a);

        Activity D = diagram.addActivity("D", false, false, false, false, false, b);
        Activity E = diagram.addActivity("E", false, false, false, false, false, b);

        Activity F = diagram.addActivity("F", false, false, false, false, false);

        diagram.addFlow(c, F, "");
        diagram.addFlow(F, endEvent, "");


        diagram.addFlow(B, C, "");
        diagram.addFlow(D, E, "");

        Event cStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, c, true, null);
        Event aStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, a, true, null);
        Event bStartEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, b, true, null);

        Event cEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, c, true, null);
        Event aEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, a, true, null);
        Event bEndEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, b, true, null);

        diagram.addFlow(cStartEvent, A, "");
        diagram.addFlow(aStartEvent, B, "");
        diagram.addFlow(bStartEvent, D, "");
        diagram.addFlow(C, aEndEvent, "");
        diagram.addFlow(E, bEndEvent, "");

        diagram.addFlow(a, cEndEvent, "");
        diagram.addFlow(b, cEndEvent, "");

        Gateway split = diagram.addGateway("", Gateway.GatewayType.PARALLEL, c);

        diagram.addFlow(A, split, "");
        diagram.addFlow(split, a, "");
        diagram.addFlow(split, b, "");

        return diagram;
    }
}
