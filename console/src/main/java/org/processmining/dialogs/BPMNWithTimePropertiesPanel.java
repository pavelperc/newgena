package org.processmining.dialogs;

import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import ru.hse.pais.shugurov.widgets.elements.ColoredSelectableElement;
import ru.hse.pais.shugurov.widgets.elements.SelectableElement;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

/**
 * Created by Ivan on 10.08.2015.
 */
public class BPMNWithTimePropertiesPanel extends GeneralPropertiesPanel
{
    private NumberPicker hoursField;
    private NumberPicker minutesField;
    private NumberPicker secondsField;
    private SelectableElement separateStartAndFinishElement;

    public BPMNWithTimePropertiesPanel(BPMNWithTimeGenerationDescription description)
    {
        super(description);

        Calendar generationStart = description.getGenerationStart();
        hoursField = new NumberPicker("Hours ", 0, 23, generationStart.get(Calendar.HOUR_OF_DAY));
        minutesField = new NumberPicker("Minutes ", 0, 59, generationStart.get(Calendar.MINUTE));
        secondsField = new NumberPicker("Seconds ", 0, 59, generationStart.get(Calendar.SECOND));

        separateStartAndFinishElement = new ColoredSelectableElement("Separate start and finish", description.isSeparatingStartAndFinish());
        separateStartAndFinishElement.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                getGenerationDescription().setSeparatingStartAndFinish(separateStartAndFinishElement.isSelected());
            }
        });

        add(hoursField);
        add(minutesField);
        add(secondsField);
        add(separateStartAndFinishElement);
    }

    @Override
    protected BPMNWithTimeGenerationDescription getGenerationDescription()
    {
        return (BPMNWithTimeGenerationDescription) super.getGenerationDescription();
    }

    public boolean verify()
    {
        checkGenerationStartTime();
        return super.verify();
    }

    private void checkGenerationStartTime()
    {

        int hours = hoursField.getValue();
        int minutes = minutesField.getValue();
        int seconds = secondsField.getValue();

        Calendar generationStart = getGenerationDescription().getGenerationStart();
        generationStart.set(Calendar.HOUR_OF_DAY, hours);
        generationStart.set(Calendar.MINUTE, minutes);
        generationStart.set(Calendar.SECOND, seconds);
    }
}
