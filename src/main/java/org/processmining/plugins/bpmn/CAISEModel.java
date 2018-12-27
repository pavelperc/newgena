package org.processmining.plugins.bpmn;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.elements.*;

/**
 * Created by Ivan on 05.02.2016.
 */
public class CAISEModel
{

    @Plugin(name = "CAISE BPMN", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram model(UIPluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("CAISE");

        Swimlane customerPool = diagram.addSwimlane("Customer", null, SwimlaneType.POOL);
        Swimlane architectureTeamPool = diagram.addSwimlane("Architecture Team", null, SwimlaneType.POOL);

        Event customerStartEvent = diagram.addEvent("Start Event", Event.EventType.START, Event.EventTrigger.NONE, null, customerPool, false, null);
        Activity newRequirementFormulated = diagram.addActivity("New Requirement Formulated", false, false, false, false, false, customerPool);
        Activity waitForProposal = diagram.addActivity("Wait for Proposal", false, false, false, false, false, customerPool);
        Activity receiveProposal = diagram.addActivity("Receive Proposal", false, false, false, false, false, customerPool);
        Gateway proposalGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, customerPool);
        Activity acceptProposal = diagram.addActivity("Accept Proposal", false, false, false, false, false, customerPool);
        Activity receiveReport = diagram.addActivity("Receive Report", false, false, false, false, false, customerPool);
        Activity rejectProposal = diagram.addActivity("reject Proposal", false, false, false, false, false, customerPool);
        Activity requestRejectedByCompany = diagram.addActivity("Request Rejected by Developer Company", false, false, false, false, false, customerPool);
        Activity analyzeResults = diagram.addActivity("Analyze Results", false, false, false, false, false, customerPool);
        Gateway responseGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, customerPool);
        Event customerEndEvent = diagram.addEvent("End Event", Event.EventType.END, null, null, customerPool, false, null);
        Gateway proposalDecisionGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, customerPool);

        Swimlane analystLane = diagram.addSwimlane("Analyst", architectureTeamPool, SwimlaneType.LANE);
        Swimlane architect = diagram.addSwimlane("Architect", architectureTeamPool, SwimlaneType.LANE);

        Event analystStartEvent = diagram.addEvent("Start Event", Event.EventType.START, null, null, analystLane, false, null);
        Activity newRequirementsRequest = diagram.addActivity("New Requirements Request", false, false, false, false, false, analystLane);
        SubProcess requirementCheck = diagram.addSubProcess("Requirement Check", false, false, false, false, false, false, analystLane);
        Event requirementCheckStartEvent = diagram.addEvent("Start Event", Event.EventType.START, null, null, requirementCheck, false, null);
        Gateway requirementCheckSplitGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, requirementCheck);
        Activity proposeChangesOfInternalFuncReqs = diagram.addActivity("Propose Changes of Internal Func Reqs", false, false, false, false, false, requirementCheck);
        Activity proposeChangesOfInternalNonFuncReqs = diagram.addActivity("Propose Changes of Internal Non-Func Reqs", false, false, false, false, false, requirementCheck);
        Gateway requirementCheckJoinGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, requirementCheck);
        Activity adjustRequirementChanges = diagram.addActivity("Adjust Requirement Changes", false, false, false, false, false, requirementCheck);
        Event requirementCheckEndEvent = diagram.addEvent("End Event", Event.EventType.END, null, null, requirementCheck, false, null);

        Activity checkRequirementChanges = diagram.addActivity("Check Requirements Changes", false, false, false, false, false, architect);
        Activity proposeArchitecturalChanges = diagram.addActivity("Propose Architectural Changes", false, false, false, false, false, architect);

        SubProcess calculateCostSubProcess = diagram.addSubProcess("Calculate Cost sub-process", false, false, false, false, false, false, analystLane);
        Event calculateCostSubProcessStartEvent = diagram.addEvent("Start Event", Event.EventType.START, null, null, calculateCostSubProcess, false, null);
        Event calculateCostSubProcessEndEvent = diagram.addEvent("End Event", Event.EventType.END, null, null, calculateCostSubProcess, false, null);
        Activity calculateCost = diagram.addActivity("Calculate Cost", false, false, false, false, false, calculateCostSubProcess);
        Gateway calculateCostGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, calculateCostSubProcess);
        Activity sendProposal = diagram.addActivity("Send Proposal", false, false, false, false, false, calculateCostSubProcess);
        Event endCancelEvent = diagram.addEvent("Cancel", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, calculateCostSubProcess, false, null);
        Gateway analystGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, analystLane);


        Activity proposalRejected = diagram.addActivity("Proposal Rejected", false, false, false, false, false, analystLane);


        Activity rejectRequest = diagram.addActivity("Reject Request", false, false, false, false, false, analystLane);
        Event intermediateEndEvent = diagram.addEvent("Cancel", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.CATCH, calculateCostSubProcess, false, calculateCostSubProcess);
        Activity approveChanges = diagram.addActivity("Approve Changes", false, false, false, false, false, analystLane);
        Activity redesignArchitecture = diagram.addActivity("Redesign Architecture", false, false, false, false, false, architect);
        Activity sendReport = diagram.addActivity("Send Report", false, false, false, false, false, architect);

        Event architectureEndEvent = diagram.addEvent("End event", Event.EventType.END, null, null, analystLane, false, null);

        DataObject customerCostDataObject = diagram.addDataObject("Cost for customer");
        DataObject functionalRequirements = diagram.addDataObject("Functional requirements");
        DataObject durationDataObject = diagram.addDataObject("Duration");
        DataObject cost = diagram.addDataObject("Cost");
        DataObject totalBudget = diagram.addDataObject("Total budget");

        diagram.addFlow(customerStartEvent, newRequirementFormulated, "");
        diagram.addMessageFlow(newRequirementFormulated, newRequirementsRequest, "");
        diagram.addFlow(newRequirementFormulated, waitForProposal, "");
        diagram.addFlow(waitForProposal, proposalGateway, "");
        diagram.addFlow(proposalGateway, receiveProposal, "");
        diagram.addFlow(proposalGateway, requestRejectedByCompany, "");
        diagram.addMessageFlow(sendProposal, receiveProposal, "");
        diagram.addFlow(receiveProposal, proposalDecisionGateway, "");
        diagram.addFlow(proposalDecisionGateway, acceptProposal, "");
        diagram.addFlow(proposalDecisionGateway, rejectProposal, "");
        diagram.addFlow(acceptProposal, receiveReport, "");
        diagram.addMessageFlow(acceptProposal, approveChanges, "");
        diagram.addFlow(rejectProposal, responseGateway, "");
        diagram.addMessageFlow(rejectProposal, proposalRejected, "");
        diagram.addFlow(receiveReport, responseGateway, "");
        diagram.addMessageFlow(sendReport, receiveReport, "");
        diagram.addFlow(responseGateway, analyzeResults, "");
        diagram.addFlow(analyzeResults, customerEndEvent, "");
        diagram.addMessageFlow(rejectRequest, requestRejectedByCompany, "");
        diagram.addFlow(requestRejectedByCompany, responseGateway, "");


        diagram.addFlow(analystStartEvent, newRequirementsRequest, "");
        diagram.addFlow(newRequirementsRequest, requirementCheck, "");

        diagram.addFlow(requirementCheckStartEvent, requirementCheckSplitGateway, "");
        diagram.addFlow(requirementCheckSplitGateway, proposeChangesOfInternalFuncReqs, "");
        diagram.addFlow(requirementCheckSplitGateway, proposeChangesOfInternalNonFuncReqs, "");
        diagram.addFlow(proposeChangesOfInternalFuncReqs, requirementCheckJoinGateway, "");
        diagram.addFlow(proposeChangesOfInternalNonFuncReqs, requirementCheckJoinGateway, "");
        diagram.addFlow(requirementCheckJoinGateway, adjustRequirementChanges, "");
        diagram.addFlow(adjustRequirementChanges, requirementCheckEndEvent, "");


        diagram.addFlow(requirementCheck, checkRequirementChanges, "");
        diagram.addFlow(checkRequirementChanges, proposeArchitecturalChanges, "");
        diagram.addFlow(proposeArchitecturalChanges, calculateCostSubProcess, "");

        diagram.addFlow(calculateCostSubProcessStartEvent, calculateCost, "");
        diagram.addFlow(calculateCost, calculateCostGateway, "");
        diagram.addFlow(calculateCostGateway, endCancelEvent, "");
        diagram.addFlow(calculateCostGateway, sendProposal, "");
        diagram.addFlow(sendProposal, calculateCostSubProcessEndEvent, "");
        diagram.addFlow(intermediateEndEvent, rejectRequest, "");

        diagram.addFlow(calculateCostSubProcess, analystGateway, "");
        diagram.addFlow(analystGateway, approveChanges, "");
        diagram.addFlow(analystGateway, proposalRejected, "");
        diagram.addFlow(proposalRejected, architectureEndEvent, "");
        diagram.addFlow(approveChanges, architectureEndEvent, "");
        diagram.addFlow(approveChanges, redesignArchitecture, "");
        diagram.addFlow(redesignArchitecture, sendReport, "");

        diagram.addDataAssociation(receiveProposal, customerCostDataObject, "");
        diagram.addDataAssociation(customerCostDataObject, proposalDecisionGateway, "");
        diagram.addDataAssociation(functionalRequirements, proposeChangesOfInternalFuncReqs, "");
        diagram.addDataAssociation(proposeArchitecturalChanges, durationDataObject, "");
        diagram.addDataAssociation(durationDataObject, calculateCost, "");
        diagram.addDataAssociation(calculateCost, cost, "");
        diagram.addDataAssociation(cost, calculateCostGateway, "");
        diagram.addDataAssociation(cost, sendProposal, "");
        diagram.addDataAssociation(cost, approveChanges, "");
        diagram.addDataAssociation(approveChanges, totalBudget, "");


        return diagram;
    }
}
