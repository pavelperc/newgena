package org.processmining.utils.helpers;

import org.processmining.models.GenerationDescription;
import org.processmining.models.bpmn_with_data.LoggableDataObject;
import org.processmining.models.descriptions.DescriptionWithDataObjects;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.util.Collection;

/**
 * Created by Ivan on 22.04.2015.
 */
public class BPMNWithDataGenerationHelper extends SimpleBPMNHelper
{
    protected BPMNWithDataGenerationHelper(BPMNWithDataHelperInitializer initializer, DescriptionWithDataObjects description)
    {
        super(initializer, (GenerationDescription) description);
    }

    public static BPMNWithDataGenerationHelper createHelperWithData(BPMNDiagram diagram, DescriptionWithDataObjects description)
    {
        BPMNWithDataHelperInitializer initializer = new BPMNWithDataHelperInitializer(diagram, description);
        initializer.initialize();
        return new BPMNWithDataGenerationHelper(initializer, description);
    }

    @Override
    public void moveToInitialState()
    {
        super.moveToInitialState();

        DescriptionWithDataObjects description = (DescriptionWithDataObjects) getGenerationDescription();

        Collection<LoggableDataObject> allDataObjects = description.getDataObjects();

        for (LoggableDataObject dataObject : allDataObjects)
        {
            dataObject.moveToInitialState();
        }
    }
}
