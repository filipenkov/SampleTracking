package com.atlassian.config.wizard;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 */
public class WizardTestCase extends TestCase
{
    private int saves;

    public void setUp()
    {
        saves = 0;
    }

    public void testSimpleWizardSetup() throws Exception
    {
        SetupWizard wizard = new SetupWizard();


        TestSetupStep step1 = new TestSetupStep("Step 1", "Setp1-action");
        TestSetupStep step2 = new TestSetupStep("Step 2", "Setp2-action");
        wizard.addStep(step1);
        wizard.addStep(step2);
        wizard.setSaveStrategy(new SaveStrategy()
        {
            public void save(SetupWizard setupWizard)
            {
                saves++;
            }
        });
        wizard.start();

        assertNotNull(wizard.getCurrentStep());
        Assert.assertEquals("Step 1", wizard.getCurrentStep().getName());
        wizard.next();

        assertNotNull(wizard.getCurrentStep());
        Assert.assertEquals("Step 2", wizard.getCurrentStep().getName());

        wizard.finish();

        assertEquals(1, step1.getStart());
        assertEquals(1, step1.getNext());
        assertEquals(3, saves); //two steps and finish
    }

    public class TestSetupStep extends DefaultSetupStep
    {
        int next = 0;
        int start = 0;

        public TestSetupStep(String name, String actionName)
        {
            super();
            setName(name);
            setActionName(actionName);
        }

        public void onNext()
        {
            next++;
        }

        public void onStart()
        {
            start++;
        }

        public int getNext()
        {
            return next;
        }

        public int getStart()
        {
            return start;
        }
    }
}
