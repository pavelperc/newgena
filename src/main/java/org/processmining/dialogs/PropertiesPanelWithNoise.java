package org.processmining.dialogs;

import org.processmining.models.descriptions.GenerationDescriptionWithNoise;
import ru.hse.pais.shugurov.widgets.elements.ColoredSelectableElement;
import ru.hse.pais.shugurov.widgets.elements.SelectableElement;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
public class PropertiesPanelWithNoise extends GeneralPropertiesPanel
{
    private final SelectableElement usingNoiseElement;
    private final SelectableElement removeEmptyTracesElement;

    public PropertiesPanelWithNoise(final GenerationDescriptionWithNoise description)
    {
        super(description);

        usingNoiseElement = new ColoredSelectableElement("Use noise", description.isUsingNoise());
        usingNoiseElement.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                description.setUsingNoise(usingNoiseElement.isSelected());
            }
        });

        removeEmptyTracesElement = new ColoredSelectableElement("Remove empty traces", description.isRemovingEmptyTraces());
        removeEmptyTracesElement.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                description.setRemovingEmptyTraces(removeEmptyTracesElement.isSelected());
            }
        });

        add(removeEmptyTracesElement, 3);
        add(usingNoiseElement, 3);
    }
}
