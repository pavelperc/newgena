package org.processmining.plugins.bpmn;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.elements.*;

/**
 * Created by Ivan on 02.05.2015.
 */
public class BPMNExampleWithCancelEventsForArticle
{
    @Plugin(name = "BPMN example with cancel events for the article", returnLabels = {"model"}, returnTypes = {BPMNDiagram.class}, parameterLabels = {})
    @UITopiaVariant(affiliation = "", email = "", author = "")
    public BPMNDiagram generateComplexModelForArticle(PluginContext context)
    {
        BPMNDiagram diagram = BPMNDiagramFactory.newBPMNDiagram("Example");

        createServerPool(diagram);

        return diagram;
    }

    protected void createServerPool(BPMNDiagram diagram)
    {
        Swimlane serverPool = diagram.addSwimlane("Server", null, SwimlaneType.POOL);

        Event serverStartEvent = diagram.addEvent("start", Event.EventType.START, Event.EventTrigger.NONE, null, serverPool, false, null);

        SubProcess generalSubProcess = createGeneraSubProcess(diagram, serverPool);
        diagram.addFlow(serverStartEvent, generalSubProcess, "");

        Event serverEndEvent = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, serverPool, false, null);
        diagram.addFlow(generalSubProcess, serverEndEvent, "");

        Event cancelEvent = diagram.addEvent("", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.CATCH, generalSubProcess, true, generalSubProcess);

        Event anotherEnd = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, serverPool, false, null);

        diagram.addFlow(cancelEvent, anotherEnd, "");
    }

    private SubProcess createGeneraSubProcess(BPMNDiagram diagram, Swimlane serverPool)
    {
        SubProcess generalSubProcess = diagram.addSubProcess("general", false, false, false, false, false, serverPool);

        DataObject paymentMethodDataObject = diagram.addDataObject("Payment method");
        DataObject insuranceDataObject = diagram.addDataObject("Insurance");

        Event generalSubProcessStartEvent = diagram.addEvent("start", Event.EventType.START, Event.EventTrigger.NONE, null, generalSubProcess, false, null);

        Gateway generalSubProcessStartGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, generalSubProcess);
        diagram.addFlow(generalSubProcessStartEvent, generalSubProcessStartGateway, "");

        SubProcess personalInformationSubProcess = createPersonalInformationSubProcess(diagram, generalSubProcess, generalSubProcessStartGateway);
        Event personalInformationCancelEvent = diagram.addEvent(
                "", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL,
                Event.EventUse.CATCH, personalInformationSubProcess, true, personalInformationSubProcess);

        SubProcess insuranceSelectionSubProcess = createInsuranceSelectionSubProcess(diagram, generalSubProcess, insuranceDataObject);
        diagram.addFlow(generalSubProcessStartGateway, insuranceSelectionSubProcess, "");

        SubProcess paymentMethodSelectionSubProcess = createPaymentMethodSelectionSubProcess(diagram, generalSubProcess, paymentMethodDataObject);
        Event paymentMethodSelectionCancelEvent = diagram.addEvent(
                "", Event.EventType.INTERMEDIATE, Event.EventTrigger.CANCEL, Event.EventUse.CATCH,
                paymentMethodSelectionSubProcess, true, paymentMethodSelectionSubProcess);

        Gateway mergeGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, generalSubProcess);
        diagram.addFlow(personalInformationSubProcess, mergeGateway, "");
        diagram.addFlow(paymentMethodSelectionSubProcess, mergeGateway, "");
        diagram.addFlow(insuranceSelectionSubProcess, paymentMethodSelectionSubProcess, "");

        SubProcess transactionProcessingSubProcess = createTransactionProcessingSubProcess(
                diagram, generalSubProcess,
                paymentMethodDataObject,
                insuranceDataObject);
        diagram.addFlow(mergeGateway, transactionProcessingSubProcess, "");

        Activity storeInDataBaseActivity = diagram.addActivity("Store in DB", false, false, false, false, false, generalSubProcess); //TODO invent a better name
        diagram.addFlow(transactionProcessingSubProcess, storeInDataBaseActivity, "");

        Activity successActivity = diagram.addActivity("Success", false, false, false, false, false, generalSubProcess);
        diagram.addFlow(storeInDataBaseActivity, successActivity, "");

        Event generalSubProcessEndEvent = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, generalSubProcess, false, null);
        diagram.addFlow(successActivity, generalSubProcessEndEvent, "");

        Event generalSubProcessCancelEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, generalSubProcess, true, null);

        diagram.addFlow(personalInformationCancelEvent, generalSubProcessCancelEvent, "");        //TODO �������� �� ��������� ����� � �������?
        diagram.addFlow(paymentMethodSelectionCancelEvent, generalSubProcessCancelEvent, "");     //TODO �������� �� ��������� ����� � �������?

        return generalSubProcess;
    }

    private SubProcess createPaymentMethodSelectionSubProcess(BPMNDiagram diagram, SubProcess generalSubProcess, DataObject paymentMethodDataObject)
    {
        DataObject something = diagram.addDataObject("something");

        SubProcess paymentMethodSelectionSubProcess = diagram.addSubProcess("Payment method selection", false, false, false, false, false, generalSubProcess);

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, paymentMethodSelectionSubProcess, false, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, paymentMethodSelectionSubProcess, false, null);
        Event cancelEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, paymentMethodSelectionSubProcess, true, null);

        Gateway gateway = diagram.addGateway("payment method gateway", Gateway.GatewayType.DATABASED, paymentMethodSelectionSubProcess);

        Activity creditCard = diagram.addActivity("Credit card", false, false, false, false, false, paymentMethodSelectionSubProcess);
        Activity paypal = diagram.addActivity("PayPal", false, false, false, false, false, paymentMethodSelectionSubProcess);
        Activity cash = diagram.addActivity("Cash", false, false, false, false, false, paymentMethodSelectionSubProcess);

        diagram.addDataAssociation(creditCard, paymentMethodDataObject, "");
        diagram.addDataAssociation(paypal, paymentMethodDataObject, "");
        diagram.addDataAssociation(cash, paymentMethodDataObject, "");

        diagram.addFlow(startEvent, gateway, "");
        diagram.addFlow(gateway, creditCard, "");
        diagram.addFlow(gateway, paypal, "");
        diagram.addFlow(gateway, cash, "");
        diagram.addFlow(gateway, cancelEvent, "");

        diagram.addFlow(creditCard, endEvent, "");
        diagram.addFlow(paypal, endEvent, "");
        diagram.addFlow(cash, endEvent, "");

        diagram.addDataAssociation(something, paymentMethodSelectionSubProcess, "");

        return paymentMethodSelectionSubProcess;
    }

    private SubProcess createTransactionProcessingSubProcess(
            BPMNDiagram diagram, SubProcess generalSubProcess,
            DataObject paymentMethodDataObject, DataObject insuranceDataObject)
    {
        SubProcess transactionProcessingSubProcess = diagram.addSubProcess("Transaction processing", false, false, false, false, false, generalSubProcess);

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, transactionProcessingSubProcess, false, null);
        Event endEvent = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, transactionProcessingSubProcess, false, null);

        Gateway startGateway = diagram.addGateway("Process depending on the selected payment method", Gateway.GatewayType.DATABASED, transactionProcessingSubProcess);
        diagram.addDataAssociation(paymentMethodDataObject, startGateway, "");

        Activity connectBank = diagram.addActivity("Connect bank", false, false, false, false, false, transactionProcessingSubProcess);
        Activity connectPaypal = diagram.addActivity("Connect PayPal", false, false, false, false, false, transactionProcessingSubProcess);
        Activity generateReceipt = diagram.addActivity("Generate receipt", false, false, false, false, false, transactionProcessingSubProcess);

        diagram.addFlow(startEvent, startGateway, "");
        diagram.addFlow(startGateway, connectBank, "");
        diagram.addFlow(startGateway, connectPaypal, "");
        diagram.addFlow(startGateway, generateReceipt, "");

        Gateway mergeGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, transactionProcessingSubProcess);

        diagram.addFlow(connectBank, mergeGateway, "");
        diagram.addFlow(connectPaypal, mergeGateway, "");
        diagram.addFlow(generateReceipt, mergeGateway, "");

        Gateway insuranceGateway = diagram.addGateway("Insurance gateway", Gateway.GatewayType.DATABASED, transactionProcessingSubProcess);
        diagram.addFlow(mergeGateway, insuranceGateway, "");
        diagram.addDataAssociation(insuranceDataObject, insuranceGateway, "");

        Activity insuranceActivity = diagram.addActivity("Purchase insurance", false, false, false, false, false, transactionProcessingSubProcess); //TODO correct phrase?
        diagram.addFlow(insuranceGateway, insuranceActivity, "");

        diagram.addFlow(insuranceActivity, endEvent, "");
        diagram.addFlow(insuranceGateway, endEvent, "");

        return transactionProcessingSubProcess;
    }

    private SubProcess createInsuranceSelectionSubProcess(BPMNDiagram diagram, SubProcess generalSubProcess, DataObject insuranceDataObject)
    {
        SubProcess insuranceSelectionSubProcess = diagram.addSubProcess("Insurance selection", false, false, false, false, false, generalSubProcess);

        Event startEvent = diagram.addEvent("", Event.EventType.START, Event.EventTrigger.NONE, null, insuranceSelectionSubProcess, false, null);
        Event endEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.NONE, null, insuranceSelectionSubProcess, false, null);

        Gateway exclusiveGateway = diagram.addGateway("", Gateway.GatewayType.DATABASED, insuranceSelectionSubProcess);

        Activity order = diagram.addActivity("Order insurance", false, false, false, false, false, insuranceSelectionSubProcess);//TODO is it correct word?
        Activity ignoreInsurance = diagram.addActivity("Ignore insurance", false, false, false, false, false, insuranceSelectionSubProcess); //TODO is it correct word?

        diagram.addFlow(startEvent, exclusiveGateway, "");
        diagram.addFlow(exclusiveGateway, order, "");
        diagram.addFlow(exclusiveGateway, ignoreInsurance, "");
        diagram.addFlow(order, endEvent, "");
        diagram.addFlow(ignoreInsurance, endEvent, "");

        diagram.addDataAssociation(order, insuranceDataObject, "");
        diagram.addDataAssociation(ignoreInsurance, insuranceDataObject, "");

        return insuranceSelectionSubProcess;
    }

    private SubProcess createPersonalInformationSubProcess(BPMNDiagram diagram, SubProcess generalSubProcess, Gateway generalSubProcessStartGateway)
    {
        SubProcess personalInformationSubProcess = diagram.addSubProcess("Personal information", false, false, false, false, false, generalSubProcess);
        diagram.addFlow(generalSubProcessStartGateway, personalInformationSubProcess, "");

        Event personalInformationSubProcessStartEvent = diagram.addEvent("start", Event.EventType.START, Event.EventTrigger.NONE, null, personalInformationSubProcess, false, null);
        Gateway personalInformationStartGateway = diagram.addGateway("personal information gateway", Gateway.GatewayType.DATABASED, personalInformationSubProcess);
        diagram.addFlow(personalInformationSubProcessStartEvent, personalInformationStartGateway, "");

        Activity cancelActivity = diagram.addActivity("cancel", false, false, false, false, false, personalInformationSubProcess);

        Event personalInformationCancelEvent = diagram.addEvent("", Event.EventType.END, Event.EventTrigger.CANCEL, Event.EventUse.THROW, personalInformationSubProcess, true, null);
        diagram.addFlow(cancelActivity, personalInformationCancelEvent, "");
        diagram.addFlow(personalInformationStartGateway, cancelActivity, "");

        Gateway startParallelGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, personalInformationSubProcess);
        diagram.addFlow(personalInformationStartGateway, startParallelGateway, "");
        Gateway mergeParallelGateway = diagram.addGateway("", Gateway.GatewayType.PARALLEL, personalInformationSubProcess);

        Activity nameActivity = diagram.addActivity("Fill in name", false, false, false, false, false, personalInformationSubProcess);
        diagram.addFlow(startParallelGateway, nameActivity, "");
        diagram.addFlow(nameActivity, mergeParallelGateway, "");

        Activity phoneNumberActivity = diagram.addActivity("Fill in phone number", false, false, false, false, false, personalInformationSubProcess);
        diagram.addFlow(startParallelGateway, phoneNumberActivity, "");
        diagram.addFlow(phoneNumberActivity, mergeParallelGateway, "");

        Activity passportActivity = diagram.addActivity("Fill in passport details", false, false, false, false, false, personalInformationSubProcess);
        diagram.addFlow(startParallelGateway, passportActivity, "");
        diagram.addFlow(passportActivity, mergeParallelGateway, "");

        Event subProcessEndEvent = diagram.addEvent("end", Event.EventType.END, Event.EventTrigger.NONE, null, personalInformationSubProcess, false, null);
        diagram.addFlow(mergeParallelGateway, subProcessEndEvent, "");

        return personalInformationSubProcess;
    }
}
