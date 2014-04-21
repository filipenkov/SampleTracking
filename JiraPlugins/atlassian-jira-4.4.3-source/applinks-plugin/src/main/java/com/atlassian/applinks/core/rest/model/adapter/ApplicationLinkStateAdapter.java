package com.atlassian.applinks.core.rest.model.adapter;

import com.atlassian.applinks.core.rest.model.ApplicationLinkState;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @since   3.0
 */
public class ApplicationLinkStateAdapter extends XmlAdapter<String, ApplicationLinkState>
{
    @Override
    public String marshal(final ApplicationLinkState applicationStatus) throws Exception {
        return applicationStatus == null ? null : applicationStatus.name();
    }

    @Override
    public ApplicationLinkState unmarshal(final String s) throws Exception {
        return s == null ? null : ApplicationLinkState.valueOf(s);
    }
}
