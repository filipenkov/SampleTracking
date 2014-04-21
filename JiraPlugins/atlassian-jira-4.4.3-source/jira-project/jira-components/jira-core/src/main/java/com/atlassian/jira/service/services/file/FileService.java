/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.service.services.file;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.PathUtils;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileService extends AbstractMessageHandlingService
{
    private static final Logger log = Logger.getLogger(FileService.class);
    private static final String KEY_DIRECTORY = "directory";
    private File directory = null;

    public static final String MAIL_DIR = PathUtils.joinPaths(JiraHome.IMPORT, "mail");
    public static final String KEY_SUBDIRECTORY = "subdirectory";

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);

        String dir = "";
        if (hasProperty(KEY_DIRECTORY))
        {
            dir = getProperty(KEY_DIRECTORY);
        }
        if (StringUtils.isNotBlank(dir))
        {
            directory = new File(dir);
        }
        else
        {
            if (hasProperty(KEY_SUBDIRECTORY) && StringUtils.isNotBlank(getProperty(KEY_SUBDIRECTORY)))
            {
                try
                {
                    directory =  new File(getJiraHome().getHome(), PathUtils.joinPaths(FileService.MAIL_DIR, getProperty(KEY_SUBDIRECTORY))).getCanonicalFile();
                }
                catch (IOException e)
                {
                    throw new ObjectConfigurationException(e);
                }
            }
            else
            {
                directory = new File(getJiraHome().getHome(), MAIL_DIR);
            }
        }

        if (!directory.isDirectory())
        {
            log.warn("Directory: " + dir + " setup for FileService is not a directory.");
        }
        else if (!directory.canRead())
        {
            log.warn("Directory: " + dir + " setup for FileService does not allow read.");
        }
        else if (!directory.canWrite())
        {
            log.warn("Directory: " + dir + " setup for FileService does not allow write.");
        }
    }

    public void run()
    {
        if (directory == null)
        {
            log.warn("Directory is not set for FileService");
            return;
        }
        if (!directory.exists())
        {
            log.warn("Directory " + directory.getPath() + " does not exist");
            return;
        }

        log.debug("Getting files in directory: " + directory);

        File[] files = directory.listFiles();

        if (files == null)
        {
            log.warn("List of files retrieved from " + directory + " was null?");
        }
        else
        {
            for (File file : files)
            {
                // it needs to be a file, and we need to be able to read it and then delete it
                if (file.isFile() && file.canRead() && file.canWrite())
                {
                    log.debug("Trying to parse file: " + file.getAbsolutePath());
                    FileInputStream fis = null;
                    try
                    {
                        fis = new FileInputStream(file);
                        final Message message = new MimeMessage(null, fis);
                        final boolean delete = getHandler().handleMessage(message);
                        fis.close();
                        fis = null;

                        if (delete)
                        {
                            log.info("Deleting file: " + file.getAbsolutePath());
                            if (!file.delete())
                            {
                                log.warn("Unable to delete file '" + file + "'.");
                            }
                            else
                            {
                                log.info("Deleted file: " + file.getAbsolutePath());
                            }
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        // this shouldn't happen
                        log.error("File not found when it should be, are two FileServices running?", e);
                    }
                    catch (MessagingException e)
                    {
                        log.error("Messaging exception: " + e, e);
                    }
                    catch (Throwable t)
                    {
                        log.error("Throwable: " + t, t);
                    }
                    finally
                    {
                        if (fis != null)
                        {
                            try
                            {
                                fis.close();
                            }
                            catch (IOException ignored)
                            {
                            }
                        }
                    }
                }
            }
        }
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("FILESERVICE", "services/com/atlassian/jira/service/services/file/fileservice.xml", null);
    }

    JiraHome getJiraHome()
    {
        return ComponentManager.getComponentInstanceOfType(JiraHome.class);
    }
}
