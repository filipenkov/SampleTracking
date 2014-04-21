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

    /**
     * An identifier of the type of warning being returned, such as the i18n code.
     */
    @XmlElement (name = "code")
    private String code;

    public ResponseInfoEntity(){}

    public ResponseInfoEntity(final String code, final String warning)
    {
        this.warning = warning;
        this.code = code;
    }

    public String getWarning()
    {
        return warning;
    }

    public String getCode()
    {
        return code;
    }

}
