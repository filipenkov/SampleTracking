package com.atlassian.applinks.core.rest.model.adapter;

import com.atlassian.applinks.core.rest.permission.PermissionCode;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PermissionCodeAdapter extends XmlAdapter<String, PermissionCode>
{
    @Override
    public PermissionCode unmarshal(final String v) throws Exception
    {
        return PermissionCode.valueOf(v);
    }

    @Override
    public String marshal(final PermissionCode v) throws Exception
    {
        return v.name();
    }
}
