package com.atlassian.applinks.core.rest.model.adapter;

import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * Adapter that does not permit {@code null} values and ensures that URI does not end with slashes.
 * 
 * @since 3.2
 */
public class RequiredBaseURIAdapter extends XmlAdapter<String, URI>
{
    @Override
    public URI unmarshal(final String v) throws Exception
    {
        return new URI(StringUtils.stripEnd(v, "/"));
    }
    
    @Override
    public String marshal(final URI v) throws Exception
    {
        return v.toString();
    }
}
