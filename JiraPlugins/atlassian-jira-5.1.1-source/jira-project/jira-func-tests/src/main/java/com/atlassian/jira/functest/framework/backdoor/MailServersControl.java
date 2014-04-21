package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate Mail Servers.
 *
 * See MailServersBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class MailServersControl extends BackdoorControl<MailServersControl>
{
    public static final String DEFAULT_FROM_ADDRESS = "jiratest@atlassian.com";
    public static final String DEFAULT_SUBJECT_PREFIX = "[JIRATEST]";

    public MailServersControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Adds an SMTP server with default parameters and a specified port.
     *
     * Deletes any existing SMTP servers first.
     *
     * @param port The SMTP port of your mail server.
     */
    public void addSmtpServer(int port)
    {
        addSmtpServer(DEFAULT_FROM_ADDRESS, DEFAULT_SUBJECT_PREFIX, port);
    }

    /**
     * Adds an SMTP server with the specified parameters.
     *
     * Deletes any existing SMTP servers first.
     *
     * @param from The default address this server will use to send emails from.
     * @param prefix The prefix for all outgoing email subjects.
     * @param port The SMTP port of your mail server.
     */
    public void addSmtpServer(String from, String prefix, int port)
    {
        addSmtpServer("Local Test Server", "", from, prefix, "localhost", port);
    }

    /**
     * Adds an SMTP server with the specified parameters.
     *
     * Deletes any existing SMTP servers first.
     *
     * @param name The name of this server within JIRA.
     * @param description The description for this server.
     * @param from The default address this server will use to send emails from.
     * @param prefix The prefix for all outgoing email subjects.
     * @param serverName The SMTP host name of your mail server.
     * @param port The SMTP port of your mail server.
     */
    public void addSmtpServer(String name, String description, String from, String prefix, String serverName,
            int port)
    {
        MailServersBean bean = new MailServersBean();
        bean.name = name;
        bean.description = description;
        bean.from = from;
        bean.prefix = prefix;
        bean.serverName = serverName;
        bean.port = String.valueOf(port);
        post(createResource().path("mailServers/smtp"), bean, String.class);
    }

    /**
     * Adds a POP server with the specified parameters.
     *
     * @param name The name of this server within JIRA.
     * @param port The POP port of your mail server.
     */
    public void addPopServer(String name, int port)
    {
        addPopServer(name, "", "pop3", "localhost", port, "username", "password");
    }
    /**
     * Adds an IMAP server with the specified parameters.
     *
     * @param name The name of this server within JIRA.
     * @param port The IMAP port of your mail server.
     */
    public void addImapServer(String name, int port)
    {
        addPopServer(name, "", "imap", "localhost", port, "username", "password");
    }

    /**
     * Adds a POP/IMAP server with the specified parameters.
     *
     * @param name The name of this server within JIRA.
     * @param description The description for this server.
     * @param protocol The server protocol (one of pop3, pop3s, imap, imaps).
     * @param serverName The POP/IMAP host name of your mail server.
     * @param port The POP/IMAP port of your mail server.
     * @param username Username.
     * @param password Password.
     */
    public void addPopServer(String name, String description, String protocol, String serverName,
            int port, String username, String password)
    {
        MailServersBean bean = new MailServersBean();
        bean.name = name;
        bean.description = description;
        bean.protocol = protocol;
        bean.serverName = serverName;
        bean.port = String.valueOf(port);
        bean.username = username;
        bean.password = password;
        post(createResource().path("mailServers/pop"), bean, String.class);
    }

    static class MailServersBean
    {
        public String name;
        public String description;
        public String protocol;
        public String serverName;
        public String port;
        public String username;
        public String password;
        public String from;
        public String prefix;
    }

}
