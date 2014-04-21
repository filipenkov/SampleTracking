package com.atlassian.gadgets.view;

import java.io.IOException;
import java.io.Writer;

/**
 * A {@code ViewComponent} represents a view of an entity in the gadgets system.  It provides a displayable
 * id and title that can be used during rendering of the component, was well as a means to write the rendered
 * component to a {@link Writer}. 
 */
public interface ViewComponent
{
    /**
     * Renders the component represented by this view and writes it to the {@code Writer}.
     * 
     * @param writer where the rendered component is written
     * @throws IOException thrown if there is a problem writing the rendered component to the {@code Writer}
     */
    void writeTo(Writer writer) throws IOException;
}
