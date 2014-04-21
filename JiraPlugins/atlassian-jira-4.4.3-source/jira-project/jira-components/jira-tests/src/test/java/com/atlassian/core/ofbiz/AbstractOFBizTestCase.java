package com.atlassian.core.ofbiz;

import org.junit.Test;
import org.junit.Before;
import com.atlassian.core.ofbiz.test.mock.MockSequenceUtil;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.MemoryHelper;
import org.ofbiz.core.entity.model.ModelEntity;

abstract public class AbstractOFBizTestCase extends ListeningTestCase
{
    static
    {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    public AbstractOFBizTestCase()
    {}


    @Before
    public void setUp() throws Exception
    {
        MemoryHelper.clearCache();
        CoreFactory.getGenericDelegator().clearAllCaches();

        final String helperName = CoreFactory.getGenericDelegator().getEntityHelperName("SequenceValueItem");
        final ModelEntity seqEntity = CoreFactory.getGenericDelegator().getModelEntity("SequenceValueItem");

        CoreFactory.getGenericDelegator().setSequencer(new MockSequenceUtil(helperName, seqEntity, "seqName", "seqId"));
    }

    // empty test to make JUnit happy
    @Test
    public void testNothing() {}
}
