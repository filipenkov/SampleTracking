package com.atlassian.crowd.model;

import com.atlassian.crowd.embedded.api.Directory;

public abstract class InternalDirectoryEntity extends InternalEntity implements DirectoryEntity
{
    protected Directory directory;

    protected InternalDirectoryEntity()
    {

    }

    protected InternalDirectoryEntity(final InternalEntityTemplate template, final Directory directory)
    {
        super(template);
        this.directory = directory;
    }

    public long getDirectoryId()
    {
        return getDirectory() != null ? getDirectory().getId() : -1L;
    }

    public Directory getDirectory()
    {
        return directory;
    }

    private void setDirectory(final Directory directory)
    {
        this.directory = directory;
    }
}
