package org.processmining.dialogs;

import com.toedter.components.JSpinField;
import ru.hse.pais.shugurov.widgets.TypicalColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Ivan Shugurov
 *         Created  05.09.2014
 */
public class NumberPicker extends JPanel
{
    private JSpinField spinner;
    private MouseListener mouseListener;

    public NumberPicker(String title, int minValue, int maxValue, int initialValue)
    {
        if (minValue > maxValue)
        {
            throw new IllegalArgumentException("Min value cannot be bigger than max value");
        }
        if (initialValue < minValue)
        {
            throw new IllegalArgumentException("Initial value cannot be less than min value");
        }
        if (initialValue > maxValue)
        {
            throw new IllegalArgumentException("Initial value cannot be bigger than max value");
        }
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(TypicalColors.LIGHT_GRAY);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setOpaque(false);
        titleLabel.setForeground(TypicalColors.TEXT_COLOR);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(15f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        add(titleLabel);
        spinner = new JSpinField(minValue, maxValue);
        spinner.setValue(initialValue);
        spinner.setMaximumSize(new Dimension(91, 50));
        spinner.setMinimumSize(new Dimension(91, 50));
        spinner.setPreferredSize(new Dimension(91, 0));
        spinner.setHorizontalAlignment(JTextField.CENTER);
        add(Box.createHorizontalGlue());
        add(spinner);
        super.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (mouseListener != null)
                {
                    mouseListener.mouseClicked(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (mouseListener != null)
                {
                    mouseListener.mousePressed(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (mouseListener != null)
                {
                    mouseListener.mouseReleased(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                NumberPicker.this.setBackground(TypicalColors.LIGHT_GRAY_HIGHLIGHTED);
                if (mouseListener != null)
                {
                    mouseListener.mouseEntered(e);
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                NumberPicker.this.setBackground(TypicalColors.LIGHT_GRAY);
                if (mouseListener != null)
                {
                    mouseListener.mouseExited(e);
                }
            }
        });
    }

    @Override
    public synchronized void addMouseListener(MouseListener l)
    {
        mouseListener = l;
    }

    public int getValue()
    {
        return spinner.getValue();
    }
}
