package com.atlassian.jira.welcome.access;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Predicates.and;

/**
 * <p>Represents the level of access {@link com.atlassian.crowd.embedded.api.User Users} have to the &quot;Welcome Screen&quot; feature in JIRA.</p>
 *
 * <p>Access to the feature is determined by checking the set of all available access constraints.</p>
 *
 * @see com.atlassian.jira.welcome.access.WelcomeScreenAccess.Constraint
 * @since 1.1
 */
public class WelcomeScreenAccess
{
    private static final Logger log = LoggerFactory.getLogger(WelcomeScreenAccess.class);    
    private final Set<Constraint> accessConstraints;

    public WelcomeScreenAccess(final Set<Constraint> accessConstraints)
    {
        this.accessConstraints = accessConstraints;
    }

    /**
     * Whether the specified user has been granted access to the &quot;Welcome Screen&quot; feature.
     *
     * @param user The user to check access for.
     * @return {@code true} if the specified user has been granted access to the &quot;Welcome Screen&quot; feature;
     * otherwise, {@code false}.
     */
    public boolean isGrantedTo(final @Nullable User user)
    {
        log.debug("Checking {} access constraints for user '{}'", accessConstraints.size(), (null == user) ? "nobody" : user.getName());
        return and(accessConstraints).apply(user);
    }

    /**
     * <p>An internal SPI implemented by extensions to the JIRA Welcome Screen that allows them to constraint access
     * to the Welcome Screen feature based on the logged in user.</p>
     *
     * <p>Plugins that implement this SPI should declare their implementations as public components in their
     * {@code atlassian-plugin.xml} manifest.</p>
     *
     * <p/>
     * For example:
     * <pre>
     * {@code
     * <atlassian-plugin key="com.example.jira-welcome-plugin-ext"
     *                   name="Example JIRA Welcome Screen Plugin Extension"
     *                   pluginsVersion="2">
     *    <!-- ... -->
     *    <component key="exampleAccessConstraint"
     *               class="com.example.jira.welcome.ext.ExampleAccessConstraint"
     *               interface="com.atlassian.jira.welcome.access.WelcomeScreenAccess$Constraint"
     *               public="true"/>
     * </atlassian-plugin>
     * }
     * </pre>
     * <p/>
     *
     * <p>This SPI should be considered unstable, and is for internal Atlassian use until further notice.</p>
     *
     * @since 1.1
     */
    public static interface Constraint extends Predicate<User>
    {
        /**
         * <p>Returns whether this constraint has been met for a given user. When this method returns {@code false},
         * no further constraints will be checked, and access to the JIRA Welcome Screen Plugin will be immediately denied
         * to the user.</p>
         *
         * <p>When this method returns {@code true}, further constraints will be checked. Constraints must be
         * commutative, as there is no guarantee made about the order in which constraints will be checked.</p>
         *
         * <p>If a constraint implementation does not have sufficient information to fully determine whether to allow
         * access or not, it should return {@code true}.</p>
         *
         * @param user the user attempting to access the JIRA Welcome Screen feature; Anonymous users are
         * represented by a {@code null} reference.
         *
         * @return {@code true} if this constraint has been met for the provided user; {@code false} otherwise.
         */
        @Override
        boolean apply(@Nullable User user);
    }
}
