package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Map;

/**
 * Abstract class for {@link com.atlassian.jira.user.profile.UserProfileFragment} that helps writing fragments that are
 * velocity rendered. It simply renders the template based of the fragment id and the implementing class can also
 * overide {@link #createVelocityParams(com.opensymphony.user.User, com.opensymphony.user.User)}
 *
 * @since v4.1
 */
public abstract class AbstractUserProfileFragment implements UserProfileFragment
{
    private static final Logger log = Logger.getLogger(AbstractUserProfileFragment.class);

    protected final ApplicationProperties applicationProperties;
    protected final JiraAuthenticationContext jiraAuthenticationContext;
    private final VelocityManager velocityManager;

    public AbstractUserProfileFragment(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext,
            VelocityManager velocityManager)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.velocityManager = velocityManager;
    }

    public final boolean showFragment(final com.opensymphony.user.User profileUser, final com.opensymphony.user.User currentUser)
    {
        return showFragment((User) profileUser, (User) currentUser);
    }

    /**
     * Whether or not we display this fragment. By default we do.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return true if we should disply this fragment, otherwise false
     */
    public boolean showFragment(User profileUser, User currentUser)
    {
        return true;
    }

    public final String getFragmentHtml(final com.opensymphony.user.User profileUser, final com.opensymphony.user.User currentUser)
    {
        return getFragmentHtml((User) profileUser, (User) currentUser);
    }

    /**
     * Creates the HTML for this fragment.
     * <p/>
     * This implementation renders the template based off of the frgmant id - {@link #getId()}
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return The HTML of this fragment
     */
    public String getFragmentHtml(User profileUser, User currentUser)
    {
        final String template = getId() + ".vm";
        try
        {
            final Map<String, Object> velocityParams = createVelocityParams(profileUser, currentUser);
            final String encoding = applicationProperties.getEncoding();
            return velocityManager.getEncodedBody("templates/plugins/userprofile/", template, encoding, velocityParams);
        }
        catch (VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + "templates/plugins/userprofile/" + template + "'.", e);
            return "";
        }
    }

    /**
     * Creates the parameters passed to the velocity template.
     * <p/>
     * By default this contains "fragId", "profileUser", "currentUser"
     * <p/>
     * Implmentors of this abstract class can override this method to provide their own params.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return a map of the params passed to the velocity template.
     * @deprecated Use {@link #createVelocityParams(User, User)}. Since v4.3
     */
    protected final Map<String, Object> createVelocityParams(com.opensymphony.user.User profileUser, com.opensymphony.user.User currentUser)
    {
        return createVelocityParams((User) profileUser, (User) currentUser);
    }

    /**
     * Creates the parameters passed to the velocity template.
     * <p/>
     * By default this contains "fragId", "profileUser", "currentUser"
     * <p/>
     * Implmentors of this abstract class can override this method to provide their own params.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return a map of the params passed to the velocity template.
     *  @since v4.3
     */
    protected Map<String, Object> createVelocityParams(User profileUser, User currentUser)
    {
        final Map<String, Object> velocityParams = JiraVelocityUtils.getDefaultVelocityParams(jiraAuthenticationContext);
        velocityParams.put("fragid", getId());
        velocityParams.put("profileUser", profileUser);
        velocityParams.put("currentUser", currentUser);
        velocityParams.put("i18n", jiraAuthenticationContext.getI18nHelper());
        return velocityParams;
    }

}
