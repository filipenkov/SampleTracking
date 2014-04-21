package com.atlassian.jira.velocity;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.FileChecker;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Provides a utility method to retrieve the List of global velocity macro files as defined in the velocity.properties file.
 *
 * @since v4.0
 */
public abstract class AbstractFileChecker implements FileChecker
{
    private List<File> velocityFiles;

    protected List<File> getGlobalVelocityFiles()
    {
        if (velocityFiles == null)
        {
            velocityFiles = new ArrayList<File>();
            Properties props = new Properties();
            try
            {
                props.load(ClassLoaderUtils.getResourceAsStream("velocity.properties", getClass()));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to find velocity.properties file.", e);
            }

            final Object globalMacros = props.get(RuntimeConstants.VM_LIBRARY);

            StringTokenizer tokenizer = new StringTokenizer((String) globalMacros, ",");

            while (tokenizer.hasMoreElements())
            {
                final String velocityFile = tokenizer.nextToken().trim();
                final File velocityTemplateFile = new File(this.getClass().getResource("/" + velocityFile).getFile());
                assert (velocityTemplateFile.exists() == true);
                velocityFiles.add(velocityTemplateFile);
            }
        }
        return velocityFiles;
    }

    protected boolean isGlobalVelocityFile(File velocityFile)
    {
        return getGlobalVelocityFiles().contains(velocityFile);
    }
}
