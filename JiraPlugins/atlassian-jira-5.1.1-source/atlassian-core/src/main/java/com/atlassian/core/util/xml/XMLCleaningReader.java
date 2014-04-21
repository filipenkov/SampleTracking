package com.atlassian.core.util.xml;


import org.apache.log4j.Logger;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Class to read XML streams and filter out invalid UTF-8 characters
 */
public class XMLCleaningReader extends FilterReader
{
    private static final Logger log = Logger.getLogger(XMLCleaningReader.class);

    public XMLCleaningReader(Reader reader)
    {
        super(reader);
    }

    public int read(char cbuf[], int off, int len) throws IOException
    {
        final int charsRead = super.read(cbuf, off, len);

        if (charsRead > -1)
        {
            int limit = charsRead + off;
            for (int j = off; j < limit; j++)
            {
                char c = cbuf[j];
                if (c > -1 && c != 9 && c != 10 && c != 13)
                {
                    if (c < 32 || (c > 55295 && c < 57344))
                    {
                        log.warn("Replaced invalid XML character " + c + " (" + (int) c + ").");
                        cbuf[j] = '\uFFFD';
                    }
                }
            }
        }

        return charsRead;
    }

    public int read() throws IOException
    {
        final int i = super.read();
        if (i < 32 && i > -1 && i != 9 && i != 10 && i != 13)
        {
            return '\uFFFD';
        }
        return i;
    }
}
