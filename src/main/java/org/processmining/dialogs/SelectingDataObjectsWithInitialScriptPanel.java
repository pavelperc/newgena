package org.processmining.dialogs;

import org.processmining.models.graphbased.directed.bpmn.elements.DataObject;
import ru.hse.pais.shugurov.widgets.panels.MultipleChoicePanel;

import java.util.Collection;

/**
 * Created by Ivan on 15.01.2016.
 */
public class SelectingDataObjectsWithInitialScriptPanel extends MultipleChoicePanel<DataObject>
{
    public SelectingDataObjectsWithInitialScriptPanel(Collection<DataObject> options)
    {
        super(options);
    }

    public SelectingDataObjectsWithInitialScriptPanel(Collection<DataObject> options, Collection<DataObject> chosenOptions)
    {
        super(options, chosenOptions);
    }
}
