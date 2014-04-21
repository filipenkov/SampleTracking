package com.atlassian.applinks.core.rest.model.adapter;

import com.atlassian.applinks.spi.application.TypeId;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TypeIdAdapter extends XmlAdapter<String, TypeId>
{
    @Override
    public TypeId unmarshal(final String v) throws Exception
    {
        return new TypeId(v);
    }

    @Override
    public String marshal(final TypeId v) throws Exception
    {
        return v.get();
    }
}
