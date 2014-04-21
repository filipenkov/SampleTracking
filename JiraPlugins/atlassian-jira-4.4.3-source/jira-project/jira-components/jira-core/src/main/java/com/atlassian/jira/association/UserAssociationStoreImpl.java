package com.atlassian.jira.association;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @since v4.3
 */
public class UserAssociationStoreImpl implements UserAssociationStore
{
    private final OfBizDelegator ofBizDelegator;
    private UserManager userManager;

    public UserAssociationStoreImpl(OfBizDelegator ofBizDelegator, UserManager userManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.userManager = userManager;
    }

    public void createAssociation(final String associationType, final User user, final GenericValue sink)
    {
        createAssociation(associationType, user.getName(), sink.getEntityName(), sink.getLong("id"));
    }

    public void createAssociation(final String associationType, final String userName, final String sinkNodeEntity, final Long sinkNodeId)
    {
        GenericValue association = getAssociation(userName, sinkNodeId, sinkNodeEntity, associationType);
        if (association == null)
        {
            final FieldMap fields = FieldMap.build("associationType", associationType)
                    .add("sourceName", userName).add("sinkNodeEntity", sinkNodeEntity).add("sinkNodeId", sinkNodeId)
                    .add("created", new Timestamp(System.currentTimeMillis()));
            // Can't use ofBizDelegator.createValue() because this will try to force an "id" field into the GV
            final GenericValue genericValue = ofBizDelegator.makeValue("UserAssociation");
            genericValue.setFields(fields);
            try
            {
                genericValue.create();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    public void removeAssociation(String associationType, User user, GenericValue sink)
    {
        removeAssociation(associationType, user.getName(), sink);
    }

    public void removeAssociation(String associationType, String username, GenericValue sink)
    {
        final FieldMap fields = new FieldMap();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("sourceName", username);
        fields.put("associationType", associationType);
        ofBizDelegator.removeByAnd("UserAssociation", fields);
    }

    public void removeUserAssociationsFromUser(final String associationType, final User user, final String sinkNodeEntity)
    {
        final FieldMap fields = new FieldMap();
        fields.put("sourceName", user.getName());
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sinkNodeEntity);
        ofBizDelegator.removeByAnd("UserAssociation", fields);
    }

    public boolean associationExists(final String associationType, final User user, final GenericValue sink)
    {
        return associationExists(associationType, user, sink.getEntityName(), sink.getLong("id"));
    }

    public boolean associationExists(final String associationType, final User user, final String sinkNodeEntity, final Long sinkNodeId)
    {
        return user != null && ofBizDelegator.findByAnd("UserAssociation", fieldMap(associationType, user.getName(), sinkNodeEntity, sinkNodeId)).size() > 0;
    }

    public List<User> getUsersFromSink(String associationType, GenericValue sink)
    {
        if (sink == null)
        {
            throw new IllegalArgumentException("Sink GenericValue can not be null.");
        }

        final FieldMap fields = new FieldMap();
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("sinkNodeId", sink.getLong("id"));

        final List<GenericValue> results = getAssociations(fields);

        final List<User> outList = new ArrayList<User>(results.size());
        for (final GenericValue result : results)
        {
            outList.add(userManager.getUser(result.getString("sourceName")));
        }
        return outList;
    }

    public List<String> getUsernamesFromSink(String associationType, GenericValue sink)
    {
        if (sink == null)
        {
            throw new IllegalArgumentException("Sink GenericValue can not be null.");
        }

        final FieldMap fields = new FieldMap();
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("sinkNodeId", sink.getLong("id"));

        final List<GenericValue> results = getAssociations(fields);

        final List<String> outList = new ArrayList<String>(results.size());
        for (final GenericValue result : results)
        {
            outList.add(result.getString("sourceName"));
        }
        return outList;
    }

    public List<GenericValue> getSinksFromUser(String associationType, User user, String sinkNodeEntity)
    {
        final List<GenericValue> associations = getAssociationsForUser(associationType, user, sinkNodeEntity);

        final List<GenericValue> sinks = new ArrayList<GenericValue>(associations.size());
        for (final GenericValue association : associations)
        {
            GenericValue sink = ofBizDelegator.findByPrimaryKey(sinkNodeEntity, association.getLong("sinkNodeId"));

            if (sink != null)
            {
                sinks.add(sink);
            }
        }
        return sinks;
    }

    private List<GenericValue> getAssociationsForUser(final String associationType, final User user, final String sinkNodeEntity)
    {
        Assertions.notNull("user", user);
        final FieldMap fields = new FieldMap();
        fields.put("associationType", associationType);
        fields.put("sourceName", user.getName());
        fields.put("sinkNodeEntity", sinkNodeEntity);
        return getAssociations(fields);
    }

    private List<GenericValue> getAssociations(Map<String, ?> fields)
    {

        return ofBizDelegator.findByAnd("UserAssociation", fields);
    }

    private Map<String, ?> fieldMap(final String associationType, final String userName, final String sinkNodeEntity, final Long sinkNodeId)
    {
        return FieldMap.build("associationType", associationType)
                .add("sourceName", userName).add("sinkNodeEntity", sinkNodeEntity).add("sinkNodeId", sinkNodeId);
    }

//    private UserAssociation createUserAssociation(final GenericValue genericValue)
//    {
//        if (genericValue == null)
//        {
//            return null;
//        }
//        return new ImmutableUserAssociation(genericValue);
//    }


    private GenericValue getAssociation(String userName, Long sinkNodeId, String sinkNodeEntity, String associationType)
    {
        final FieldMap fields = FieldMap.build("associationType", associationType)
                .add("sourceName", userName).add("sinkNodeEntity", sinkNodeEntity).add("sinkNodeId", sinkNodeId);
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("UserAssociation", fields));
    }
}
