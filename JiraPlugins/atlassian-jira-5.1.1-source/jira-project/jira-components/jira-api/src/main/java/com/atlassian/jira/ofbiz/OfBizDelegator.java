package com.atlassian.jira.ofbiz;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.exception.DataAccessException;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelReader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A wrapper around {@link org.ofbiz.core.entity.DelegatorInterface} that does not throw {@link GenericEntityException}.
 */
@PublicApi
public interface OfBizDelegator
{

    public static final String VERSION = "Version";
    public static final String ISSUE_LINK = "IssueLink";
    public static final String ISSUE_LINK_TYPE = "IssueLinkType";
    public static final String PROJECT_COMPONENT = "Component";

    /**
     * Finds GenericValue records by the specified field value.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fieldName The field to do filtering by.
     * @param fieldValue The desired value for the filtering field.
     * @return List of GenericValue instances that match the query
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByField(String entityName, String fieldName, Object fieldValue);

    /**
     * Finds GenericValue records by the specified field value.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fieldName The field to do filtering by.
     * @param fieldValue The desired value for the filtering field.
     * @param orderBy Single field to order by.
     * @return List of GenericValue instances that match the query
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByField(String entityName, String fieldName, Object fieldValue, String orderBy);

    /**
     * Finds GenericValue records by all of the specified fields (ie: combined using AND).
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fields The fields of the named entity to query by with their corresponding values
     * @return List of GenericValue instances that match the query
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByAnd(String entityName, Map<String, ?> fields) throws DataAccessException;

    List<GenericValue> findByAnd(String entityName, Map<String, ?> fields, List<String> orderBy) throws DataAccessException;

    List<GenericValue> findByAnd(String entityName, List<EntityCondition> expressions) throws DataAccessException;

    List<GenericValue> findByOr(String entityName, List<? extends EntityCondition> expressions, List<String> orderBy) throws DataAccessException;

    List<GenericValue> findByLike(String entityName, Map<String, ?> map) throws DataAccessException;

    List<GenericValue> findByLike(String entityName, Map<String, ?> map, List<String> orderBy) throws DataAccessException;

    void removeAll(List<GenericValue> genericValues) throws DataAccessException;

    int removeByAnd(String s, Map<String, ?> map) throws DataAccessException;


    /**
     * Remove the given entity from the DB.
     *
     * @param entityName the entity type (ie TABLE)
     * @param id the id of the row to delete.
     *
     * @return number of rows effected by this operation
     */
    int removeById(String entityName, Long id);

    int removeValue(GenericValue value) throws DataAccessException;

    void storeAll(List<GenericValue> genericValues) throws DataAccessException;

    List<GenericValue> findAll(String s) throws DataAccessException;

    List<GenericValue> findAll(String s, List<String> orderBy) throws DataAccessException;

    void store(GenericValue gv) throws DataAccessException;

    /**
     * Creates a new GenericValue, and persists it.
     * If there is no "id" in the field values, one is created using the entity sequence.
     *
     * @param entityName the entity name.
     * @param fields field values
     * @return The new GenericValue.
     * @throws DataAccessException if an error occurs in the Database layer
     *
     * @see #makeValue(String)
     */
    GenericValue createValue(String entityName, Map<String, Object> fields) throws DataAccessException;

    /**
     * Creates an Entity in the form of a GenericValue without persisting it.
     *
     * @param entityName the entity name.
     * @return The new GenericValue.
     *
     * @see #makeValue(String, java.util.Map)
     * @see #createValue(String, java.util.Map)
     */
    GenericValue makeValue(String entityName);

    /**
     * Creates an Entity in the form of a GenericValue without persisting it.
     *
     * @param entityName the entity name.
     * @param fields initial field values
     * @return The new GenericValue.
     *
     * @see #makeValue(String)
     * @see #createValue(String, java.util.Map)
     */
    GenericValue makeValue(String entityName, Map<String, Object> fields);

    /**
     * Find a Generic Entity by its numeric ID.
     *
     * <p> This method is a synonym for {@link #findByPrimaryKey(String, Long)}
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param id The numeric "id" field value that is the primary key of this entity.
     * @return The GenericValue corresponding to the ID
     *
     * @throws com.atlassian.jira.exception.DataAccessException DataAccessException
     */
    GenericValue findById(String entityName, Long id) throws DataAccessException;

    /**
     * Find a Generic Entity by its single numeric Primary Key.
     *
     * <p> This method is a convenience for entities with a numeric primary key on single field called "id".
     * This is the case for most JIRA entities.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param id The numeric "id" field value that is the primary key of this entity.
     * @return The GenericValue corresponding to the primary key
     *
     * @throws com.atlassian.jira.exception.DataAccessException DataAccessException
     * @see #findByPrimaryKey(String, java.util.Map)
     */
    GenericValue findByPrimaryKey(String entityName, Long id) throws DataAccessException;

    /**
     * Find a Generic Entity by its Primary Key.
     *
     * @param entityName The Name of the Entity as defined in the entity XML file
     * @param fields The field/value pairs of the primary key (in JIRA, mostly just a single field "id")
     * @return The GenericValue corresponding to the primary key
     *
     * @throws com.atlassian.jira.exception.DataAccessException DataAccessException
     * @see #findByPrimaryKey(String, Long)
     */
    GenericValue findByPrimaryKey(String entityName, Map<String, ?> fields) throws DataAccessException;

    List<GenericValue> getRelated(String relationName, GenericValue gv) throws DataAccessException;

    /**
     * Ensure that there is a view-entity defined in entitymodel.xml (and entitygroup.xml) for the entity
     * you are calling this method with, and  that the view-entity is named correctly!
     * The view-entity must be named the name of the normal entity with 'Count' appended. For example for the
     * 'Issue' entity the view-entity must be called 'IssueCount'.
     * Otherwise an exception will be thrown.
     *
     * @param entityName entity name
     * @return count
     * @throws DataAccessException if data access problems occur
     */
    long getCount(String entityName) throws DataAccessException;

    /**
     * Returns a new OfBizListIterator.
     * <p/>
     * <b>IMPORTANT</b>: the returned iterator needs to be propery closed in a {@code finally} block to avoid connection
     * leaks.
     */
    OfBizListIterator findListIteratorByCondition(String entityType, EntityCondition condition) throws DataAccessException;

    /**
     * Returns a new OfBizListIterator.
     * <p/>
     * <b>IMPORTANT</b>: the returned iterator needs to be propery closed in a {@code finally} block to avoid connection
     * leaks.
     */
    OfBizListIterator findListIteratorByCondition(String entityName, EntityCondition whereEntityCondition, EntityCondition havingEntityCondition, Collection<String> fieldsToSelect, List<String> orderBy, EntityFindOptions entityFindOptions) throws DataAccessException;

    /**
     * This can be used to perform an update on the entityName of all the rows
     * identified by the keys with the values stored in the updateValues.
     *
     * @param entityName   identifies the table to perform the update on.
     * @param updateValues is a map where the key is the fieldName and the value
     *                     is the value to update the column to.
     * @param keys         is a list of Long values that represent the primary keys of the
     *                     the where clause.
     * @return the number of rows updated
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    int bulkUpdateByPrimaryKey(String entityName, Map<String, ?> updateValues, List<Long> keys) throws DataAccessException;

    /**
     * This can be used to perform an update on the entityName of all the rows
     * identified by AND criteria of the fields specified by the criteria map.
     *
     * @param entityName   identifies the table to perform the update on.
     * @param updateValues is a map where the key is the fieldName and the value
     *                     is the value to update the column to.
     * @param criteria     map of field to value mapping that will be used to generate the
     *                     where clause of the update SQL statement. Multiple entries in the map are joined using the
     *                     AND operator.
     * @return the number of rows updated
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    int bulkUpdateByAnd(String entityName, Map<String, ?> updateValues, Map<String, ?> criteria) throws DataAccessException;

    /**
     * This can be used to perform an update on the entityName of all the rows
     * identified by AND criteria of the fields specified by the criteria map.
     *
     * @param entityName    table na,e
     * @param updateColumns map of update to - update from columns
     * @param criteria      map of column names and their values that will create WHERE clause
     * @return the number of rows updated
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    int bulkCopyColumnValuesByAnd(String entityName, Map updateColumns, Map criteria) throws DataAccessException;

    /**
     * This can be used to remove rows for a given entity based on <code>entityName</code>
     * and where <code>entityId</q
     *
     * @param entityName identifies the table to perform the remove on.
     * @param entityId   the Ofbiz fieldName to be used for the identifier, eg WHERE fieldName IN (...). Must be the
     *                   same case as that found in entitymodel.xml.
     * @param ids        a list of entity IDs of the rows to be removed
     * @return number of rows removed
     * @throws GenericModelException if the given entityId is not valid for the given entity
     * @throws DataAccessException   if there are problems executing/accessing the data store
     */
    int removeByOr(String entityName, String entityId, List<Long> ids) throws DataAccessException, GenericModelException;

    /**
     * Finds GenericValues by the conditions specified in the EntityCondition object.
     *
     * @param entityName The Name of the Entity as defined in the entity model XML file
     * @param entityCondition The EntityCondition object that specifies how to constrain this query
     * @param fieldsToSelect The fields of the named entity to get from the database; if empty or null all fields will be retreived
     * @param orderBy The fields of the named entity to order the query by; optionally add a " ASC" for ascending or " DESC" for descending
     * @return List of GenericValue objects representing the search results
     *
     * @since v3.12
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByCondition(String entityName, EntityCondition entityCondition, Collection<String> fieldsToSelect, List<String> orderBy) throws DataAccessException;

    /**
     * Finds GenericValues by the conditions specified in the EntityCondition object with no specified order.
     * <p>
     * Convenience method for calling {@link #findByCondition(String, EntityCondition, Collection, List)} with 
     * an empty orderBy list.
     *
     * @param entityName The Name of the Entity as defined in the entity model XML file
     * @param entityCondition The EntityCondition object that specifies how to constrain this query
     * @param fieldsToSelect The fields of the named entity to get from the database; if empty or null all fields will be retreived
     * @return List of GenericValue objects representing the search results
     *
     * @since v4.1
     * @throws com.atlassian.jira.exception.DataAccessException If an error occurs in the persistence layer.
     */
    List<GenericValue> findByCondition(String entityName, EntityCondition entityCondition, Collection<String> fieldsToSelect) throws DataAccessException;

    /**
     * Returns a model reader that can be used to retrieve all the different entitynames configured in the
     * entitymodel.
     *
     * @return a {@link ModelReader}
     * @since 4.4
     */
    ModelReader getModelReader();

    /**
     * Refreshes the sequencer that is used to retrieve unique IDs in the database.
     *
     * @since 4.4
     */
    void refreshSequencer();
}
