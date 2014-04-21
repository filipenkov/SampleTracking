package com.atlassian.crowd.embedded.admin.support;

public class TroubleshootingCommand
{
    private long directoryId;
    private String edit;
    private String username;
    private String password;

    public long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(final long directoryId)
    {
        this.directoryId = directoryId;
    }

    public String getEdit()
    {
        return edit;
    }

    public boolean redirectToEdit()
    {
        return edit != null;
    }

    public void setEdit(String edit)
    {
        this.edit = edit;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }
}
