package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class TestUpgradeTask_Build403 extends MockControllerTestCase
{

    @Test
    public void testDoUpgrade() throws Exception
    {
        ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);
        constantsManager.getPriorities();
        Collection<JiraMockGenericValue> priorites = new ArrayList<JiraMockGenericValue>();
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "121212"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "ABCDEF"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "12AB12"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "1212ef"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "ab1212"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "abcdef"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "aBcDeF"), true));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "aaaaaa"), true));

        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "1212121"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "ababaz"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "ZZZZZZ"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "white"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "transparent"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "purple"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "CAFEBABE"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "AAAAAAAA"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", "1212121"), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", ""), false));
        priorites.add(new JiraMockGenericValue("Priority", EasyMap.build("statusColor", null), false));

        mockController.setReturnValue(priorites);

        mockController.replay();

        UpgradeTask_Build403 task = new UpgradeTask_Build403(constantsManager);
        task.doUpgrade(false);

        mockController.verify();

        for (JiraMockGenericValue priority : priorites)
        {
            priority.verify();
        }
    }


    @Test
    public void testDoUpgradeEmpty() throws Exception
    {
        ConstantsManager constantsManager = mockController.getMock(ConstantsManager.class);
        constantsManager.getPriorities();
        Collection<JiraMockGenericValue> priorites = new ArrayList<JiraMockGenericValue>();

        mockController.setReturnValue(priorites);

        mockController.replay();

        UpgradeTask_Build403 task = new UpgradeTask_Build403(constantsManager);
        task.doUpgrade(false);

        mockController.verify();

        for (JiraMockGenericValue priority : priorites)
        {
            priority.verify();
        }
    }


    class JiraMockGenericValue extends MockGenericValue
    {
        private boolean stored = false;
        private boolean updated = false;
        private final boolean shouldUpdated;

        public JiraMockGenericValue(String entityName, Map fields, boolean shouldUpdated)
        {
            super(entityName, fields);
            this.shouldUpdated = shouldUpdated;
        }

        public void store() throws GenericEntityException
        {
            assertTrue(shouldUpdated);
            stored = true;
        }

        public void setString(String s, String s1)
        {
            assertTrue(shouldUpdated);
            super.setString(s, s1);
            updated = true;
        }

        public void verify()
        {
            assertEquals(shouldUpdated, updated);
            assertEquals(shouldUpdated, stored);
        }
    }
}
