package com.atlassian.jira.issue.fields.config.persistence;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestCachedFieldConfigSchemePersister extends MockControllerTestCase
{
    @Test
    public void testGetConfigSchemeForFieldConfig() throws Exception
    {
        final Long fieldConfigId = 10L;
        final Long schemeId = 200L;

        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.findByAnd("FieldConfigSchemeIssueType", ImmutableMap.of("fieldconfiguration", fieldConfigId));
        mockController.setReturnValue(ImmutableList.of(new MockGenericValue("FieldConfigSchemeIssueType", ImmutableMap.of("fieldconfigscheme", schemeId))));

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        fieldConfig.getId();
        mockController.setReturnValue(fieldConfigId, 3);

        final FieldConfigPersister fieldConfigPersister = mockController.getMock(FieldConfigPersister.class);
        final ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);

        final FieldConfigScheme configScheme = mockController.getMock(FieldConfigScheme.class);

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);
        final CachedFieldConfigSchemePersister persister = new CachedFieldConfigSchemePersister(delegator, constantsManager, fieldConfigPersister, null)
        {
            @Override
            public FieldConfigScheme getFieldConfigScheme(final Long configSchemeId)
            {
                called.incrementAndGet();
                return configScheme;
            }
        };

        persister.getConfigSchemeForFieldConfig(fieldConfig);
        persister.getConfigSchemeForFieldConfig(fieldConfig);
        
        assertEquals(1, called.get());
        mockController.verify();
    }
}
