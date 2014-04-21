package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.util.dbc.Null;
import org.ofbiz.core.entity.model.ModelEntity;

import java.io.PrintWriter;

/**
 * Abstract base class that will print "<entity-engine-xml>" open and closing tags at the begining and
 * end of handling a document.
 *
 * @since v3.13
 */
public abstract class AbstractImportPartitionHandler implements ImportEntityHandler
{
    private final PrintWriter printWriter;
    private final String encoding;

    public AbstractImportPartitionHandler(final PrintWriter printWriter, final String encoding)
    {
        this.printWriter = printWriter;
        this.encoding = encoding;
    }

    public void startDocument()
    {
        printWriter.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        printWriter.println("<entity-engine-xml>");
    }

    public void endDocument()
    {
        printWriter.print("</entity-engine-xml>");
    }

    public void assertModelEntityForName(final ModelEntity modelEntity, final String expectedName)
    {
        Null.not("modelEntity", modelEntity);
        Null.not("expectedName", expectedName);
        if (!expectedName.equals(modelEntity.getEntityName()))
        {
            throw new IllegalArgumentException("This handler must only be created with a " + expectedName + " model entity");
        }
    }

    public String getEncoding()
    {
        return encoding;
    }
}
