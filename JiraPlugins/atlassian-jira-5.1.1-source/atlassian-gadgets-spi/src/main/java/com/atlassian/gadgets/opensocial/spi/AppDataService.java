package com.atlassian.gadgets.opensocial.spi;

import java.util.Map;
import java.util.Set;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.AppId;
import com.atlassian.gadgets.opensocial.model.PersonId;

/**
 * Interface for handling application data requests. The appId parameter in the methods below is a unique identifier for
 * a particular gadget (e.g. Chart Gadget).
 *
 * <p>
 * This is an optional SPI that host applications may implement if they wish to provide the ability to read and store
 * custom application data using the OpenSocial APIs. If the {@link AppDataService} is implemented, an implementation of [@link PersonService}
 * must also be provided. If an implementation of {@link AppDataService} is not supplied, the OpenSocial appData API will not be supported,
 * but other OpenSocial and Gadget functionality will work normally.
 *
 * <p>
 * Application data is a per-{@link com.atlassian.gadgets.opensocial.model.Person}, per-{@link AppId} persistent store of field / value pairs. The fields and values are stored as {@link String}s.
 * Implementations may choose to restrict the set of field names that may be stored. There is no default set of field names that must be
 * supported.
 *
 * @since 2.0
 */
public interface AppDataService
{
    /**
     * Fetch the application data for the specified people
     * @param people The people whose data is being requested
     * @param appId The app id for the gadget making this request
     * @param fields The requested fields
     * @param requestContext The request context
     * @return the data, as a map from person to field / value pairs
     * @throws AppDataServiceException if there is a problem while performing this operation
     */
    public Map<PersonId, Map<String, String>> getPeopleData(Set<PersonId> people, AppId appId, Set<String> fields, OpenSocialRequestContext requestContext) throws AppDataServiceException;

    /**
     * Fetch the application data for the specified people and <b>all</b> fields
     * @param people The people whose data is being requested
     * @param appId The app id for the gadget making this request
     * @param requestContext The request context
     * @return the data, as a map from person to field / value pairs. All field / value pairs for the person are returned
     * @throws AppDataServiceException
     */
    public Map<PersonId, Map<String, String>> getPeopleData(Set<PersonId> people, AppId appId, OpenSocialRequestContext requestContext) throws AppDataServiceException;

    /**
     * Delete all application data for the specified person and fields
     * @param personId The person whose data is being deleted
     * @param appId The app id for the gadget making this request
     * @param fields The requested fields
     * @param requestContext The request context
     * @throws AppDataServiceException if there is a problem while performing this operation
     */
    public void deletePersonData(PersonId personId, AppId appId, Set<String> fields, OpenSocialRequestContext requestContext) throws AppDataServiceException;

    /**
     * Delete all application data for the specified person and <b>all</b> fields
     * @param personId The person whose data is being deleted
     * @param appId The app id for the gadget making this request
     * @param requestContext The request context
     * @throws AppDataServiceException if there is a problem while performing this operation
     */
    public void deletePersonData(PersonId personId, AppId appId, OpenSocialRequestContext requestContext) throws AppDataServiceException;

    /**
     * Update (add or modify) application data for the specified person with the specified field / value pairs.
     * 
     * @param personId The person whose data is being updated
     * @param appId The app id for the gadget making this request
     * @param values The new values for the fields to take on
     * @param requestContext The request context
     * @throws AppDataServiceException if there is a problem while performing this operation
     */
    public void updatePersonData(PersonId personId, AppId appId, Map<String, String> values, OpenSocialRequestContext requestContext) throws AppDataServiceException;
}

