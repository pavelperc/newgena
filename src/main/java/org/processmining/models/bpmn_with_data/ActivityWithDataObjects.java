package org.processmining.models.bpmn_with_data;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.base_bpmn.Activity;
import org.processmining.models.base_bpmn.SimpleMessageFlow;
import org.processmining.models.base_bpmn.SimpleSequenceFlow;
import org.processmining.models.base_bpmn.SubProcess;
import org.processmining.utils.BPMNLoggingSingleton;
import org.processmining.utils.BPMNWithDataLoggingSingleton;
import org.processmining.utils.python.PythonRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan on 21.04.2015.
 */
public class ActivityWithDataObjects extends Activity
{
    private String scriptPath;
    private List<LoggableStringDataObject> inputDataObjects;
    private Map<String, LoggableStringDataObject> labelsToDataObjects;

    protected ActivityWithDataObjects(
            org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity,
            SubProcess parentSubProcess, List<SimpleSequenceFlow> inputSequenceFlows,
            List<SimpleSequenceFlow> outputSequenceFlows, List<SimpleMessageFlow> inputMessageFlows,
            List<SimpleMessageFlow> outputMessageFlows, List<LoggableStringDataObject> inputDataObjects,
            List<LoggableStringDataObject> outputDataObjects,
            String scriptPath)
    {
        super(actualActivity, parentSubProcess, inputSequenceFlows, outputSequenceFlows, inputMessageFlows, outputMessageFlows);
        this.scriptPath = scriptPath;
        this.inputDataObjects = inputDataObjects;

        labelsToDataObjects = new HashMap<>();

        for (LoggableStringDataObject outDataObject : outputDataObjects)
        {
            labelsToDataObjects.put(outDataObject.getLabel(), outDataObject);
        }
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        StringBuilder argumentsBuilder = new StringBuilder();

        argumentsBuilder.append(" activity ");

        for (LoggableStringDataObject dataObject : inputDataObjects)
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(dataObject.getLabel());
            argumentsBuilder.append(':');
            argumentsBuilder.append(dataObject.read());
            argumentsBuilder.append("\" ");
        }

        argumentsBuilder.append("- ");

        for (String dataObjectLabel : labelsToDataObjects.keySet())
        {
            argumentsBuilder.append('"');
            argumentsBuilder.append(dataObjectLabel);
            argumentsBuilder.append("\" ");
        }

        String arguments = argumentsBuilder.toString();

        String response = PythonRunner.run(scriptPath, arguments); //TODO arguments
        String[] args = response.split("\"");

        for (String argument : args)
        {
            if (!argument.trim().isEmpty())
            {
                String[] keyValue = argument.split(":");
                String key = keyValue[0];
                String value = keyValue[1];

                LoggableStringDataObject dataObject = labelsToDataObjects.get(key);
                dataObject.write(value);
            }
        }

        return super.move(trace);
    }

    @Override
    protected BPMNLoggingSingleton getLoggingSingleton()
    {
        return BPMNWithDataLoggingSingleton.getBPMNWithDataSingleton();
    }


    public static class ActivityWithDataObjectsBuilder extends ActivityBuilder
    {
        private String scriptPath;
        private List<LoggableStringDataObject> inputDataObjects = new ArrayList<>();
        private List<LoggableStringDataObject> outputDataObjects = new ArrayList<>();

        public ActivityWithDataObjectsBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Activity actualActivity,
                                              String scriptPath)
        {
            super(actualActivity);
            this.scriptPath = scriptPath;
        }

        public void inputDataObject(LoggableStringDataObject dataObject)
        {
            inputDataObjects.add(dataObject);
        }

        public void outputDataObject(LoggableStringDataObject dataObject)
        {
            outputDataObjects.add(dataObject);
        }

        @Override
        public Activity build()
        {
            return new ActivityWithDataObjects(
                    actualNode, (SubProcess) parentSubProcess,
                    inputSequenceFlows, outputSequenceFlows,
                    inputMessageFlows, outputMessageFlows,
                    inputDataObjects, outputDataObjects,
                    scriptPath);
        }
    }

}
