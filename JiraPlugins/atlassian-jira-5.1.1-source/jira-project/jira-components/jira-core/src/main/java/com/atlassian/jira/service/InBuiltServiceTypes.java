package com.atlassian.jira.service;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.concurrent.Immutable;

/**
 * Represents the in-built service types that ship with JIRA.
 *
 * @see JiraService
 * @since v4.3
 */
public interface InBuiltServiceTypes
{
    /**
     * Gets a list of all the in-built service types.
     *
     * @return A list of all the in-built service types.
     */
    Iterable<InBuiltServiceType> all();

    /**
     * Gets a list of all the in-built services types that can be managed by an specified user.
     *
     * @param user the user in play.
     * @return A list of all the in-built services types that can be managed by an specified user.
     */
    Iterable<InBuiltServiceType> manageableBy(User user);

    /**
     * Describes an in-built service in JIRA.
     *
     * @see InBuiltServiceTypes
     */
    @Immutable
    class InBuiltServiceType
    {
        private final Class type;

        private final String i18nKey;

        InBuiltServiceType(final Class type, final String i18nKey)
        {
            this.type = type;
            this.i18nKey = i18nKey;
        }

        /**
         * Gets the {@link Class} that implements this service.
         *
         * @return the Class that implements this service.
         */
        public Class getType()
        {
            return type;
        }

        /**
         * Gets an i18nk key which describes the capabilities of this service.
         *
         * @return An i18nk key which describes the capabilities of this service.
         */
        public String getI18nKey()
        {
            return i18nKey;
        }
    }
}
