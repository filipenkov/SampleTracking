package com.atlassian.crowd.embedded.admin.rest.entities;

import com.atlassian.crowd.embedded.api.Directory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of {@link DirectoryEntity directories}.
 */
@XmlRootElement(name="directories")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectoryList
{
    @XmlElement(name="directory")
    private List<DirectoryEntity> directories = new ArrayList<DirectoryEntity>();

    public List<DirectoryEntity> getDirectories()
    {
        return directories;
    }

    public void setDirectories(List<DirectoryEntity> directories)
    {
        this.directories = directories;
    }
}
