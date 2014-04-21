package com.atlassian.jira.dev.backdoor;

/**
 * Bean used to hold POST requests for the {@link MailServersBackdoor}.
 *
 * @since v5.0
 */
public class MailServersBean
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
