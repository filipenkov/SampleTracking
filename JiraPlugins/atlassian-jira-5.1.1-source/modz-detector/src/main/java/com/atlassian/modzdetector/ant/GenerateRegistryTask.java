package com.atlassian.modzdetector.ant;

import com.atlassian.modzdetector.HashRegistry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant task for the creation of the registry.
 */
public class GenerateRegistryTask extends Task
{

    private static final Logger log = LoggerFactory.getLogger(GenerateRegistryTask.class);

    private List files = new ArrayList();
    private List classes = new ArrayList();

    private String name;

    public void addFilesystem(FileSet f)
    {
        if (f != null)
        {
            files.add(f);
        }
    }

    public void addClasspath(FileSet f)
    {
        if (f != null)
        {
            classes.add(f);
        }
    }

    // "name" is an attribute, not a nested element, on the task element
    public void setName(String fileName)
    {
        name = fileName;
    }

    public void execute() throws BuildException
    {
        try
        {
            HashRegistry hr;
            if (name != null && name.length() > 0)
            {
                hr = new HashRegistry(name);
            }
            else
            {
                hr = new HashRegistry();
            }
            hr.setClasspathMode();
            register(hr, classes);
            hr.setFilesystemMode();
            register(hr, files);
            hr.store();
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }

    private void register(HashRegistry hr, List<FileSet> filesets)
    {
        for (FileSet set : filesets)
        {
            DirectoryScanner ds = set.getDirectoryScanner(getProject());
            final String[] fileNames = ds.getIncludedFiles();
            for (String filename : fileNames)
            {
                log.debug("registering " + filename);
                try
                {
                    String filenameFixed = filename.replace('\\', '/');
                    hr.register(filenameFixed, new FileInputStream(new File(set.getDir(getProject()), filename)));
                }
                catch (FileNotFoundException e)
                {
                    log.error("cannot find file " + filename);
                }
            }
        }
    }
}
