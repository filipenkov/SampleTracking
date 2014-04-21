/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.mail;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailServersValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(MailServersValuesGenerator.class);

    public Map getValues(Map params)
    {
        Map returnValues = new HashMap();
        try
        {
            List pops = MailFactory.getServerManager().getPopMailServers();
            for (int i = 0; i < pops.size(); i++)
            {
                MailServer mailServer = (MailServer) pops.get(i);
                returnValues.put(mailServer.getId().toString(), mailServer.getName());
            }
        }
        catch (MailException e)
        {
            log.error("Could not retrieve mail servers", e);
        }
        return returnValues;
    }
}
