package com.atlassian.spring.container;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;

public class SpringTestContainerContext extends SpringContainerContext
{
    public static final String[] DEFAULT_CONFIG_FILES = {};

    public String[] userConfigFiles;

    public SpringTestContainerContext() throws BeansException, IOException
    {
        configure(getDefaultConfigFiles());
    }

    /**
     * Override this method to use different files.
     */
    protected String[] getDefaultConfigFiles()
    {
        return DEFAULT_CONFIG_FILES;
    }

    public SpringTestContainerContext(ApplicationContext context)
    {
        setApplicationContext(context);
    }

    public ApplicationContext getApplicationContext()
    {
        return super.getApplicationContext();
    }

    public void refresh()
    {
        try
        {
            if(userConfigFiles!= null) {
                configure(userConfigFiles);
            } else {
                configure(getDefaultConfigFiles());
            }
            contextReloaded();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void configure(String[] files) throws BeansException, IOException
    {
        userConfigFiles = files;
        setApplicationContext(new FileSystemXmlApplicationContext(files));
    }

}
