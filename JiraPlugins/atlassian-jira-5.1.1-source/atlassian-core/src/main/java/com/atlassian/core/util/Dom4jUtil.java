/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.core.util;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;

public class Dom4jUtil
{
    //~ Methods --------------------------------------------------------------------------------------------------------

    public static void saveDocumentTo(org.dom4j.Document doc, String folder, String fileName)
        throws IOException
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        File file = new File(folder, fileName);

        if (!file.exists())
        {
            file.createNewFile();
        }

        OutputStreamWriter writeOut = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        XMLWriter writer = new XMLWriter(writeOut, format);
        writer.write(doc);
        writer.close();
    }
}
