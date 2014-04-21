package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement (name = "responseInfo")
public class ResponseInfoEntity
{
    @XmlElement (name = "warning")
    private String warning;

    public ResponseInfoEntity(){}

    public ResponseInfoEntity(final String warning)
    {
        this.warning = warning;
    }

    public String getWarning()
    {
        return warning;
    }
}
