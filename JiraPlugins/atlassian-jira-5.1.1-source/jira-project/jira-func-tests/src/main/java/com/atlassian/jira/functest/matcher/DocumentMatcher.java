package com.atlassian.jira.functest.matcher;

import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Document;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * Template class for matchers that work on DOM documents.
 *
 * @since v5.0
 */
public abstract class DocumentMatcher extends TypeSafeMatcher<InputStream>
{
    @Override
    final public boolean matchesSafely(InputStream inputStream)
    {
        try
        {
            if (inputStream == null)
            {
                return matchesDocument(null);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse(inputStream);

            return matchesDocument(doc);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    abstract protected boolean matchesDocument(@Nullable Document document) throws Exception;
}
