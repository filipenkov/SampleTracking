package com.atlassian.mail;

import com.atlassian.mail.config.ConfigLoader;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import javax.mail.Multipart;

public class MailFactory
{
    public static final String MAIL_DISABLED_KEY = "atlassian.mail.senddisabled";

    private static MailServerManager serverManager;

    public static void refresh()
    {
        serverManager = null;
    }

    public static MailServerManager getServerManager()
    {
        if (serverManager == null)
        {
            synchronized (MailFactory.class)
            {
                if (serverManager == null)
                {
                    serverManager = ConfigLoader.getServerManager();
                }
            }
        }

        return serverManager;
    }

    public static void setServerManager(MailServerManager serverManager)
    {
        MailFactory.serverManager = serverManager;
    }

    public static boolean isSendingDisabled()
    {
        return Boolean.getBoolean(MAIL_DISABLED_KEY);
    }
}