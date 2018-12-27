package org.processmining.utils.helpers;

import org.processmining.models.bpmn_with_data.LoggableDataObject;
import org.processmining.models.descriptions.BPMNWithTimeAndDataDescription;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.descriptions.DescriptionWithDataObjects;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.util.Collection;

/**
 * Created by Ivan on 24.02.2016.
 */
public class BPMNWithTimeAndDataHelper extends BPMNWithTimeHelper
{
    protected BPMNWithTimeAndDataHelper(BPMNWithTimeHelperInitializer initializer, BPMNWithTimeGenerationDescription description)
    {
        super(initializer, description);
    }

    public static BPMNWithTimeAndDataHelper creteHelper(BPMNDiagram diagram, BPMNWithTimeAndDataDescription description)
    {
        BPMNWithTimeAndDataHelperInitializer initializer = new BPMNWithTimeAndDataHelperInitializer(diagram, description);
        initializer.initialize();
        return new BPMNWithTimeAndDataHelper(initializer, description);
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
