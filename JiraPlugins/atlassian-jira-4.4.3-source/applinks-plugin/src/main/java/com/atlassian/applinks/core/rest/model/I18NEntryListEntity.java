package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement(name = "i18n")
public class I18NEntryListEntity
{
    Map<String, String> entries;

    public I18NEntryListEntity(Map<String, String> entries)
    {
        this.entries = entries;
    }

    public I18NEntryListEntity()
    {
    }
}
