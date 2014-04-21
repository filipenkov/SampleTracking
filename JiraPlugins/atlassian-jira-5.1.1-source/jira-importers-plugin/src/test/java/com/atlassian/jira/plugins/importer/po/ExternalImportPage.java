package com.atlassian.jira.plugins.importer.po;

import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.po.common.AbstractImporterPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.List;

public class ExternalImportPage extends AbstractImporterPage {
    @ElementBy(id="importers")
    private PageElement importersDiv;

    @Override
    public String getUrl() {
        return "/secure/admin/views/ExternalImport1.jspa";
    }

    public List<String> getImportersOrder() {
        return Immutables.transformThenCopyToList(
                driver.findElement(By.id("importers")).findElements(By.cssSelector("div.importer")), new Function<WebElement, String>() {
            @Override
            public String apply(@Nullable WebElement input) {
                return input.getAttribute("title");
            }
        });
    }

    @Override
    public TimedCondition isAt() {
        return importersDiv.timed().isVisible();
    }
}
