package com.atlassian.jira.configurator.gui;

import javax.swing.*;
import java.awt.*;

public class LabelledComponent extends JPanel
{
    final JLabel label;

    public LabelledComponent(final String labelText, final JComponent component)
    {
        setLayout(new BorderLayout(4, 0));
        label = new JLabel(labelText);
        add(label, BorderLayout.WEST);
        add(component, BorderLayout.CENTER);        
    }

    public int getPreferredLabelWidth()
    {
        return label.getPreferredSize().width;
    }

    public void setLabelWidth(final int width)
    {
        label.setPreferredSize(new Dimension(width, label.getPreferredSize().height));
    }
}
