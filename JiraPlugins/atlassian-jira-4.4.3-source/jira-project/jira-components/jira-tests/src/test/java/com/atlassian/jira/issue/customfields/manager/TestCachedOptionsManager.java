package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static com.atlassian.jira.util.collect.MapBuilder.newBuilder;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since v4.0
 */
public class TestCachedOptionsManager extends MockControllerTestCase
{
    private OfBizDelegator ofBizDelegator;
    private CollectionReorderer<Option> collectionReorderer;
    private FieldConfigManager fieldConfigManager;

    @Before
    public void setUp() throws Exception
    {
        ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        collectionReorderer = mockController.getMock(CollectionReorderer.class);
        fieldConfigManager = mockController.getMock(FieldConfigManager.class);
    }

    @Test
    public void testGetAllOptions() throws Exception
    {
        final Option option = new MockOption(null, null, null, null, null, 10L);
        ofBizDelegator.findAll(EasyMock.eq("CustomFieldOption"), (List<String>) EasyMock.anyObject());
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        final OptionsManager optionsManager = new CachedOptionsManager(ofBizDelegator, collectionReorderer, fieldConfigManager, null)
        {
            @Override
            List<Option> convertGVsToOptions(final List<GenericValue> optionGvs)
            {
                called.set(true);
                return CollectionBuilder.newBuilder(option).asList();
            }
        };

        List<Option> result = optionsManager.getAllOptions();
        assertTrue(called.get());
        assertTrue(result.contains(option));
        assertEquals(1, result.size());

        result = optionsManager.getAllOptions();

        assertTrue(called.get());
        assertTrue(result.contains(option));
        assertEquals(1, result.size());
        mockController.verify();
    }

    @Test
    public void testFindByOptionValue() throws Exception
    {
        final Option option1 = new MockOption(null, null, null, "value", null, 10L);
        mockController.replay();
        final AtomicInteger called = new AtomicInteger(0);
        final OptionsManager manager = new CachedOptionsManager(ofBizDelegator, collectionReorderer, fieldConfigManager, null)
        {
            @Override
            public List<Option> getAllOptions()
            {
                called.getAndIncrement();
                return CollectionBuilder.<Option> newBuilder(option1, new MockOption(null, null, null, "DiffValue", null, 10L)).asList();
            }
        };

        List<Option> result = manager.findByOptionValue("VALUE");
        assertEquals(1, result.size());
        assertTrue(result.contains(option1));

        result = manager.findByOptionValue("Value");
        assertEquals(1, result.size());
        assertTrue(result.contains(option1));

        assertEquals(1, called.get());

        mockController.verify();
    }

    @Test
    public void testRemoveCustomFieldConfigOptions() throws Exception
    {
        ofBizDelegator.removeByAnd("CustomFieldOption", newBuilder("customfieldconfig", 1L).toMap());
        mockController.setReturnValue(1L);

        mockController.replay();

        final OptionsManager manager = new CachedOptionsManager(ofBizDelegator, collectionReorderer, fieldConfigManager, null);
        final FieldConfigImpl fieldConfig = new FieldConfigImpl(1L, "", "", null, "");

        manager.removeCustomFieldConfigOptions(fieldConfig);

        mockController.verify();
    }
}
