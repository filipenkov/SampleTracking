package com.atlassian.jira.plugin.webfragment;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.util.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A factory that produces a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} objects.
 *
 * @since v4.0
 */
@PublicSpi
public interface SimpleLinkFactory
{
    /**
     * Initialization method for the factory.  Used to retrieve information off the module descriptor
     *
     * @param descriptor The descriptor responsible for defining this factory.
     */
    void init(SimpleLinkFactoryModuleDescriptor descriptor);

    /**
     * Generates a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} objects.
     *
     * @param user The user this list is being generated for.
     * @return a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} objects tailored for the user.
     */
    @NotNull
    List<SimpleLink> getLinks(User user, Map<String, Object> params);
}
