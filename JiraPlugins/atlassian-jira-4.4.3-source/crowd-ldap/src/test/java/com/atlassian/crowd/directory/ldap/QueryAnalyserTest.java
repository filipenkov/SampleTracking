package com.atlassian.crowd.directory.ldap;

import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.PropertyUtils;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import junit.framework.TestCase;

public class QueryAnalyserTest extends TestCase
{
    public void testNullUserQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).returningAtMost(100);

        assertTrue(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertTrue(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testNullGroupQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).returningAtMost(100);

        assertTrue(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertTrue(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testLdapUserTermQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.DISPLAY_NAME).containing("blah")).returningAtMost(100);

        assertTrue(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testInternalUserTermQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(PropertyUtils.ofTypeString("custom")).containing("blah")).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertTrue(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testLdapUserBooleanQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(
                Combine.allOf(
                        Restriction.on(UserTermKeys.DISPLAY_NAME).containing("blah"),
                        Restriction.on(UserTermKeys.EMAIL).containing("diblah")
                )).returningAtMost(100);

        assertTrue(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testInternalUserBooleanQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(
                Combine.allOf(
                        Restriction.on(PropertyUtils.ofTypeString("phone")).containing("94811111"),
                        Restriction.on(PropertyUtils.ofTypeString("gender")).containing("mail")
                )).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertTrue(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testMixedUserBooleanQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(
                Combine.allOf(
                        Restriction.on(UserTermKeys.DISPLAY_NAME).containing("blah"),
                        Restriction.on(PropertyUtils.ofTypeString("gender")).containing("mail")
                )).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }
    
    public void testLdapGroupTermQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(Restriction.on(GroupTermKeys.NAME).containing("blah")).returningAtMost(100);

        assertTrue(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testInternalGroupTermQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(Restriction.on(PropertyUtils.ofTypeString("custom")).containing("blah")).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertTrue(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testLdapGroupBooleanQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(
                Combine.allOf(
                        Restriction.on(GroupTermKeys.NAME).containing("blah"),
                        Restriction.on(GroupTermKeys.ACTIVE).containing(true)
                )).returningAtMost(100);

        assertTrue(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testInternalGroupBooleanQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(
                Combine.allOf(
                        Restriction.on(PropertyUtils.ofTypeString("phone")).containing("94811111"),
                        Restriction.on(PropertyUtils.ofTypeString("gender")).containing("mail")
                )).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertTrue(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testMixedGroupBooleanQuery()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(
                Combine.allOf(
                        Restriction.on(GroupTermKeys.NAME).containing("blah"),
                        Restriction.on(PropertyUtils.ofTypeString("gender")).containing("mail")
                )).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }

    public void testUnknownEntity()
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.token()).with(Restriction.on(PropertyUtils.ofTypeString("custom")).containing("blah")).returningAtMost(100);

        assertFalse(QueryAnalyser.isQueryOnLdapFieldsOnly(query));
        assertFalse(QueryAnalyser.isQueryOnInternalFieldsOnly(query));
    }
}
