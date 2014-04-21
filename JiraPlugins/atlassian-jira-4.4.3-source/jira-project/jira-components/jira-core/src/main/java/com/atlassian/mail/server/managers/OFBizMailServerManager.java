package com.atlassian.mail.server.managers;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.user.preferences.DefaultPreferences;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.mail.MailConstants.DEFAULT_POP_PORT;
import static com.atlassian.mail.MailConstants.DEFAULT_POP_PROTOCOL;
import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PORT;
import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PROTOCOL;

/**
 * This was taken from atlassian-mail and placed into its now rightful home of JIRA.
 *
 * @since 4.3 
 */
public class OFBizMailServerManager extends AbstractMailServerManager
{
    public MailServer getMailServer(Long id) throws MailException
    {
        try
        {
            GenericValue gv = CoreFactory.getGenericDelegator().findByPrimaryKeyCache("MailServer", UtilMisc.toMap("id", id));
            if (gv == null)
                return null;
            else
                return constructMailServer(gv);
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }

    public MailServer getMailServer(String name) throws MailException
    {
        try
        {
            GenericValue gv = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAndCache("MailServer", UtilMisc.toMap("name", name)));
            if (gv == null)
                return null;
            else
                return constructMailServer(gv);
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }

    public List getServerNames() throws MailException
    {
        try
        {
            List mailServerGVs = CoreFactory.getGenericDelegator().findAllCache("MailServer", UtilMisc.toList("id asc"));
            List mailServers = new ArrayList();
            for (int i = 0; i < mailServerGVs.size(); i++)
            {
                GenericValue emailServerGV = (GenericValue) mailServerGVs.get(i);
                mailServers.add(constructMailServer(emailServerGV));
            }
            return mailServers;
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }

    public List getSmtpMailServers() throws MailException
    {
        return getMailServersByType(SERVER_TYPES[1]);
    }

    public List getPopMailServers() throws MailException
    {
        return getMailServersByType(SERVER_TYPES[0]);
    }

    public Long create(MailServer mailServer) throws MailException
    {
        try
        {
            GenericValue storedMailServer = EntityUtils.createValue("MailServer", getMapFromColumns(mailServer));
            return storedMailServer.getLong("id");
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }

    public void update(MailServer mailServer) throws MailException
    {
        try
        {
            GenericValue storedMailServer = getMailServerGV(mailServer.getId());
            storedMailServer.setFields(getMapFromColumns(mailServer));
            storedMailServer.store();
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }

    public void delete(Long mailServerId) throws MailException
    {
        try
        {
            GenericValue storedMailServer = getMailServerGV(mailServerId);
            storedMailServer.remove();
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }


    public SMTPMailServer getDefaultSMTPMailServer()
    {
        SMTPMailServer smtps;
        try
        {
            DefaultPreferences dp = new DefaultPreferences();
            smtps = (SMTPMailServer) getMailServer(dp.getString("DefaultSmtpServer"));
            if (smtps != null)
                return smtps;
        }
        catch (Exception e)
        {
        }

        try
        {
            if (getSmtpMailServers() == null || getSmtpMailServers().size() == 0)
                return null;
            return (SMTPMailServer) getSmtpMailServers().get(0);
        }
        catch (MailException e)
        {
        }

        return null;
    }

    public PopMailServer getDefaultPopMailServer()
    {
        PopMailServer pops;
        try
        {
            DefaultPreferences dp = new DefaultPreferences();
            pops = (PopMailServer) getMailServer(dp.getString("DefaultPopServer"));
            if (pops != null)
                return pops;
        }
        catch (Exception e)
        {
        }

        try
        {
            if (getPopMailServers() == null || getPopMailServers().size() == 0)
                return null;
            return (PopMailServer) getPopMailServers().get(0);
        }
        catch (MailException e)
        {
        }

        return null;
    }

    protected List getMailServersByType(String serverType) throws MailException
    {
        List returnValue = new ArrayList();
        List mailServers = getServerNames();
        for (int i = 0; i < mailServers.size(); i++)
        {
            MailServer mailServer = (MailServer) mailServers.get(i);
            if (serverType.equals(mailServer.getType()))
                returnValue.add(mailServer);
        }
        return returnValue;
    }

    protected GenericValue getMailServerGV(Long id) throws MailException
    {
        try
        {
            return CoreFactory.getGenericDelegator().findByPrimaryKeyCache("MailServer", UtilMisc.toMap("id", id));
        }
        catch (GenericEntityException e)
        {
            throw new MailException(e);
        }
    }

    protected MailServer constructMailServer(GenericValue gv)
    {

        final String serverType = gv.getString("type");
        String port = gv.getString("smtpPort");
        // this is to fix upgrade errors when smtp port is null in the DB
        if (port == null)
        {
             port = getDefaultPort(serverType);
        }
        // this is for upgrade errors where protocol is null
        String protocol = gv.getString("protocol");
        MailProtocol mailProtocol = (protocol != null) ? MailProtocol.getMailProtocol(protocol) : getDefaultProtocol(serverType);
        // this is for upgrade errors where timeout is null
        long timeout = gv.getLong("timeout") == null ? MailConstants.DEFAULT_TIMEOUT : gv.getLong("timeout");
        {

        }
        if (SERVER_TYPES[0].equals(serverType))
        {
            return new PopMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"),
                    mailProtocol, gv.getString("servername"), port, gv.getString("username"), gv.getString("password"), timeout);
        }
        else if (SERVER_TYPES[1].equals(serverType))
        {

            boolean isTlsRequired = Boolean.valueOf(gv.getString("istlsrequired"));
            if (TextUtils.stringSet(gv.getString("servername")))
            {
                return new SMTPMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"),
                        gv.getString("from"), gv.getString("prefix"), false, mailProtocol, gv.getString("servername"),
                        port, isTlsRequired, gv.getString("username"), gv.getString("password"),timeout);
            }
            else
            {
                return new SMTPMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"),
                        gv.getString("from"), gv.getString("prefix"), true, mailProtocol, gv.getString("jndilocation"),
                        port, isTlsRequired, gv.getString("username"), gv.getString("password"));
            }
        }
        else
            return null;
    }

    private MailProtocol getDefaultProtocol(String serverType)
    {
        return SERVER_TYPES[0].equals(serverType) ? DEFAULT_POP_PROTOCOL: DEFAULT_SMTP_PROTOCOL;
    }

    private String getDefaultPort(String serverType)
    {
        return SERVER_TYPES[0].equals(serverType) ? DEFAULT_POP_PORT: DEFAULT_SMTP_PORT;

    }

    protected Map getMapFromColumns(MailServer mailServer) throws MailException
    {
        Map columns = new HashMap();
        columns.put("name", mailServer.getName());
        columns.put("description", mailServer.getDescription());
        columns.put("username", mailServer.getUsername());
        columns.put("password", mailServer.getPassword());
        columns.put("type", mailServer.getType());
        columns.put("servername", mailServer.getHostname());
        columns.put("smtpPort", mailServer.getPort());
        columns.put("protocol",mailServer.getMailProtocol().getProtocol());
        columns.put("timeout", mailServer.getTimeout());

        if (SERVER_TYPES[0].equals(mailServer.getType()))
        {
            //Do nothing different
        }
        else if (SERVER_TYPES[1].equals(mailServer.getType()))
        {
            SMTPMailServer smtp = (SMTPMailServer) mailServer;
            columns.put("from", smtp.getDefaultFrom());
            columns.put("prefix", smtp.getPrefix());
            columns.put("istlsrequired",Boolean.toString(smtp.isTlsRequired()));

            if (smtp.isSessionServer())
            {
                columns.put("jndilocation", smtp.getJndiLocation());
            }
            else
            {
                columns.put("servername", smtp.getHostname());
            }
        }
        else
        {
            throw new MailException("The Type of Mail Server is not recognised");
        }
        return columns;
    }
}
