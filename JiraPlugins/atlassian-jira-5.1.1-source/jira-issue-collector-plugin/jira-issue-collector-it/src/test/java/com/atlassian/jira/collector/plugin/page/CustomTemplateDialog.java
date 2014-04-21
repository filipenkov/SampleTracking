package com.atlassian.jira.collector.plugin.page;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.core.Is.is;

public class CustomTemplateDialog
{
    final Map<String, PageElement> fieldMap = new HashMap<String, PageElement>();

    @ElementBy (className = "custom-fields")
    private PageElement customFieldsPicker;

    @ElementBy (cssSelector = ".collector-preview .dialog-title")
    private PageElement dialogTitle;

    @ElementBy (cssSelector = ".collector-preview .title-input")
    private PageElement titleInput;

    public CustomTemplateDialog()
    {
    }

    @Init
    public void init()
    {
        waitUntil(customFieldsPicker.timed().isVisible(), is(true));
        for (PageElement li : customFieldsPicker.findAll(By.tagName("li")))
        {
            String fieldId = li.getAttribute("data-field-id");
            fieldMap.put(fieldId, li.find(By.className("user-form-picker-field")));
        }
    }

    public CustomTemplateDialog addField(String fieldId)
    {
        PageElement fieldButton = fieldMap.get(fieldId);
        fieldButton.click();
        waitUntil(fieldButton.find(By.className("icon")).timed().hasClass("icon-tick"), is(true));
        return this;
    }
    
    public CustomTemplateDialog title(String title)
    {
        dialogTitle.click();
        waitUntil(titleInput.timed().isVisible(), is(true));
        
        titleInput.clear().type(title).type(Keys.ENTER);
        waitUntil(dialogTitle.timed().isVisible(), is(true));
        return this;
    }

    public CustomTemplateDialog removeField(String fieldId)
    {
        PageElement fieldButton = fieldMap.get(fieldId);
        fieldButton.click();
        waitUntil(fieldButton.find(By.className("icon")).timed().hasClass("icon-add12"), is(true));
        return this;
    }
}
