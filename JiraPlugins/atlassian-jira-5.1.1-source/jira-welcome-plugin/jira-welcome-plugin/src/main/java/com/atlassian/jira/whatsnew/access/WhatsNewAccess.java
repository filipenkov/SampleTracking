package com.atlassian.jira.whatsnew.access;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Predicates.and;

/**
 * <p>Represents the level of access {@link User Users} have to the &quot;What's New&quot; Feature in JIRA.</p>
 *
 * <p>Access to the feature is determined by checking the set of all available access constraints.</p>
 *
 * @see WhatsNewAccess.Constraint
 * @since 1.1
 */
public class WhatsNewAccess
{
    private final Set<Constraint> accessConstraints;

    public WhatsNewAccess(final Set<Constraint> accessConstraints)
    {
        this.accessConstraints = accessConstraints;
    }

    /**
     * Whether the specified user has been granted access to the &quot;What's New&quot; Feature.
     *
     * @param user The user to check access for.
     * @return {@code true} if the specified user has been granted access to the &quot;What's New&quot; Feature;
     * otherwise, {@code false}.
     */
    public boolean isGrantedTo(final @Nullable User user)
    {
        return and(accessConstraints).apply(user);
    }

    /**
     * <p>An internal SPI implemented by extensions to the JIRA What's New Plugin that allows them to constraint access
     * to the What's New Feature based on the logged in user.</p>
     *
     * <p>Plugins that implement this SPI should declare their implementations as public components in their
     * {@code atlassian-plugin.xml} manifest.</p>
     *
     * <p/>
     * For example:
     * <pre>
     * {@code
     * <atlassian-plugin key="com.example.jira-whats-new-plugin-ext"
     *                   name="Example JIRA What's New Plugin Extension"
     *                   pluginsVersion="2">
     *    <!-- ... -->
     *    <component key="exampleAccessConstraint"
     *               class="com.example.jira.whatsnew.ext.ExampleAccessConstraint"
     *               interface="com.atlassian.jira.whatsnew.access.WhatsNewAccess$Constraint"
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
         * no further constraints will be checked, and access to the JIRA What's New Plugin will be immediately denied
         * to the user.</p>
         *
         * <p>When this method returns {@code true}, further constraints will be checked. Constraints must be
         * commutative, as there is no guarantee made about the order in which constraints will be checked.</p>
         *
         * <p>If a constraint implementation does not have sufficient information to fully determine whether to allow
         * access or not, it should return {@code true}.</p>
         *
         * @param user the user attempting to access the JIRA What's New Feature; Anonymous users are
         * represented by a {@code null} reference.
         *
         * @return {@code true} if this constraint has been met for the provided user; {@code false} otherwise.
         */
        @Override
        boolean apply(@Nullable User user);
    }
}
