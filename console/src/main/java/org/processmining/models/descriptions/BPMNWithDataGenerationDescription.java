package org.processmining.models.descriptions;

import org.processmining.models.bpmn_with_data.DataObjectType;
import org.processmining.models.bpmn_with_data.LoggableDataObject;
import org.processmining.models.bpmn_with_data.LoggableStringDataObject;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.DataAssociation;
import org.processmining.models.graphbased.directed.bpmn.elements.DataObject;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.*;

/**
 * Created by Ivan on 08.04.2015.
 */
public class BPMNWithDataGenerationDescription extends BasicBPMNGenerationDescription implements DescriptionWithDataObjects
{
    private BPMNDiagram diagram;
    private Collection<LoggableDataObject> dataObjects = new ArrayList<>();      //TODO where do I use them?
    private Map<BPMNNode, Collection<LoggableStringDataObject>> nodesToOutputDataObjects;
    private Map<BPMNNode, Collection<LoggableStringDataObject>> nodesToInputDataObjects;
    private Map<Gateway, Collection<LoggableStringDataObject>> gatewaysToInputDataObjects;
    private Map<DataObject, LoggableStringDataObject> dataObjectsToLoggableDataObjects = new HashMap<>();
    private Map<DataObject, Boolean> dataObjectToNecessityToSetInitialValue = new HashMap<>();
    private Map<DataObject, DataObjectType> dataObjectToDataObjectType = new HashMap<>();
    private Map<DataObject, String> dataObjectToScriptPaths = new HashMap<>();
    private Map<Activity, String> activitiesToScriptPaths;
    private Map<Gateway, String> gatewaysToScriptPaths;

    @SuppressWarnings("unchecked")
    public BPMNWithDataGenerationDescription(BPMNDiagram diagram)
    {
        this.diagram = diagram;

        boolean useResources = !diagram.getSwimlanes().isEmpty() || !diagram.getPools().isEmpty();
        setUsingResources(useResources);

        dataObjectsToLoggableDataObjects = initLoggableDataObjects(diagram);
    }

    @Override
    public Map<Gateway, String> getGatewaysToScriptPaths()
    {
        return gatewaysToScriptPaths;
    }

    @Override
    public void setDataObjectScriptPaths()
    {
        for (Map.Entry<DataObject, String> pair : dataObjectToScriptPaths.entrySet())
        {
            LoggableStringDataObject loggableObject = dataObjectsToLoggableDataObjects.get(pair.getKey());
            loggableObject.setScriptPath(pair.getValue());
        }
    }

    public void initDataAssociations()
    {
        nodesToOutputDataObjects = new HashMap<>();
        nodesToInputDataObjects = new HashMap<>();
        gatewaysToInputDataObjects = new HashMap<>();
        activitiesToScriptPaths = new HashMap<>();
        gatewaysToScriptPaths = new HashMap<>();

        for (DataAssociation dataAssociation : diagram.getDataAssociations())
        {
            BPMNNode source = dataAssociation.getSource();
            BPMNNode target = dataAssociation.getTarget();

            if (source instanceof Activity)
            {
                activitiesToScriptPaths.put((Activity) source, "");
            }
            else
            {
                if (target instanceof Activity)
                {
                    activitiesToScriptPaths.put((Activity) target, "");
                }
                else
                {
                    if (target instanceof Gateway && ((Gateway) target).getGatewayType() == Gateway.GatewayType.DATABASED)
                    {
                        gatewaysToScriptPaths.put((Gateway) target, "");
                    }
                }
            }

            if (source instanceof Activity)
            {
                handleNode(dataObjectsToLoggableDataObjects, nodesToOutputDataObjects, target, source);
            }
            else
            {
                if (target instanceof Gateway)
                {
                    Gateway targetGateway = (Gateway) target;

                    if (targetGateway.getGatewayType() == Gateway.GatewayType.DATABASED)
                    {
                        Map intermediateMap = (Map) gatewaysToInputDataObjects;
                        handleNode(dataObjectsToLoggableDataObjects, intermediateMap, source, target);
                    }
                }
                else
                {
                    handleNode(dataObjectsToLoggableDataObjects, nodesToInputDataObjects, source, target);
                }
            }
        }
    }

    private Map<DataObject, LoggableStringDataObject> initLoggableDataObjects(BPMNDiagram diagram)
    {
        Map<DataObject, LoggableStringDataObject> dataObjectsToLoggableDataObjects = new HashMap<>();

        for (DataObject dataObject : diagram.getDataObjects())
        {
            LoggableStringDataObject loggableDataObject = new LoggableStringDataObject(dataObject.getLabel());
            dataObjects.add(loggableDataObject);
            dataObjectsToLoggableDataObjects.put(dataObject, loggableDataObject);

            dataObjectToNecessityToSetInitialValue.put(dataObject, false);
            dataObjectToDataObjectType.put(dataObject, DataObjectType.STRING_OBJECT);
        }

        return dataObjectsToLoggableDataObjects;
    }

    private void handleNode(
            Map<DataObject, LoggableStringDataObject> dataObjectsToLoggableDataObjects,
            Map<BPMNNode, Collection<LoggableStringDataObject>> nodesToDataObjects,
            BPMNNode dataObjectAsNode, BPMNNode actualNode)
    {
        Collection<LoggableStringDataObject> associatedInputDataObjects = getDataObjects(actualNode, nodesToDataObjects);
        LoggableStringDataObject correspondingLoggableStringDataObject = findCorrespondingLoggableDataObject(dataObjectsToLoggableDataObjects, dataObjectAsNode);
        associatedInputDataObjects.add(correspondingLoggableStringDataObject);
    }

    private LoggableStringDataObject findCorrespondingLoggableDataObject(Map<DataObject, LoggableStringDataObject> dataObjectsToLoggableDataObjects, BPMNNode dataObjectAsNode)
    {
        DataObject dataObject = (DataObject) dataObjectAsNode;
        return dataObjectsToLoggableDataObjects.get(dataObject);
    }

    private Collection<LoggableStringDataObject> getDataObjects(BPMNNode node, Map<BPMNNode, Collection<LoggableStringDataObject>> map)
    {
        Collection<LoggableStringDataObject> dataObjectsAssociatedWithSource = map.get(node);

        if (dataObjectsAssociatedWithSource == null)
        {
            dataObjectsAssociatedWithSource = new ArrayList<>();
            map.put(node, dataObjectsAssociatedWithSource);
        }
        return dataObjectsAssociatedWithSource;
    }

    @Override
    public Collection<LoggableDataObject> getDataObjects()
    {
        return Collections.unmodifiableCollection(dataObjects);
    }

    @Override
    public Set<BPMNNode> getNodesWithOutputDataObjects()
    {
        return Collections.unmodifiableSet(nodesToOutputDataObjects.keySet());
    }

    public Collection<BPMNNode> getNodesWithInputDataObjects()
    {
        return Collections.unmodifiableSet(nodesToInputDataObjects.keySet());
    }

    @Override
    public Set<Gateway> getExclusiveGatewaysWithInputDataObjects()
    {
        return Collections.unmodifiableSet(gatewaysToInputDataObjects.keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<LoggableStringDataObject> getOutputDataObjects(BPMNNode node)
    {
        Collection<LoggableStringDataObject> associatedDataObjects;

        associatedDataObjects = (Collection) nodesToOutputDataObjects.get(node);

        if (associatedDataObjects != null)
        {
            return associatedDataObjects;
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<LoggableStringDataObject> getInputDataObjects(BPMNNode node)
    {
        Collection<LoggableStringDataObject> associatedDataObjects = (Collection) nodesToInputDataObjects.get(node);

        if (associatedDataObjects != null)
        {
            return associatedDataObjects;
        }

        associatedDataObjects = (Collection) gatewaysToInputDataObjects.get(node);

        if (associatedDataObjects != null)
        {
            return associatedDataObjects;
        }

        return new ArrayList<>();
    }

    public LoggableDataObject getLoggableDataObject(DataObject dataObject)
    {
        return dataObjectsToLoggableDataObjects.get(dataObject);
    }

    public void setLoggableDataObject(DataObject dataObject, LoggableStringDataObject loggableDataObject)
    {
        if (dataObjectsToLoggableDataObjects.containsKey(dataObject))
        {
            dataObjectsToLoggableDataObjects.put(dataObject, loggableDataObject);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect data object");
        }
    }

    public boolean isNecessaryToSetInitialValues(DataObject dataObject)
    {
        return dataObjectToNecessityToSetInitialValue.get(dataObject);
    }

    public void setNecessetyToSetInitialValue(DataObject dataObject, boolean necessity)
    {
        if (dataObjectToNecessityToSetInitialValue.containsKey(dataObject))
        {
            dataObjectToNecessityToSetInitialValue.put(dataObject, necessity);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect data object");
        }

    }

    public DataObjectType getDataObjectType(DataObject dataObject)
    {
        return dataObjectToDataObjectType.get(dataObject);
    }

    public void setDataObjectType(DataObject dataObject, DataObjectType type)
    {
        if (dataObjectToDataObjectType.containsKey(dataObject))
        {
            dataObjectToDataObjectType.put(dataObject, type);
        }
        else
        {
            throw new IllegalArgumentException("Incorrect data object");
        }
    }

    public boolean isNecessaryToSetInitialValues()
    {
        for (boolean necessityToSetInitialValue : dataObjectToNecessityToSetInitialValue.values())
        {
            if (necessityToSetInitialValue)
            {
                return true;
            }
        }

        return false;
    }

    public List<DataObject> getDataObjectWhichRequireInitialValues()
    {
        List<DataObject> dataObjects = new ArrayList<>();

        for (Map.Entry<DataObject, Boolean> keyValuePair : dataObjectToNecessityToSetInitialValue.entrySet())
        {
            if (keyValuePair.getValue())
            {
                dataObjects.add(keyValuePair.getKey());
            }
        }

        return dataObjects;
    }

    public Set<DataObject> getDataObjectsWithScripts()
    {
        return dataObjectToScriptPaths.keySet();
    }

    public void setDataObjectsWithScripts(Set<DataObject> dataObjects)
    {
        for (Iterator<Map.Entry<DataObject, String>> iterator = dataObjectToScriptPaths.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<DataObject, String> entry = iterator.next();

            if (!dataObjects.contains(entry.getKey()))
            {
                iterator.remove();
            }
        }

        for (DataObject dataObject : dataObjects)
        {
            if (!dataObjectToScriptPaths.containsKey(dataObject))
            {
                dataObjectToScriptPaths.put(dataObject, "");
            }
        }
    }

    public Map<Activity, String> getActivitiesToScriptPaths()
    {
        return activitiesToScriptPaths;
    }

    @Override
    public String getActivityScriptPath(Activity activity)
    {
        return activitiesToScriptPaths.get(activity);
    }

    public Map<DataObject, String> getDataObjectToScriptPaths()
    {
        return dataObjectToScriptPaths;
    }
}
