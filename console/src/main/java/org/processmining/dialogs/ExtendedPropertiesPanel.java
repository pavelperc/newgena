//package org.processmining.dialogs;
//
//import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
//import ru.hse.pais.shugurov.widgets.elements.ColoredSelectableElement;
//import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
//import ru.hse.pais.shugurov.widgets.elements.SelectableElement;
//
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.Calendar;
//
///**
// * Created by Ivan Shugurov on 30.10.2014.
// */
//public class ExtendedPropertiesPanel extends PropertiesPanelWithNoise
//{
//    private InputTextElement minimumIntervalBetweenActionsElement;
//    private InputTextElement maximumIntervalBetweenActionsElement;   //TODO пока не могу выбирать единицу измерения времени
//    private NumberPicker hoursField;
//    private NumberPicker minutesField;
//    private NumberPicker secondsField;
//    private SelectableElement separateStartAndFinishElement;
//    private SelectableElement usingResourcesElement;
//    private SelectableElement usingComplexResourcesElement;
//    private SelectableElement usingSynchronizationOnResourcesElement;
//
//    public ExtendedPropertiesPanel(final TimeDrivenGenerationDescription description)
//    {
//        super(description);
//
//        minimumIntervalBetweenActionsElement = new InputTextElement("Minimum interval", Integer.toString(description.getMinimumIntervalBetweenActions()));
//        maximumIntervalBetweenActionsElement = new InputTextElement("Maximum interval", Integer.toString(description.getMaximumIntervalBetweenActions()));
//
//        separateStartAndFinishElement = new ColoredSelectableElement("Separate start and finish", description.isSeparatingStartAndFinish());
//        separateStartAndFinishElement.addMouseListener(new MouseAdapter()
//        {
//            @Override
//            public void mouseClicked(MouseEvent e)
//            {
//                getGenerationDescription().setSeparatingStartAndFinish(separateStartAndFinishElement.isSelected());
//            }
//        });
//
//        Calendar generationStart = description.getGenerationStart();
//        hoursField = new NumberPicker("Hours ", 0, 23, generationStart.get(Calendar.HOUR_OF_DAY));
//        minutesField = new NumberPicker("Minutes ", 0, 59, generationStart.get(Calendar.MINUTE));
//        secondsField = new NumberPicker("Seconds ", 0, 59, generationStart.get(Calendar.SECOND));
//
//        usingResourcesElement = new ColoredSelectableElement("Use resources", description.isUsingResources());
//        usingComplexResourcesElement = new ColoredSelectableElement("Use complex resource settings", description.isUsingComplexResourceSettings());
//        usingComplexResourcesElement.addMouseListener(new MouseAdapter()
//        {
//            @Override
//            public void mouseClicked(MouseEvent e)
//            {
//                description.setUsingComplexResourceSettings(usingComplexResourcesElement.isSelected());
//            }
//        });
//        usingSynchronizationOnResourcesElement = new ColoredSelectableElement("Use synchronization on resources", description.isUsingSynchronizationOnResources());
//        usingSynchronizationOnResourcesElement.addMouseListener(new MouseAdapter()
//        {
//            @Override
//            public void mouseClicked(MouseEvent e)
//            {
//                description.setUsingSynchronizationOnResources(usingSynchronizationOnResourcesElement.isSelected());
//            }
//        });
//        usingResourcesElement.addMouseListener(new MouseAdapter()
//        {
//            @Override
//            public void mouseClicked(MouseEvent e)
//            {
//                description.setUsingResources(usingResourcesElement.isSelected());
//                usingComplexResourcesElement.setAvailable(usingResourcesElement.isSelected());
//                usingSynchronizationOnResourcesElement.setAvailable(usingResourcesElement.isSelected());
//                //BEGIN TODO remove надо пофиксить баг в виджетах
//                if (description.isUsingResources())
//                {
//                    if (description.isUsingComplexResourceSettings())
//                    {
//                        usingComplexResourcesElement.select();
//                    }
//                    if (description.isUsingSynchronizationOnResources())
//                    {
//                        usingSynchronizationOnResourcesElement.select();
//                    }
//                }
//                //END TODO remove
//            }
//        });
//
//        if (!description.isUsingResources())
//        {
//            usingComplexResourcesElement.setAvailable(false);
//            usingSynchronizationOnResourcesElement.setAvailable(false);
//        }
//
//        add(minimumIntervalBetweenActionsElement, 3);
//        add(maximumIntervalBetweenActionsElement, 4);
//        add(hoursField, 5);
//        add(minutesField, 6);
//        add(secondsField, 7);
//        add(separateStartAndFinishElement, 9);
//        add(usingResourcesElement, 12);
//        add(usingComplexResourcesElement, 13);
//        add(usingSynchronizationOnResourcesElement, 14);
//    }
//
//    @Override
//    protected TimeDrivenGenerationDescription getGenerationDescription()
//    {
//        return (TimeDrivenGenerationDescription) super.getGenerationDescription();
//    }
//
//    @Override
//    public boolean verify()
//    {
//        boolean isCorrect = super.verify();
//        isCorrect &= checkIntervals();
//        isCorrect &= checkGenerationStartTime();
//        return isCorrect;
//    }
//
//    private boolean checkIntervals()
//    {
//        boolean isCorrect = true;
//        int minIntervalBetweenActions = -1;
//        try
//        {
//            minIntervalBetweenActions = Integer.parseInt(minimumIntervalBetweenActionsElement.getValue());
//        } catch (NumberFormatException e)
//        {
//            isCorrect = false;
//        }
//        if (minIntervalBetweenActions < 0)
//        {
//            isCorrect = false;
//        }
//        int maxIntervalBetweenActions = -1;
//        try
//        {
//            maxIntervalBetweenActions = Integer.parseInt(maximumIntervalBetweenActionsElement.getValue());
//        } catch (NumberFormatException e)
//        {
//            isCorrect = false;
//        }
//        if (maxIntervalBetweenActions < 0 || maxIntervalBetweenActions < minIntervalBetweenActions)
//        {
//            isCorrect = false;
//        }
//        if (isCorrect)
//        {
//            TimeDrivenGenerationDescription description = getGenerationDescription();
//            description.setMinimumIntervalBetweenActions(minIntervalBetweenActions);
//            description.setMaximumIntervalBetweenActions(maxIntervalBetweenActions);
//        }
//        return isCorrect;
//    }
//
//    private boolean checkGenerationStartTime()   //TODO проверка не работает если ввести неправильные значения
//    {
//        boolean isCorrect = true;
//
//        int hours = hoursField.getValue();
//        if (hours < 0 || hours > 23)
//        {
//            isCorrect = false;
//        }
//        int minutes = minutesField.getValue();
//        if (minutes < 0 || minutes > 59)
//        {
//            isCorrect = false;
//        }
//        int seconds = secondsField.getValue();
//        if (seconds < 0 || seconds > 59)
//        {
//            isCorrect = false;
//        }
//
//        if (isCorrect)
//        {
//            Calendar generationStart = getGenerationDescription().getGenerationStart();
//            generationStart.set(Calendar.HOUR_OF_DAY, hours);
//            generationStart.set(Calendar.MINUTE, minutes);
//            generationStart.set(Calendar.SECOND, seconds);
//        }
//
//        return isCorrect;
//    }
//}
