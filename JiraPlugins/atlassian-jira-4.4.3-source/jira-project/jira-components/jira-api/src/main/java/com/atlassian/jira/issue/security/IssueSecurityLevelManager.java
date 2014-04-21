package com.atlassian.jira.issue.security;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * TODO: convert to use {@link com.atlassian.jira.issue.security.IssueSecurityLevel}'s
 */
public interface IssueSecurityLevelManager
{
    /**
     * Returns the list of Security Levels for the given Issue Security Level Scheme.
     *
     * @param schemeId ID of the Issue Security Level Scheme.
     * @return the list of Security Levels for the given Issue Security Level Scheme.
     */
    List<GenericValue> getSchemeIssueSecurityLevels(Long schemeId);

    boolean schemeIssueSecurityExists(Long id);

    String getIssueSecurityName(Long id);

    String getIssueSecurityDescription(Long id);

    GenericValue getIssueSecurity(Long id);

    /**
     * Get the different levels of security that can be set for this issue
     *
     * @param entity This is the issue or the project that the security is being checked for
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     */
    List<GenericValue> getUsersSecurityLevels(GenericValue entity, User user) throws GenericEntityException;

    /**
     * Get the different levels of security that can be set for this issue
     *
     * @param entity This is the issue or the project that the security is being checked for
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     *
     * @deprecated Please use {@link #getUsersSecurityLevels(org.ofbiz.core.entity.GenericValue, com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    List<GenericValue> getUsersSecurityLevels(GenericValue entity, com.opensymphony.user.User user) throws GenericEntityException;

    /**
     * Get the different levels of security that the user can see across all projects.
     *
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     */
    Collection<GenericValue> getAllUsersSecurityLevels(User user) throws GenericEntityException;

    /**
     * Get the different levels of security that the user can see across all projects.
     *
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     *
     * @deprecated Please use {@link #getAllUsersSecurityLevels(com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    Collection<GenericValue> getAllUsersSecurityLevels(com.opensymphony.user.User user) throws GenericEntityException;

    /**
     * Get all the different levels of security across all schemes.
     *
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     */
    Collection<GenericValue> getAllSecurityLevels() throws GenericEntityException;

    /**
     * Get the different levels of security that a user can see that have the specified name.
     *
     * @param user the user
     * @param securityLevelName the name of the security level.
     * @return a collection of the GenericValues representing each level they can see with the specified name.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     */
    Collection<GenericValue> getUsersSecurityLevelsByName(User user, String securityLevelName) throws GenericEntityException;

    /**
     * Get the different levels of security that a user can see that have the specified name.
     *
     * @param user the user
     * @param securityLevelName the name of the security level.
     * @return a collection of the GenericValues representing each level they can see with the specified name.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     *
     * @deprecated Please use {@link #getUsersSecurityLevelsByName(com.atlassian.crowd.embedded.api.User, String)} instead. Since v4.3
     */
    Collection<GenericValue> getUsersSecurityLevelsByName(com.opensymphony.user.User user, String securityLevelName) throws GenericEntityException;

    /**
     * Get the different levels of security that have the specified name.
     *
     * @param securityLevelName the name of the security level.
     * @return a collection of the GenericValues representing each level with the specified name.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     */
    Collection<GenericValue> getSecurityLevelsByName(String securityLevelName) throws GenericEntityException;

    Long getSchemeDefaultSecurityLevel(GenericValue project) throws GenericEntityException;

    GenericValue getIssueSecurityLevel(Long id) throws GenericEntityException;

    void deleteSecurityLevel(Long levelId) throws GenericEntityException;

    void clearUsersLevels();

    void clearProjectLevels(GenericValue project);
}
