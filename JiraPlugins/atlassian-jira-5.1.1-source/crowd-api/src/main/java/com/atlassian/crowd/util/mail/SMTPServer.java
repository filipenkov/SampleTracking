package com.atlassian.crowd.util.mail;

import javax.mail.internet.InternetAddress;

/**
 * POJO holding SMTP server config.
 */
public class SMTPServer
{
    private String host;
    private String username;
    private String password;
    private InternetAddress from;
    private String prefix;
    private String jndiLocation;
    private int port;
    private boolean jndiMailActive;
    private boolean useSSL;
    
    public static final int DEFAULT_MAIL_PORT = 25;

    public SMTPServer(String jndiLocation, InternetAddress from, String prefix)
    {
        this.jndiLocation = jndiLocation;
        this.from = from;
        this.jndiMailActive = true;
        this.prefix = prefix;
    }

    public SMTPServer(int port, String prefix, InternetAddress from, String password, String username, String host, boolean useSSL)
    {
        this.jndiMailActive = false;
        this.port = port;
        this.prefix = prefix;
        this.from = from;
        this.password = password;
        this.username = username;
        this.host = host;
        this.useSSL = useSSL;
    }

    public SMTPServer()
    {
    }

    public String getHost()
    {
        return host;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public boolean getUseSSL()
    {
        return useSSL;
    }

    public InternetAddress getFrom()
    {
        return from;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public int getPort()
    {
        return port;
    }

    public String getJndiLocation()
    {
        return jndiLocation;
    }

    public boolean isJndiMailActive()
    {
        return jndiMailActive;
    }
}
