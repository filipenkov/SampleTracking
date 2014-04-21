package com.atlassian.applinks.core.rest.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.net.URI;

/**
 * Adaptor that does not permit {@code null} values.
 */
public class RequiredURIAdapter extends XmlAdapter<String, URI>
{
    @Override
    public URI unmarshal(final String v) throws Exception
    {
        return new URI(v);
    }

    @Override
    public String marshal(final URI v) throws Exception
    {
        return v.toString();
    }
}
