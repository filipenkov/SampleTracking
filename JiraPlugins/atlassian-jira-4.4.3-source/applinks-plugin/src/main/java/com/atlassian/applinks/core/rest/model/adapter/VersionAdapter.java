package com.atlassian.applinks.core.rest.model.adapter;

import org.osgi.framework.Version;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class VersionAdapter extends XmlAdapter<String, Version>
{
    @Override
    public Version unmarshal(final String v) throws Exception
    {
        return new Version(v);
    }

    @Override
    public String marshal(final Version v) throws Exception
    {
        return v.toString();
    }
}
