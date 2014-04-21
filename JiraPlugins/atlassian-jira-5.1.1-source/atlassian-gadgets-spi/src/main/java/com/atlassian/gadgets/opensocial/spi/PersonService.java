package com.atlassian.gadgets.opensocial.spi;

import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.Group;
import com.atlassian.gadgets.opensocial.model.Person;

/**
 * Interface for handling {@code Person} requests.
 *
 * <p>
 * The {@link PersonService} has three functions: first, it resolves {@code String}-based person id's, ensuring that a user corresponding to
 * the requested id exists in the system. Second, it aggregates social data associated with a user (which may be stored in disparate places in the host application)
 *  into a single {@link Person} container. Third, it aggregates {@link Group} members in {@link PersonService#getPeople}, collecting all members of a named group
 * for each of a set of {@link Person}s together into one collection. All of this data is expected to be stored persistently by the host application. For example, a typical
 * {@link PersonService} implementation might resolve {@link Person} requests against its database of users, filling in the {@link Person} fields
 * from stored profile information for that user.
 *
 * <p>
 * This is an optional SPI that host applications may implement if they wish to provide the ability to query person and group
 * data using the OpenSocial APIs. Host applications that wish to provide {@link AppDataService} or {@link ActivityService} implementations
 * must also implement {@link PersonService} as well, as those services depend on this service.
 *
 * <p>
 * The {@link PersonService} is <b>not</b> intended to provide a mechanism for updating social data.
 *
 * @since 2.0
 */
public interface PersonService
{
    /**
     * Returns a {@link Person} corresponding to the {@code personId}
     * @param personId The requested person id
     * @param requestContext context for this request
     * @return a {@link Person} corresponding to the requested {@code personId}, or {@code null} if the requested {@code personId} could not be resolved
     * @throws PersonServiceException if there is a problem while performing this operation
     */
    @Nullable Person getPerson(String personId, OpenSocialRequestContext requestContext) throws PersonServiceException;

    /**
     * Returns a {@link java.util.List} corresponding to the group members of the passed in person IDs
     * @param personIds A set of users
     * @param group The group to select from each of the passed in {@code personIds}
     * @param requestContext context for this request
     * @return a set of people
     * @throws PersonServiceException if there is a problem while performing this operation
     */
    Set<Person> getPeople(Set<String> personIds, Group group, OpenSocialRequestContext requestContext) throws PersonServiceException;
}
