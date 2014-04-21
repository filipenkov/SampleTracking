/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import java.util.List;

public class EditHandlerDetailsPage extends AbstractMailPage
{
    @ElementBy(name = "project")
    private SelectElement project;

    @ElementBy(name = "issuetype")
    private SelectElement issuetype;

    @ElementBy(name = "stripquotes")
    private CheckboxElement stripQuotes;

    @FindBy(name="reporterusername")
    protected WebElement reporterusername;

    @ElementBy(name="reporterusername")
    protected PageElement reporter;

    @ElementBy(cssSelector="div#reporterusername_container ~ div.error")
    protected PageElement reporterUsernameError;

    @FindBy(name="catchemail")
    protected WebElement catchemail;

    @ElementBy(cssSelector="input[name=catchemail] ~ div.error")
    protected PageElement catchemailError;

    @FindBy(name="forwardEmail")
    protected WebElement forwardEmail;

    @ElementBy(cssSelector="input[name=forwardEmail] ~ div.error")
    protected PageElement forwardEmailError;

    @ElementBy(name = "createusers")
    private CheckboxElement createUsers;

    @ElementBy(name = "notifyusers")
    private CheckboxElement notifyUsers;


    @ElementBy (id = "addButton", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement add;

    @ElementBy(name="splitregex")
    protected PageElement splitRegexpPe;

    @FindBy(name="splitregex")
    protected WebElement splitRegexpWe;


    public EditHandlerDetailsPage addWithErrors() {
        add.click();
        return pageBinder.bind(EditHandlerDetailsPage.class);
    }

    public IncomingServersPage add() {
        add.click();
        driver.waitUntilElementIsNotVisible(By.id("mailHandlerForm"));
        return pageBinder.bind(IncomingServersPage.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return add.timed().isPresent();
    }

    public EditHandlerDetailsPage setCatchemail(String email) {
        changeText(catchemail, email);
        return this;
    }

    public EditHandlerDetailsPage setReporterusername(String user) {
        changeText(reporterusername, user);
        return this;
    }

    public boolean isReporterPresent() {
        return reporter.isVisible();
    }

    public EditHandlerDetailsPage setForwardEmail(String email) {
        changeText(forwardEmail, email);
        return this;
    }

    public List<String> getFieldNames() {
        List<String> result = Lists.newArrayList();
        for(WebElement elem : driver.findElements(By.cssSelector("div.field-group label"))) {
            // dirty hack to trim the text of inner children of this element (provided that they were in the markup in a separate line
            result.add(elem.getText().split("\n")[0]);
        }
        return result;
    }

    public String getProjectKey()
    {
        return project.getSelected().value();
    }

    public String getProjectName()
    {
        return project.getSelected().text();
    }

    public EditHandlerDetailsPage setProjectByKey(String projectKey)
    {
        project.select(Options.value(projectKey));
        return this;
    }

    public String getIssueTypeName()
    {
        return issuetype.getSelected().text();
    }

    public EditHandlerDetailsPage setIssueTypeByName(String issueTypeName)
    {
        issuetype.select(Options.text(issueTypeName));
        return this;
    }

    public EditHandlerDetailsPage setCreateUsers(boolean set)
    {
        if (set) {
            createUsers.check();
        } else {
            createUsers.uncheck();
        }
        return this;
    }

    public EditHandlerDetailsPage setNotifyUsers(boolean set)
    {
        if (set) {
            notifyUsers.check();
        } else {
            notifyUsers.uncheck();
        }
        return this;
    }

    public EditHandlerDetailsPage setStripQuotes(boolean set)
    {
        if (set) {
            stripQuotes.check();
        } else {
            stripQuotes.uncheck();
        }
        return this;
    }


    @Nullable
    public String getReporterError() {
        return reporterUsernameError.timed().getText().byDefaultTimeout();
    }

    public boolean hasReporterError() {
        return reporterUsernameError.timed().isPresent().by(1000);
    }

    @Nullable
    public String getCatchEmailError() {
        return catchemailError.timed().getText().byDefaultTimeout();
    }

    public boolean hasCatchEmailError() {
        return catchemailError.timed().isPresent().by(1000);
    }

    @Nullable
    public String getForwardEmailError() {
        return forwardEmailError.timed().getText().byDefaultTimeout();
    }

    public boolean hasForwardEmailError() {
        return forwardEmailError.timed().isPresent().by(1000);
    }
    
    public EditHandlerDetailsPage setSplitRegexp(String splitRegexp) {
        changeText(this.splitRegexpWe, splitRegexp);
        return this;
    }

    public String getSplitRegexp() {
        return splitRegexpPe.timed().getValue().byDefaultTimeout();
    }

}
