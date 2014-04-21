package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

@XmlRootElement(name = "error")
public class ErrorListEntity
{
    @XmlAttribute
    private int status;

    @XmlElement(name = "message")
    private List<String> errors;

    @XmlElement(name = "fields")
    private List<String> fields;

    public ErrorListEntity()
    {
    }

    public ErrorListEntity(int status, String... errors)
    {
        this(status, Arrays.asList(errors));
    }

    public ErrorListEntity(int status, List<String> errors, List<String> fields)
    {
        this(status, errors);
        this.fields = fields;
    }

    public ErrorListEntity(int status, List<String> errors)
    {
        this.status = status;
        this.errors = errors;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public List<String> getFields()
    {
        return fields;
    }
}
