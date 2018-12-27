package org.processmining.dialogs;

import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import ru.hse.pais.shugurov.widgets.TypicalColors;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan Shugurov on 31.10.2014.
 */
public class StaticPrioritiesPanel extends EmptyPanel
{
    private final int MAX_PRIORITY;
    private final Map<Transition, InputTextElement> transitionsToInputElements = new HashMap<Transition, InputTextElement>();
    private final Map<Transition, Integer> priorities;

    public StaticPrioritiesPanel(Map<Transition, Integer> priorities)
    {
        if (priorities == null)
        {
            throw new IllegalArgumentException("Priorities map cannot be equal to null");
        }

        this.priorities = priorities;

        MAX_PRIORITY = priorities.size();

        for (Map.Entry<Transition, Integer> priorityEntry : priorities.entrySet())
        {
            Transition transition = priorityEntry.getKey();
            InputTextElement priorityElement = new InputTextElement(transition.getLabel(), Integer.toString(priorityEntry.getValue())); //TODo не хардкодить приоритеты
            add(priorityElement);
            transitionsToInputElements.put(transition, priorityElement);
        }

        JTextPane explanation = new JTextPane();
        StyledDocument styledDocument = explanation.getStyledDocument();
        SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        styledDocument.setParagraphAttributes(0, styledDocument.getLength(), centerAttribute, false);
        String text = String.format("Max priority: %s\nMin priority: %s", MAX_PRIORITY, 1);
        explanation.setText(text);
        explanation.setEditable(false);
        explanation.setMaximumSize(new Dimension(2000, 2000));
        explanation.setBackground(new Color(60, 60, 60));
        explanation.setForeground(TypicalColors.TEXT_COLOR);
        add(explanation);
    }

    public boolean verify()
    {
        boolean correct = true;
        for (Map.Entry<Transition, InputTextElement> entry : transitionsToInputElements.entrySet())
        {
            Transition transition = entry.getKey();
            InputTextElement inputTextElement = entry.getValue();
            String priorityString = inputTextElement.getValue();
            try
            {
                int priority = Integer.parseInt(priorityString);
                if (priority < GenerationDescriptionWithStaticPriorities.MIN_PRIORITY || priority > MAX_PRIORITY)
                {
                    correct = false;
                }
                else
                {
                    priorities.put(transition, priority);
                }
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
                correct = false;
            }
        }
        return correct;
    }
}
