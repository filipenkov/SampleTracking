package com.atlassian.core.ofbiz;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.association.DefaultAssociationManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.association.UserAssociationStoreImpl;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericPK;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.MockModelEntity;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.util.UtilMisc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.elementsEqual;
import static com.google.common.collect.Iterables.find;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultAssociationManager extends ListeningTestCase
{
    public static final String NODE_ASSOCIATION_ENTITY_NAME = "NodeAssociation";
    private GenericDelegator databaseDelegator;
    private AssociationManager associationManager;
    private GenericValue entity1;
    private GenericValue entity2;
    private GenericValue entity3;
    private GenericValue entity4;
    private GenericValue entity5;

    private User adminUser;

    @Before
    public void setUp() throws Exception
    {
        adminUser = ImmutableUser.newUser().name("admin").toUser();
        entity1 = new MockGenericValue("Issue", ImmutableMap.of("id", 1L));
        entity2 = new MockGenericValue("Issue", ImmutableMap.of("id", 2L));
        entity3 = new MockGenericValue("Issue", ImmutableMap.of("id", 3L));
        entity4 = new MockGenericValue("Issue", ImmutableMap.of("id", 4L));
        entity5 = new MockGenericValue("Issue", ImmutableMap.of("id", 5L));
        databaseDelegator = Stubs.DatabaseDelegator.create(ImmutableList.of(entity1, entity2, entity3, entity4, entity5));
        associationManager = new DefaultAssociationManager(databaseDelegator);
    }

    @Test
    public void creatingAnAssociationShouldCreateANewValueInTheDatabaseAndReturnItWhenThereIsNoExistingValue()
            throws GenericEntityException
    {
        when(databaseDelegator.findByAnd(eq(NODE_ASSOCIATION_ENTITY_NAME), anyMap())).thenReturn(null);

        final GenericValue expectedAssociation = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                (
                        "associationType", "IssueComponent",
                        "sourceNodeId", entity3.getLong("id"),
                        "sourceNodeEntity", entity3.getEntityName(),
                        "sinkNodeId", entity1.get("id"),
                        "sinkNodeEntity", entity1.getEntityName()
                ));

        final GenericValue actualAssociation = associationManager.createAssociation(entity3, entity1, "IssueComponent");

        verify(databaseDelegator, times(1)).
                makeValue
                        (
                                expectedAssociation.getEntityName(),
                                expectedAssociation.getAllFields()
                        );

        assertEquals(expectedAssociation, actualAssociation);
    }

    @Test
    public void creatingAnAssociationShouldReturnTheValueStoredInTheDatabaseWhenTheAssociationAlreadyExists()
            throws GenericEntityException
    {
        final GenericValue expectedAssociation = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                (
                        "associationType", "IssueComponent",
                        "sourceNodeId", entity3.getLong("id"),
                        "sourceNodeEntity", entity3.getEntityName(),
                        "sinkNodeId", entity1.get("id"),
                        "sinkNodeEntity", entity1.getEntityName()
                ));

        when(databaseDelegator.findByAnd(eq(NODE_ASSOCIATION_ENTITY_NAME), anyMap())).
                thenReturn(ImmutableList.of(expectedAssociation));

        final GenericValue actualAssociation = associationManager.createAssociation(entity3, entity1, "IssueComponent");

        assertEquals(expectedAssociation, actualAssociation);
    }

    @Test
    public void creatingAnAssociationShouldNotCreateAnyValuesInTheDatabaseWhenTheAssociationAlreadyExists()
            throws GenericEntityException
    {
        final GenericValue expectedAssociation = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                (
                        "associationType", "IssueComponent",
                        "sourceNodeId", entity3.getLong("id"),
                        "sourceNodeEntity", entity3.getEntityName(),
                        "sinkNodeId", entity1.get("id"),
                        "sinkNodeEntity", entity1.getEntityName()
                ));

        when(databaseDelegator.findByAnd(eq(NODE_ASSOCIATION_ENTITY_NAME), anyMap())).
                thenReturn(ImmutableList.of(expectedAssociation));

        associationManager.createAssociation(entity3, entity1, "IssueComponent");

        verify(databaseDelegator, never()).makeValue(anyString(), anyMap());
    }

    @Test
    public void gettingAnAssociationReturnsTheValueStoredInTheDatabaseWhenItExists() throws GenericEntityException
    {
        final GenericValue expectedAssociation = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                (
                        "associationType", "IssueComponent",
                        "sourceNodeId", entity3.getLong("id"),
                        "sourceNodeEntity", entity3.getEntityName(),
                        "sinkNodeId", entity1.get("id"),
                        "sinkNodeEntity", entity1.getEntityName()
                ));

        when(databaseDelegator.findByAnd(eq(NODE_ASSOCIATION_ENTITY_NAME), anyMap())).
                thenReturn(ImmutableList.of(expectedAssociation));

        final GenericValue actualAssociation = associationManager.getAssociation(entity3, entity1, "IssueComponent");
        assertEquals(actualAssociation, expectedAssociation);
    }

    @Test
    public void removingAnAssociationShouldRemoveTheDatabaseValueUsingThePrimaryKeyWhenTheValueExists()
            throws GenericEntityException
    {
        final GenericValue associationToBeRemoved = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                (
                        "associationType", "IssueComponent",
                        "sourceNodeId", entity3.getLong("id"),
                        "sourceNodeEntity", entity3.getEntityName(),
                        "sinkNodeId", entity1.get("id"),
                        "sinkNodeEntity", entity1.getEntityName()
                ));

        final GenericPK primaryKeyForAssociationToBeRemoved =
                new GenericPK(databaseDelegator, Stubs.Entities.nodeAssociation(), associationToBeRemoved.getAllFields());

        associationManager.removeAssociation(entity3, entity1, "IssueComponent");

        verify(databaseDelegator, times(1)).
                removeByPrimaryKey
                        (
                                primaryKeyForAssociationToBeRemoved
                        );
    }

    @Test
    public void removingAssociationsFromASourceValueShouldRemoveAllAssociationsFromThatValueInTheDatabase()
            throws GenericEntityException
    {
        associationManager.removeAssociationsFromSource(entity3);
        verify(databaseDelegator).removeByAnd
                (
                        NODE_ASSOCIATION_ENTITY_NAME,
                        ImmutableMap.of
                                (
                                        "sourceNodeId", entity3.get("id"),
                                        "sourceNodeEntity", entity3.getEntityName()
                                )
                );
    }

    @Test
    public void removingAssociationsFromASinkValueShouldRemoveAllAssociationsToThatValueInTheDatabase()
            throws GenericEntityException
    {
        associationManager.removeAssociationsFromSink(entity3);
        verify(databaseDelegator).removeByAnd
                (
                        NODE_ASSOCIATION_ENTITY_NAME,
                        ImmutableMap.of
                                (
                                        "sinkNodeId", entity3.get("id"),
                                        "sinkNodeEntity", entity3.getEntityName()
                                )
                );
    }

    @Test
    public void swappingAssociationsOfAllSourceEntitiesOfAGivenTypeShouldRemoveTheAssociationsFromEachSourceEntityToThePreviousSinkValue()
            throws GenericEntityException
    {
        final ImmutableList<GenericValue> existingAssociationsToEntity1FromIssueEntities =
                ImmutableList.<GenericValue>of
                        (
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity1.getLong("id"),
                                                "sinkNodeEntity", entity1.getEntityName(),
                                                "sourceNodeId", entity2.getLong("id"),
                                                "sourceNodeEntity", entity2.getEntityName(),
                                                "associationType", "AnAssociationType"
                                        )),
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity1.getLong("id"),
                                                "sinkNodeEntity", entity1.getEntityName(),
                                                "sourceNodeId", entity3.getLong("id"),
                                                "sourceNodeEntity", entity3.getEntityName(),
                                                "associationType", "AnAssociationType"
                                        )
                                )
                        );

        when(databaseDelegator.
                findByAnd
                        (
                                NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "sinkNodeId", entity1.getLong("id"),
                                        "sinkNodeEntity", entity1.getEntityName(),
                                        "associationType", "AnAssociationType",
                                        "sourceNodeEntity", "Issue"
                                )
                        )
        ).thenReturn(existingAssociationsToEntity1FromIssueEntities);

        final GenericPK primaryKeyOfFirstAssociationToBeRemoved =
                new GenericPK
                        (
                                databaseDelegator, Stubs.Entities.nodeAssociation(),
                                existingAssociationsToEntity1FromIssueEntities.get(0).getAllFields()
                        );

        final GenericPK primaryKeyOfSecondAssociationToBeRemoved =
                new GenericPK
                        (
                                databaseDelegator, Stubs.Entities.nodeAssociation(),
                                existingAssociationsToEntity1FromIssueEntities.get(1).getAllFields()
                        );

        associationManager.swapAssociation("Issue", "AnAssociationType", entity1, entity5);

        verify(databaseDelegator).removeByPrimaryKey(primaryKeyOfFirstAssociationToBeRemoved);
        verify(databaseDelegator).removeByPrimaryKey(primaryKeyOfSecondAssociationToBeRemoved);
    }

    @Test
    public void swappingAssociationsOfAllSourceEntitiesOfAGivenTypeShouldAddAssociationsFromEachSourceEntityOfThatTypeToTheNewSinkValue()
            throws GenericEntityException
    {
        final ImmutableList<GenericValue> existingAssociationsToEntity1FromIssueEntities =
                ImmutableList.<GenericValue>of
                        (
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity1.getLong("id"),
                                                "sinkNodeEntity", entity1.getEntityName(),
                                                "sourceNodeId", entity2.getLong("id"),
                                                "sourceNodeEntity", entity2.getEntityName(),
                                                "associationType", "AnAssociationType"
                                        )),
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity1.getLong("id"),
                                                "sinkNodeEntity", entity1.getEntityName(),
                                                "sourceNodeId", entity3.getLong("id"),
                                                "sourceNodeEntity", entity3.getEntityName(),
                                                "associationType", "AnAssociationType"
                                        )
                                )
                        );

        when(databaseDelegator.
                findByAnd
                        (
                                NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "sinkNodeId", entity1.getLong("id"),
                                        "sinkNodeEntity", entity1.getEntityName(),
                                        "associationType", "AnAssociationType",
                                        "sourceNodeEntity", "Issue"
                                )
                        )
        ).thenReturn(existingAssociationsToEntity1FromIssueEntities);


        final ImmutableList<GenericValue> expectedNewAssociationsFromIssueEntitiesToEntity5 =
                ImmutableList.<GenericValue>of
                        (
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity5.getLong("id"),
                                                "sinkNodeEntity", entity5.getEntityName(),
                                                "sourceNodeId", entity2.getLong("id"),
                                                "sourceNodeEntity", entity2.getEntityName(),
                                                "associationType", "AnAssociationType"
                                        )),
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity5.getLong("id"),
                                                "sinkNodeEntity", entity5.getEntityName(),
                                                "sourceNodeId", entity3.getLong("id"),
                                                "sourceNodeEntity", entity3.getEntityName(),
                                                "associationType", "AnAssociationType"
                                        )
                                )
                        );

        associationManager.swapAssociation("Issue", "AnAssociationType", entity1, entity5);

        verify(databaseDelegator).
                makeValue
                        (
                                expectedNewAssociationsFromIssueEntitiesToEntity5.get(0).getEntityName(),
                                expectedNewAssociationsFromIssueEntitiesToEntity5.get(0).getAllFields()
                        );

        verify(databaseDelegator).
                makeValue
                        (
                                expectedNewAssociationsFromIssueEntitiesToEntity5.get(1).getEntityName(),
                                expectedNewAssociationsFromIssueEntitiesToEntity5.get(1).getAllFields()
                        );
    }

    @Test
    public void swappingAssociationsOfAllSourceEntitiesOfAGivenTypeShouldNotSwapAssociationsOfADifferentAssociationType()
            throws GenericEntityException
    {
        final ImmutableList<GenericValue> existingAssociationsToEntity1FromIssueEntities =
                ImmutableList.<GenericValue>of
                        (
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity1.getLong("id"),
                                                "sinkNodeEntity", entity1.getEntityName(),
                                                "sourceNodeId", entity3.getLong("id"),
                                                "sourceNodeEntity", entity3.getEntityName(),
                                                "associationType", "AssociationTypeA"
                                        )),
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity1.getLong("id"),
                                                "sinkNodeEntity", entity1.getEntityName(),
                                                "sourceNodeId", entity4.getLong("id"),
                                                "sourceNodeEntity", entity4.getEntityName(),
                                                "associationType", "AssociationTypeB"
                                        )
                                )
                        );

        when(databaseDelegator.
                findByAnd
                        (
                                NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "sinkNodeId", entity1.getLong("id"),
                                        "sinkNodeEntity", entity1.getEntityName(),
                                        "associationType", "AssociationTypeA",
                                        "sourceNodeEntity", "Issue"
                                )
                        )
        ).thenReturn(ImmutableList.of(existingAssociationsToEntity1FromIssueEntities.get(0)));

        final GenericValue expectedNewAssociationEntity3ToEntity2 =
                                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                        (
                                                "sinkNodeId", entity2.getLong("id"),
                                                "sinkNodeEntity", entity2.getEntityName(),
                                                "sourceNodeId", entity3.getLong("id"),
                                                "sourceNodeEntity", entity3.getEntityName(),
                                                "associationType", "AssociationTypeA"
                                        ));

        final GenericValue unExpectedAssociationFromEntity4ToEntity2 =
                new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                        (
                                "sinkNodeId", entity2.getLong("id"),
                                "sinkNodeEntity", entity2.getEntityName(),
                                "sourceNodeId", entity4.getLong("id"),
                                "sourceNodeEntity", entity4.getEntityName(),
                                "associationType", "AssociationTypeB"
                        ));

        associationManager.swapAssociation("Issue", "AssociationTypeA", entity1, entity2);

        // Verify that the sinks have been swapped for the Issue entities where the association type equals
        // AssociationTypeA
        verify(databaseDelegator).
                makeValue
                        (
                                expectedNewAssociationEntity3ToEntity2.getEntityName(),
                                expectedNewAssociationEntity3ToEntity2.getAllFields()
                        );

        // Verify that the sinks have not been swapped for the Issue entities where the association type equals
        // AssociationTypeB
        verify(databaseDelegator, never()).
                makeValue
                        (
                                unExpectedAssociationFromEntity4ToEntity2.getEntityName(),
                                unExpectedAssociationFromEntity4ToEntity2.getAllFields()
                        );
    }

    @Test
    public void swappingAssociationsForAListOfSourceEntitiesShouldRemoveTheAssociationsFromEachSourceValueToThePreviousSinkValue()
            throws Exception
    {
        final GenericPK primaryKeyOfFirstAssociationToBeRemoved =
                new GenericPK(databaseDelegator, Stubs.Entities.nodeAssociation(),
                        ImmutableMap.of
                                (
                                        "sinkNodeId", entity1.getLong("id"),
                                        "sinkNodeEntity", entity1.getEntityName(),
                                        "sourceNodeId", entity2.getLong("id"),
                                        "sourceNodeEntity", entity2.getEntityName(),
                                        "associationType", "AnAssociationType"
                                ));

        final GenericPK primaryKeyOfSecondAssociationToBeRemoved =
                new GenericPK(databaseDelegator, Stubs.Entities.nodeAssociation(),
                        ImmutableMap.of
                                (
                                        "sinkNodeId", entity1.getLong("id"),
                                        "sinkNodeEntity", entity1.getEntityName(),
                                        "sourceNodeId", entity3.getLong("id"),
                                        "sourceNodeEntity", entity3.getEntityName(),
                                        "associationType", "AnAssociationType"
                                )
                );

        associationManager.swapAssociation(ImmutableList.of(entity2, entity3), "AnAssociationType", entity1, entity5);

        verify(databaseDelegator).removeByPrimaryKey(primaryKeyOfFirstAssociationToBeRemoved);
        verify(databaseDelegator).removeByPrimaryKey(primaryKeyOfSecondAssociationToBeRemoved);
    }

    @Test
    public void swappingAssociationsForAListOfSourceEntitiesShouldCreateAssociationsFromEachSourceValueToTheNewSinkValue()
            throws Exception
    {
        final GenericValue firstValueToBeCreated = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME,
                ImmutableMap.of
                        (
                                "sinkNodeId", entity5.getLong("id"),
                                "sinkNodeEntity", entity5.getEntityName(),
                                "sourceNodeId", entity2.getLong("id"),
                                "sourceNodeEntity", entity2.getEntityName(),
                                "associationType", "AnAssociationType"
                        ));

        final GenericValue secondValueToBeCreated = new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME,
                        ImmutableMap.of
                                (
                                        "sinkNodeId", entity5.getLong("id"),
                                        "sinkNodeEntity", entity5.getEntityName(),
                                        "sourceNodeId", entity3.getLong("id"),
                                        "sourceNodeEntity", entity3.getEntityName(),
                                        "associationType", "AnAssociationType"
                                )
                );

        associationManager.swapAssociation(ImmutableList.of(entity2, entity3), "AnAssociationType", entity1, entity5);

        verify(databaseDelegator).
                makeValue(firstValueToBeCreated.getEntityName(), firstValueToBeCreated.getAllFields());

        verify(databaseDelegator).
                makeValue(secondValueToBeCreated.getEntityName(), secondValueToBeCreated.getAllFields());
    }

    @Test (expected = IllegalArgumentException.class)
    public void gettingTheIdsOfSinksForUserEntityAssociationsShouldFailWhenTheGivenUserIsNull()
            throws GenericEntityException
    {
        associationManager.getSinkIdsFromUser(null, "TestEntity", "favourite", false);
    }

    @Test
    public void gettingTheIdsOfSinksOfAnSpecifiedEntityNameForAUserEntitySourceReturnsTheSinksMatchingThatEntityName()
            throws GenericEntityException
    {
        final GenericValue expectedEntity1 = new MockGenericValue("TestEntity", ImmutableMap.of("id", 1L));
        final GenericValue expectedEntity2 = new MockGenericValue("TestEntity", ImmutableMap.of("id", 2L));
        
        final List<GenericValue> expectedAssociationToTestEntitySinks = ImmutableList.<GenericValue>of
                (
                        new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "associationType", "UserAssociation",
                                        "sourceNodeId", 1L,
                                        "sourceNodeEntity", "User",
                                        "sinkNodeId", expectedEntity1.get("id"),
                                        "sinkNodeEntity", expectedEntity1.getEntityName()
                                )
                        ),
                        new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "associationType", "UserAssociation",
                                        "sourceNodeId", 1L,
                                        "sourceNodeEntity", "User",
                                        "sinkNodeId", expectedEntity2.get("id"),
                                        "sinkNodeEntity", expectedEntity2.getEntityName()
                                )
                        )
                );
        
        when(databaseDelegator.findByAnd
                (
                        "UserAssociation",
                        ImmutableMap.of
                                (
                                        "sourceName", adminUser.getName(),
                                        "associationType", "favourite",
                                        "sinkNodeEntity", "TestEntity"
                                )
                )
        ).thenReturn(expectedAssociationToTestEntitySinks);
        
        final List<Long> expectedSinkIds = Lists.transform(expectedAssociationToTestEntitySinks, new Function<GenericValue, Long>()
        {
            @Override
            public Long apply(final GenericValue genericValue)
            {
                return genericValue.getLong("sinkNodeId");
            }
        });

        final Iterable<Long> actualSinkIdsForUser =
                associationManager.getSinkIdsFromUser(adminUser, "TestEntity", "favourite", false);

        assertEquals(expectedSinkIds, actualSinkIdsForUser);
    }
    
    @Test
    public void gettingTheIdsOfSinksOfAnSpecifiedEntityNameForAUserEntitySourceShouldNotReturnSinksMatchingADifferentEntityName()
            throws GenericEntityException
    {
        final GenericValue testEntity1 = new MockGenericValue("TestEntity", ImmutableMap.of("id", 1L));
        final GenericValue testEntity2 = new MockGenericValue("TestEntity", ImmutableMap.of("id", 2L));

        final List<GenericValue> associationToTestEntitySinks = ImmutableList.<GenericValue>of
                (
                        new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "associationType", "UserAssociation",
                                        "sourceNodeId", 1L,
                                        "sourceNodeEntity", "User",
                                        "sinkNodeId", testEntity1.get("id"),
                                        "sinkNodeEntity", testEntity1.getEntityName()
                                )
                        ),
                        new MockGenericValue(NODE_ASSOCIATION_ENTITY_NAME, ImmutableMap.of
                                (
                                        "associationType", "UserAssociation",
                                        "sourceNodeId", 1L,
                                        "sourceNodeEntity", "User",
                                        "sinkNodeId", testEntity2.get("id"),
                                        "sinkNodeEntity", testEntity2.getEntityName()
                                )
                        )
                );

        when(databaseDelegator.findByAnd
                (
                        "UserAssociation",
                        ImmutableMap.of
                                (
                                        "sourceName", adminUser.getName(),
                                        "associationType", "favourite",
                                        "sinkNodeEntity", "TestEntity"
                                )
                )
        ).thenReturn(associationToTestEntitySinks);

        final List<Long> sinkIdsForTestEntities = Lists.transform(associationToTestEntitySinks, new Function<GenericValue, Long>()
        {
            @Override
            public Long apply(final GenericValue genericValue)
            {
                return genericValue.getLong("sinkNodeId");
            }
        });

        final Iterable<Long> actualSinkIdsForUser =
                associationManager.getSinkIdsFromUser(adminUser, "DifferentEntity", "favourite", false);

        assertFalse(elementsEqual(sinkIdsForTestEntities, actualSinkIdsForUser));
    }

    @Test
    public void removingTheAssociationsForAUserShouldRemoveAllUserAssociationsFromTheDatabase()
            throws GenericEntityException
    {
        associationManager.removeUserAssociationsFromUser(adminUser);
        verify(databaseDelegator).removeByAnd("UserAssociation", ImmutableMap.of("sourceName", adminUser.getName()));
    }

    @Test
    public void removingTheAssociationsOfAnSpeficiedTypeForAUserShouldOnlyRemoveTheAssociationsOfThatTypeFromTheDatabase()
            throws GenericEntityException
    {
        associationManager.removeUserAssociationsFromUser(adminUser, "favourite-association-type");
        verify(databaseDelegator, only()).removeByAnd
                (
                        "UserAssociation",
                        ImmutableMap.of("sourceName", adminUser.getName(), "associationType", "favourite-association-type")
                );
    }

    @Test
    public void removingTheAssociationsOfAnSpeficiedTypeForAUserToEntitiesOfASpecifiedNameShouldOnlyRemoveTheAssociationsToTheEntitiesOfThaName()
            throws GenericEntityException
    {
        associationManager.removeUserAssociationsFromUser(adminUser, "favourite-association-type", "an-entity-name");
        verify(databaseDelegator, only()).removeByAnd
                (
                        "UserAssociation",
                        ImmutableMap.of
                                (
                                        "sourceName", adminUser.getName(), "associationType", "favourite-association-type",
                                        "sinkNodeEntity", "an-entity-name"
                                )
                );
    }

    @Test
    public void removingTheAssociationsOfAnSpeficiedTypeForAnEntityShouldRemoveTheAssociationsToAllUsersForThatEntity()
            throws GenericEntityException
    {
        final String testEntity = "test-entity";
        final GenericValue ent1 = new MockGenericValue(testEntity, ImmutableMap.of("id", 1L));

        associationManager.removeUserAssociationsFromSink(ent1, "favourite-association-type");

        verify(databaseDelegator, only()).removeByAnd
                (
                        "UserAssociation",
                        ImmutableMap.of
                                (
                                        "sinkNodeId", ent1.getLong("id"), "sinkNodeEntity", ent1.getEntityName(),
                                        "associationType", "favourite-association-type"
                                )
                );
    }

    private static class Stubs
    {
        private static class Entities
        {
            private static final MockModelEntity NODE_ASSOCIATION_ENTITY = new MockModelEntity
                    (
                            NODE_ASSOCIATION_ENTITY_NAME,
                            ImmutableList.of
                                    (
                                            "sourceNodeId", "sourceNodeEntity", "sinkNodeId", "sinkNodeEntity",
                                            "associationType", "sequence"
                                    )
                    );
            private static final ModelEntity ISSUE_ENTITY = new MockModelEntity
                    (
                            "Issue",
                            ImmutableList.<String>builder()
                                    .add("id").add("key").add("project").add("reporter").add("assignee").add("type")
                                    .add("summary").add("description").add("environment").add("priority")
                                    .add("resolution").add("status").add("created").add("updated")
                                    .add("duedate").add("resolutiondate").add("votes").add("watches")
                                    .add("timeoriginalestimate").add("timeestimate").add("timespent").add("workflowId")
                                    .add("security").add("fixfor").add("component").
                                    build()
                    );

            static ModelEntity nodeAssociation()
            {
                return NODE_ASSOCIATION_ENTITY;
            }

            static ModelEntity issue()
            {
                return ISSUE_ENTITY;
            }
        }

        private static class DatabaseDelegator
        {
            private static GenericDelegator create(final Iterable<GenericValue> entities) throws GenericEntityException
            {
                final GenericDelegator databaseDelegator = mock(GenericDelegator.class);
    
                when(databaseDelegator.makePK(eq(NODE_ASSOCIATION_ENTITY_NAME), anyMap())).
                        thenAnswer(new Answer<Object>()
                        {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable
                            {
                                return new GenericPK
                                        (
                                                databaseDelegator, Entities.nodeAssociation(),
                                                (Map<String, ?>) invocation.getArguments()[1]
                                        );
                            }
                        });
                when(databaseDelegator.makePK(eq("Issue"), anyMap())).
                        thenAnswer(new Answer<Object>()
                        {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable
                            {
                                return new GenericPK
                                        (
                                                databaseDelegator, Entities.issue(),
                                                (Map<String, ?>) invocation.getArguments()[1]
                                        );
                            }
                        });
    
                when(databaseDelegator.makeValue(anyString(), anyMap())).
                        thenAnswer(new Answer<Object>()
                        {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable
                            {
                                return new MockGenericValue((String) invocation.getArguments()[0], (Map) invocation.getArguments()[1]);
                            }
                        });

                when(databaseDelegator.findByPrimaryKey(any(GenericPK.class))).
                        thenAnswer(new Answer<Object>()
                        {
                            @Override
                            public Object answer(final InvocationOnMock invocation) throws Throwable
                            {
                                if (invocation.getArguments()[0] == null)
                                {
                                    return null;
                                }
                                if (invocation.getArguments()[0] instanceof GenericPK)
                                {
                                    return find(entities, new Predicate<GenericValue>()
                                    {
                                        @Override
                                        public boolean apply(final GenericValue genericValue)
                                        {
                                            final GenericPK firstArgument = (GenericPK) invocation.getArguments()[0];
                                            return firstArgument.get("id").equals(genericValue.getLong("id"));
                                        }
                                    }, null);
                                }
                                return null;
                            }
                        });
                return databaseDelegator;
            }
        }
    }
}
