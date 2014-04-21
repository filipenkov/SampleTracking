package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.jelly.JellyTagException;

import java.util.Iterator;

public class JellyUtils
{
    public static void processErrorCollection(ErrorCollection errorCollection) throws JellyTagException
    {
        if (errorCollection != null && errorCollection.hasAnyErrors())
        {
            StringBuffer errors = new StringBuffer("The following problems were found:\n");

            for (Iterator iterator = errorCollection.getErrorMessages().iterator(); iterator.hasNext();)
            {
                errors.append(iterator.next()).append('\n');
            }

            for (Iterator iterator = errorCollection.getErrors().keySet().iterator(); iterator.hasNext();)
            {
                Object key = iterator.next();
                errors.append(key).append(':').append(' ').append(errorCollection.getErrors().get(key));
            }

            throw new JellyTagException(errors.toString());
        }
    }
}
