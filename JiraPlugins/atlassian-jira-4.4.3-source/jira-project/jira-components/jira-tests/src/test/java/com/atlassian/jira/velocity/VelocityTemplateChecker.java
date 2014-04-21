package com.atlassian.jira.velocity;

import junit.framework.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class checks a given file for local velocity macros, but it ignores files which are specified as a global
 * velocity macro template file available in all velocity macro files.
 *
 * @since v4.0
 */
public class VelocityTemplateChecker extends AbstractFileChecker
{
    private final FilenameFilter velocityTemplateFileFilter;

    public VelocityTemplateChecker()
    {
        velocityTemplateFileFilter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                final File velocityFile = new File(dir, name);
                return name.endsWith(".vm") && !isGlobalVelocityFile(velocityFile);
            }
        };
    }

    public void checkFile(final File file)
    {
        LineNumberReader reader;
        try
        {
            reader = new LineNumberReader(new FileReader(file));
            String line;
            Pattern macroPattern = Pattern.compile("#macro[\\s]*\\([\\w\\d\\s$]*\\).*");
            Pattern commentPattern = Pattern.compile("##.*");

            while ((line = reader.readLine()) != null)
            {
                Matcher macroMatcher = macroPattern.matcher(line);
                Matcher commentMatcher = commentPattern.matcher(line);

                if (macroMatcher.find() && !commentMatcher.find())
                {
                    Assert.fail("Found local velocity macro in '" + file.getAbsolutePath() + "' Macro signature '" + macroMatcher.group() + "'");
                }
            }
        }
        catch (FileNotFoundException ignore)
        {
        }
        catch (IOException ignore)
        {
        }
    }

    public void testFinished()
    {
        //does nothing
        //Will fail during execution
    }

    public FilenameFilter getFilenameFilter()
    {
        return velocityTemplateFileFilter;
    }
}
