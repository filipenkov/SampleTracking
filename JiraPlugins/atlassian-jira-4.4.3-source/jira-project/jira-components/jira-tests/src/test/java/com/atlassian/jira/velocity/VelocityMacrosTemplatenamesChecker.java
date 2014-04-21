package com.atlassian.jira.velocity;

import junit.framework.Assert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ensures a macro is not defined twice (macro with the name already exists) in one of the global velocity macro files.
 *
 * @since v4.0
 */
public class VelocityMacrosTemplatenamesChecker extends AbstractFileChecker
{
    Map<String,File> macroNames = new HashMap<String,File>();

    public VelocityMacrosTemplatenamesChecker()
    {

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
                    Pattern macroNamePattern = Pattern.compile("\\([\\s]*[\\w]*");
                    final Matcher macroNameMatcher = macroNamePattern.matcher(macroMatcher.group());
                    if (macroNameMatcher.find())
                    {
                        final String macroName = macroNameMatcher.group().substring(1);
                        if (!macroNames.containsKey(macroName))
                        {
                            macroNames.put(macroName,file);
                        }
                        else
                        {
                            Assert.fail("macro name: '" + macroName+"' in '"+ file.getAbsolutePath()+"' is already defined in '"+ macroNames.get(macroName).getAbsolutePath()+"'.");
                        }
                    }
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

    public FilenameFilter getFilenameFilter()
    {
        return new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                final File velocityFile = new File(dir, name);
                return name.endsWith(".vm") && isGlobalVelocityFile(velocityFile);
            }
        };
    }

    public void testFinished()
    {
    }
}
