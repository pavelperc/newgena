package org.processmining.dialogs;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import ru.hse.pais.shugurov.widgets.TypicalColors;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ivan on 15.01.2016.
 */
public class ScriptSelection<T extends BPMNNode> extends EmptyPanel implements Verifiable
{
    private java.util.List<Pair<InputTextElement, T>> inputElementsToNodes;
    private Map<T, String> nodesToScripts;

    public ScriptSelection(final Map<T, String> nodesToScripts)
    {
        this.nodesToScripts = nodesToScripts;
        inputElementsToNodes = new ArrayList<>();

        for (final Map.Entry<T, String> pair : nodesToScripts.entrySet())
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setBackground(TypicalColors.LIGHT_GRAY);

            final InputTextElement inputTextElement = new InputTextElement(pair.getKey().getLabel(), pair.getValue(),
                    new Dimension(300, 50));
            inputTextElement.setTextFieldSize(new Dimension(400, 50));
            panel.add(inputTextElement);

            final JButton fileButton = new JButton("choose");

            fileButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String currentPath = inputTextElement.getValue();

                    if (currentPath.isEmpty())
                    {
                        currentPath = Paths.get("").toAbsolutePath().toString();
                    }

                    JFileChooser fileChooser = new JFileChooser(currentPath);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Python file", "py");
                    fileChooser.setFileFilter(filter);

                    int returnValue = fileChooser.showOpenDialog(ScriptSelection.this);

                    if (returnValue == JFileChooser.APPROVE_OPTION)
                    {
                        String path = fileChooser.getSelectedFile().getAbsolutePath();
                        inputTextElement.setValue(path);
                    }
                }
            });

            Pair<InputTextElement, T> inputNodePair = new Pair<>(inputTextElement, pair.getKey());
            inputElementsToNodes.add(inputNodePair);

            panel.add(fileButton);

            add(panel);
        }
    }

    @Override
    public boolean verify()
    {
        boolean correct = true;

        for (Pair<InputTextElement, T> pair : inputElementsToNodes)
        {
            String path = pair.getFirst().getValue();

            if (path.isEmpty())
            {
                correct = false;
            }

            nodesToScripts.put(pair.getSecond(), path);
        }

        return correct;
    }
}
