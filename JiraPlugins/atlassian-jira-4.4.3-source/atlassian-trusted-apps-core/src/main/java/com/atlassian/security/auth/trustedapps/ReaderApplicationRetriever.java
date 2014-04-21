package com.atlassian.security.auth.trustedapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Take a Reader and produce an {@link Application}.
 */
public class ReaderApplicationRetriever implements ApplicationRetriever
{
    private final ListApplicationRetriever delegate;

    public ReaderApplicationRetriever(final Reader reader, final EncryptionProvider encryptionProvider)
    {
        Null.not("reader", reader);
        Null.not("encryptionProvider", encryptionProvider);

        delegate = new ListApplicationRetriever(encryptionProvider, extract(reader));
    }

    public Application getApplication() throws RetrievalException
    {
        return delegate.getApplication();
    }

    private List<String> extract(final Reader r)
    {
        final BufferedReader reader = new BufferedReader(r);
        final List<String> result = new ArrayList<String>();

        try
        {
            for (String str = reader.readLine(); str != null; str = reader.readLine())
            {
                final String line = str.trim();
                if (line.length() > 0)
                {
                    result.add(line);
                }
            }
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableList(result);
    }
}
