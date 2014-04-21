package com.atlassian.crowd.embedded.admin.list;

/**
 * The first stage in adding a directory - choosing which type it will be. This controls the subsequent form display.
 */
public final class NewDirectoryCommand
{
    private NewDirectoryType newDirectoryType;

    public NewDirectoryType getNewDirectoryType()
    {
        return newDirectoryType;
    }

    public void setNewDirectoryType(NewDirectoryType newDirectoryType)
    {
        this.newDirectoryType = newDirectoryType;
    }
}
