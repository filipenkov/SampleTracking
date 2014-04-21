package com.atlassian.mail;

import com.atlassian.mail.server.MailServerManager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This enum represents common mail protocols
 * It consists of the protocol, default port and the class of mail protocol
 *
 * @since v4.3
 */
public enum MailProtocol
{
    SMTP("smtp", "25", MailServerManager.SERVER_TYPES[1]),
    SECURE_SMTP("smtps", "465", MailServerManager.SERVER_TYPES[1]),
    POP("pop3", "110", MailServerManager.SERVER_TYPES[0]),
    SECURE_POP("pop3s", "995", MailServerManager.SERVER_TYPES[0]),
    IMAP("imap", "143", MailServerManager.SERVER_TYPES[0]),
    SECURE_IMAP("imaps", "993", MailServerManager.SERVER_TYPES[0]);

    private final String protocol;
    private final String defaultPort;
    private final String mailServerType;

    MailProtocol(String protocol, String defaultPort, String serverType)
    {
        this.protocol = protocol;
        this.defaultPort = defaultPort;
        this.mailServerType = serverType;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getDefaultPort()
    {
        return defaultPort;
    }

    public String getMailServerType()
    {
        return mailServerType;     
    }
    
    public static MailProtocol getMailProtocol(String protocol) {
        MailProtocol mailProtocol  = null;
        for (final  MailProtocol element: EnumSet.allOf(MailProtocol.class))
        {
            if(element.getProtocol().equals(protocol)) {
                mailProtocol = element;
                break;
            }
        }
        return mailProtocol;
    }

    public static MailProtocol[] getMailProtocolsForServerType(String serverType)
    {
        final List<MailProtocol> mailProtocols = new ArrayList<MailProtocol>();
        for (final  MailProtocol element: EnumSet.allOf(MailProtocol.class))
        {
            if(element.getMailServerType().equals(serverType)) {
                mailProtocols.add(element);
            }
        }
        return mailProtocols.toArray(new MailProtocol[mailProtocols.size()]);
    }
}
