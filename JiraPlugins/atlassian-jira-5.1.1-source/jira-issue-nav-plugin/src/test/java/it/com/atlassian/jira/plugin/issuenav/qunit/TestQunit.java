package it.com.atlassian.jira.plugin.issuenav.qunit;

import com.atlassian.aui.test.runner.QUnitPageObjectsHelper;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;

public class TestQunit {
    private JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);

    private final File outdir;

    public TestQunit() {
        String location = System.getProperty("jira.qunit.testoutput.location");
        if (StringUtils.isEmpty(location)) {
            System.err.println("Writing result XML to tmp, jira.qunit.testoutput.location not defined");
            location = System.getProperty("java.io.tmpdir");
        }

        outdir = new File(location);
    }

    @Test
    public void runJustOurTest() throws Exception {
        QUnitPageObjectsHelper helper = new QUnitPageObjectsHelper(outdir, product.getPageBinder());
        helper.runTests(QUnitPageObjectsHelper.suiteNameContains("com.atlassian.jira.jira-issue-nav-plugin"));
    }

}
