package com.atlassian.jira.velocity;

import com.atlassian.jira.FileChecker;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class checks a given file for local velocity macros, but it ignores files which are specified as a global
 * velocity macro template file available in all velocity macro files. Local velocity macros open us up to a concurrency
 * bug in velocity.
 *
 * @since v4.0
 */
class VelocityTemplateChecker implements FileChecker
{

    static final Pattern MACRO_PATTERN = Pattern.compile("#macro\\s*\\([\\w\\d\\s]*\\)\\s*(?!##LOCAL_MACRO_EXCEPTION.*)$");
    static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*##.*");

    public List<String> checkFile(final File file)
    {
        List<String> fails = new ArrayList<String>();
        LineNumberReader reader = null;
        try
        {
            reader = new LineNumberReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null)
            {
                String fail = checkLine(file, line);
                if (fail != null)
                {
                    fails.add(fail);
                }
            }
        }
        catch (IOException ignore)
        {
            fails.add("ioexception for file '" + file.getPath() + "' : " + ignore.getMessage());
        }
        finally
        {
            if (reader != null)
            {
                IOUtils.closeQuietly(reader);
            }
        }
        return fails;
    }

    private String checkLine(File file, String line)
    {
        Matcher macroMatcher = MACRO_PATTERN.matcher(line);
        Matcher commentMatcher = COMMENT_PATTERN.matcher(line);

        if (macroMatcher.matches() && !commentMatcher.matches())
        {
            return "Found local velocity macro in '" + file.getAbsolutePath() + "' Macro signature '" + macroMatcher.group() + "'";
        }
        else
        {
            return null;
        }
    }

    private boolean excluded(File file)
    {
        return false;
    }

    public FilenameFilter getFilenameFilter()
    {
        return NonGlobalVelocityTemplateFilter.INSTANCE;
    }
}
