package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfWorkflow extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    String createWorkflow()
    {
        String workflowId = "WK_" + System.currentTimeMillis();
        navigation.gotoAdminSection("workflows");
        tester.setFormElement("newWorkflowName", workflowId);
        tester.setFormElement("description", "My description");

        return workflowId;
    }

    public void testWorkflows() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck(
                    "Add Workflow",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            createWorkflow();
                        }
                    },
                    new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck(
                    "Delete Workflow",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("del_" + workflowId);
                        }
                    },
                    new XsrfCheck.FormSubmission("Delete")),
                new XsrfCheck(
                    "Copy Workflow",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("copy_" + workflowId);
                        }
                    },
                    new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                    "Edit Workflow",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("edit_live_" + workflowId);
                            tester.setFormElement("description", "My new description");
                        }
                    },
                    new XsrfCheck.FormSubmission("Update"))
        ).run(funcTestHelperFactory);
    }

    private void addTransition()
    {
        String workflowId = createWorkflow();
        tester.submit("Add");

        tester.clickLink("steps_live_" + workflowId);
        tester.clickLink("add_trans_1");

        tester.setFormElement("transitionName", "My transition");
        tester.setFormElement("description", "My description");
    }


    public void testWorkflowSteps() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck(
                    "Add New Step",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("steps_live_" + workflowId);
                            tester.setFormElement("stepName", "My step");
                        }
                    },
                    new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                    "Delete Step",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("steps_live_" + workflowId);
                            tester.setFormElement("stepName", "My step");
                            tester.submit("Add");

                            tester.clickLink("delete_step_2");
                        }
                    },
                    new XsrfCheck.FormSubmission("Delete")),
                new XsrfCheck(
                    "Edit Step",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("steps_live_" + workflowId);
                            tester.clickLink("edit_step_1");
                            tester.setFormElement("stepName", "My new Step name");
                        }
                    },
                    new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                    "Add Transition",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addTransition();
                        }
                    },
                    new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                    "Delete Transition from list",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addTransition();
                            tester.submit("Add");

                            tester.clickLink("del_trans_1");
                            tester.selectOption("transitionIds", "My transition");
                        }
                    },
                    new XsrfCheck.FormSubmission("Delete")),
                new XsrfCheck(
                    "Edit Transition",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addTransition();
                            tester.submit("Add");

                            tester.clickLinkWithText("My transition");
                            tester.clickLink("edit_transition");
                        }
                    },
                    new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                    "Delete this Transition",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addTransition();
                            tester.submit("Add");

                            tester.clickLinkWithText("My transition");
                            tester.clickLink("delete_transition");
                        }
                    },
                    new XsrfCheck.FormSubmission("Delete"))
        ).run(funcTestHelperFactory);
    }

    private void addCondition()
    {
        addTransition();
        tester.submit("Add");

        tester.clickLinkWithText("My transition");
        tester.clickLink("view_all_trans");
        tester.clickLinkWithText("Conditions");
        tester.clickLinkWithText("Add");

        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-condition");
        tester.submit("Add");
    }

    private void addValidator()
    {
        addTransition();
        tester.submit("Add");

        tester.clickLinkWithText("My transition");
        tester.clickLink("view_all_trans");
        tester.clickLinkWithText("Validators");
        tester.clickLinkWithText("Add");
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-validator");
        tester.submit("Add");
    }

    private void addFunction()
    {
        addTransition();
        tester.submit("Add");
        tester.clickLinkWithText("My transition");
        tester.clickLink("view_all_trans");
        tester.clickLinkWithText("Functions");
        tester.clickLinkWithText("Add");
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:assigntocurrentuser-function");
    }

    public void testWorkflowTransition() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck(
                    "Add Condition",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addCondition();
                        }
                    },
                    new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                    "Delete Condition",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addCondition();
                            tester.submit("Add");
                        }
                    },
                    new XsrfCheck.XPathLinkSubmission("//div[@class='single-leaf']/a[contains(text(), 'Delete')]")),
                new XsrfCheck(
                    "Edit Condition",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addCondition();
                            tester.submit("Add");

                            XPathLocator locator = new XPathLocator(getTester(), "//div[@class='single-leaf']/a[contains(text(), 'Edit')]");
                            final Node node = locator.getNode();
                            tester.gotoPage(((Attr) node.getAttributes().getNamedItem("href")).getValue());
                        }
                    },
                    new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                    "Switch group condition to OR",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            addCondition();
                            tester.submit("Add");

                            tester.clickLinkWithText("Add");

                            tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-condition");
                            tester.submit("Add");
                            tester.submit("Add");
                        }
                    },
                    new XsrfCheck.XPathLinkSubmission("//div[@class='operator']/a[contains(text(), 'Switch to OR')]")),
                new XsrfCheck(
                        "Add Validator",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addValidator();
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                    new XsrfCheck(
                        "Delete Validator",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addValidator();
                                tester.submit("Add");
                            }
                        },
                        new XsrfCheck.XPathLinkSubmission("//div[@class='single-leaf']/a[contains(text(), 'Delete')]")),
                    new XsrfCheck(
                        "Edit Validator",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addValidator();
                                tester.submit("Add");

                                XPathLocator locator = new XPathLocator(getTester(), "//div[@class='single-leaf']/a[contains(text(), 'Edit')]");
                                final Node node = locator.getNode();
                                tester.gotoPage(((Attr) node.getAttributes().getNamedItem("href")).getValue());
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                //TODO: AbstractAddWorkflowTransitionDescriptorParams.doDefault() calls doExecute() :(
                //JRADEV-213
//                new XsrfCheck(
//                        "Add Function",
//                        new XsrfCheck.Setup()
//                        {
//                            public void setup()
//                            {
//                                addFunction();
//                            }
//                        },
//                        new XsrfCheck.FormSubmission("Add")),
                    new XsrfCheck(
                        "Delete Function",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFunction();
                                tester.submit("Add");
                            }
                        },
                        new XsrfCheck.XPathLinkSubmission("//div[@class='highlighted-leaf']/a[contains(text(), 'Delete')]")),
                    new XsrfCheck(
                        "Move Function Up",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFunction();
                                tester.submit("Add");
                            }
                        },
                        new XsrfCheck.XPathLinkSubmission("//div[@class='highlighted-leaf']/a[contains(text(), 'Move Up')]")),
                    new XsrfCheck(
                        "Move Function Down",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFunction();
                                tester.submit("Add");
                            }
                        },
                        new XsrfCheck.XPathLinkSubmission("//div[@class='highlighted-leaf']/a[contains(text(), 'Move Down')]")),
                    new XsrfCheck(
                        "Edit Generic Function",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFunction();
                                tester.submit("Add");

                                XPathLocator locator = new XPathLocator(getTester(), "//div[@class='leaf']/a[contains(text(), 'Edit')]");
                                final Node node = locator.getNode();
                                tester.gotoPage(((Attr) node.getAttributes().getNamedItem("href")).getValue());
                            }
                        },
                        new XsrfCheck.FormSubmission("Update"))


        ).run(funcTestHelperFactory);
    }

    public void testWorkflowProperties() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck(
                    "Add New Property",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("steps_live_" + workflowId);
                            tester.clickLinkWithText("View Properties");
                            
                            tester.setFormElement("attributeKey", "key");
                            tester.setFormElement("attributeValue", "value");
                        }
                    },
                    new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                    "Delete Property",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            String workflowId = createWorkflow();
                            tester.submit("Add");

                            tester.clickLink("steps_live_" + workflowId);
                            tester.clickLinkWithText("View Properties");

                            tester.setFormElement("attributeKey", "myKey");
                            tester.setFormElement("attributeValue", "value");
                            tester.submit("Add");
                        }
                    },
                    new XsrfCheck.LinkWithIdSubmission("del_meta_myKey"))
        ).run(funcTestHelperFactory);
    }

    String createStatus()
    {
        String statusId = "ST_" + System.currentTimeMillis();
        navigation.gotoPage("secure/admin/workflows/ViewStatuses.jspa");
        tester.setFormElement("name", statusId);
        tester.setFormElement("description", "My description");

        return statusId;
    }

    public void testWorkflowStatuses() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck(
                    "Add New Status",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            createStatus();
                        }
                    },
                    new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck(
                    "Delete Status",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            createStatus();
                            tester.submit("Add");
                            tester.clickLinkWithText("Delete");
                        }
                    },
                    new XsrfCheck.FormSubmission("Delete")),
            new XsrfCheck(
                    "Edit Status",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            createStatus();
                            tester.submit("Add");
                            tester.clickLink("edit_10003");
                        }
                    },
                    new XsrfCheck.FormSubmission("Update")),
            new XsrfCheck(
                    "Translate Status",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            createStatus();

                            tester.clickLink("view-translation");
                            tester.setFormElement("jira.translation.Status.1.name", "My Name");
                            tester.setFormElement("jira.translation.Status.1.desc", "My description");
                        }
                    },
                    new XsrfCheck.FormSubmission("update"))
        ).run(funcTestHelperFactory);
    }

    public void testXmlImport() throws Exception
    {
        navigation.gotoAdminSection("workflows");
        tester.clickLink("xml_jira");
        final String xml = tester.getDialog().getResponseText();

        new XsrfTestSuite(
            new XsrfCheck(
                    "Import XML",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            navigation.gotoAdminSection("workflows");
                            tester.clickLinkWithText("import a workflow from XML");
                            tester.setFormElement("name", "myName");
                            tester.setFormElement("workflowXML", xml);
                        }
                    },
                    new XsrfCheck.FormSubmission("Import"))).run(funcTestHelperFactory);
    }

    public void testCreateDraftAndPublishWorkflow() throws Exception
    {
        administration.restoreData("TestDraftWorkflow.xml");

        new XsrfTestSuite(
            new XsrfCheck(
                    "Create Draft",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            navigation.gotoAdminSection("workflows");
                        }
                    },
                    new XsrfCheck.LinkWithIdSubmission("createDraft_Workflow1")),
            new XsrfCheck(
                    "Publish Draft",
                    new XsrfCheck.Setup()
                    {
                        public void setup()
                        {
                            navigation.gotoAdminSection("workflows");
                            tester.clickLink("del_Workflow1");
                            tester.submit("Delete");
                            tester.clickLink("createDraft_Workflow1");
                            tester.clickLink("publish_draft_workflow");
                            tester.checkCheckbox("enableBackup", "false");
                        }
                    },
                    new XsrfCheck.FormSubmission("Publish"))).run(funcTestHelperFactory);
    }
}