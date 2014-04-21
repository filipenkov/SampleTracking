package com.atlassian.applinks.core.rest.model.adapter;

import com.atlassian.applinks.api.ApplicationId;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ApplicationIdAdapter extends XmlAdapter<String, ApplicationId>
{
    @Override
    public ApplicationId unmarshal(final String v) throws Exception
    {
        return new ApplicationId(v);
    }

    @Override
    public String marshal(final ApplicationId v) throws Exception
    {
        return v.toString();
    }
}
