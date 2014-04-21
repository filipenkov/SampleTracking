package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "applicationLinks")
public class ApplicationLinkListEntity
{
    @XmlElement(name = "applicationLinks")
    private List<ApplicationLinkEntity> applicationLinks;

    public ApplicationLinkListEntity()
    {
    }

    public ApplicationLinkListEntity(List<ApplicationLinkEntity> applicationLinks)
    {
        this.applicationLinks = applicationLinks;
    }

    public List<ApplicationLinkEntity> getApplications()
    {
        return applicationLinks;
    }
}
