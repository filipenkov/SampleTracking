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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractEditMailServerPage extends AbstractMailPage
{
    @ElementBy (name = "name")
    protected PageElement name;

    @ElementBy (name = "serverName")
    protected PageElement hostname;

    @ElementBy (name = "protocol")
    protected SelectElement protocol;

    @ElementBy (name = "port")
    protected PageElement port;

    @ElementBy (name = "timeout")
    protected PageElement timeout;

    @ElementBy (name = "username")
    protected PageElement username;

    @ElementBy (name = "tlsRequired")
    protected CheckboxElement tlsRequired;

    @ElementBy (cssSelector = "input[name=password] ~ div.fieldDescription")
    private PageElement passwordDescription;

    @ElementBy (name = "password")
    protected PageElement password;

    @ElementBy (name = "serviceProvider")
    protected SelectElement serviceProvider;

    @ElementBy (name = "from")
    protected PageElement from;

    @ElementBy (cssSelector = "input[name=from] ~ div.error")
    protected PageElement fromError;

    @ElementBy (name = "prefix")
    protected PageElement prefix;

    @ElementBy (name = "jndiLocation")
    protected PageElement jndiLocation;

    @ElementBy (cssSelector = "input[name=jndiLocation] ~ div.error")
    protected PageElement jndiLocationError;


    @ElementBy (name = "changePassword")
    private CheckboxElement changePassword;

    @ElementBy (cssSelector = "input[name=changePassword] ~ .fieldDescription")
    private PageElement changePasswordDescription;

    @ElementBy (id = "test_connection")
    protected PageElement testButton;

    @ElementBy (id = "add_submit")
    protected PageElement addButton;

    @ElementBy (id = "update_submit")
    protected PageElement updateButton;

    public AbstractEditMailServerPage setName(@Nonnull String name)
    {
        this.name.clear();
        this.name.type(name);
        return this;
    }

    public AbstractEditMailServerPage setHostName(@Nonnull String hostname)
    {
        this.hostname.clear();
        this.hostname.type(hostname);
        return this;
    }

    public String getHostName()
    {
        return hostname.getValue();
    }

    public AbstractEditMailServerPage setUsername(@Nonnull String value)
    {
        username.clear();
        username.type(value);
        driver.executeScript("jQuery('input[name=username]').change()");
        return this;
    }

    public String getUsername()
    {
        return username.getValue();
    }

    public AbstractEditMailServerPage setPassword(@Nonnull String value)
    {
        password.clear();
        password.type(value);
        return this;
    }

    public String getPassword()
    {
        return password.getValue();
    }

    public AbstractEditMailServerPage setProtocol(@Nonnull String value)
    {
        protocol.select(Options.value(value));
        return this;
    }

    public String getProtocol()
    {
        return protocol.getSelected().value();
    }

    public AbstractEditMailServerPage setPort(@Nonnull String value)
    {
        port.clear();
        port.type(value);
        return this;
    }

    public String getPort()
    {
        return port.getValue();
    }

    public AbstractEditMailServerPage setTimeout(@Nonnull String value)
    {
        timeout.clear();
        timeout.type(value);
        return this;
    }

    public String getTimeout()
    {
        return timeout.getValue();
    }

    public AbstractEditMailServerPage setServiceProvider(@Nonnull String value)
    {
        serviceProvider.select(Options.value(value));
        return this;
    }

    public AbstractEditMailServerPage setServiceProviderByName(@Nonnull String name)
    {
        serviceProvider.select(Options.text(name));
        return this;
    }

    public String getServiceProvider()
    {
        return serviceProvider.getSelected().value();
    }

    public AbstractEditMailServerPage setTlsRequired(boolean required)
    {
        if (required)
        {
            tlsRequired.check();
        }
        else
        {
            tlsRequired.uncheck();
        }
        return this;
    }

    public boolean isTlsRequired()
    {
        return tlsRequired.isSelected();
    }

    public boolean isChangePasswordVisible()
    {
        return changePassword.isVisible();
    }

    public boolean isChangePasswordEnabled()
    {
        return changePassword.isEnabled();
    }

    public boolean isChangePasswordDescriptionVisible()
    {
        return changePasswordDescription.isVisible();
    }

    public String getChangePasswordDescription()
    {
        return changePasswordDescription.getText();
    }

    public boolean isChangePassword()
    {
        return changePassword.isSelected();
    }

    public AbstractEditMailServerPage setChangePassword(boolean change)
    {
        if (changePassword.isSelected() != change)
        {
            changePassword.click();
        }
        return this;
    }

    public boolean isPasswordVisible()
    {
        return password.isVisible();
    }

    public String getPasswordDescription()
    {
        return passwordDescription.getText();
    }

    public AbstractEditMailServerPage setFrom(@Nonnull String from) {
        this.from.clear();
        this.from.type(from);
        return this;
    }

    public boolean hasFromError() {
        return fromError.isPresent() && fromError.isVisible();
    }

    public String getFromError() {
        return fromError.getText();
    }

    public AbstractEditMailServerPage setEmailPrefix(@Nonnull String value) {
        prefix.clear();
        prefix.type(value);
        return this;
    }

    public String getEmailPrefix() {
        return prefix.getValue();
    }

    public AbstractEditMailServerPage setJndiLocation(@Nonnull String location) {
        jndiLocation.clear();
        jndiLocation.type(location);
        return this;
    }

    public String getJndiLocation() {
        return jndiLocation.getValue();
    }

    public String getJndiError()
    {
        return jndiLocationError.getText();
    }


    public boolean isJndiLocationVisible() {
        return jndiLocation.isPresent() && jndiLocation.isVisible();
    }

    public boolean testSucceeded() {
        final List<WebElement> messages = driver.findElements(By.cssSelector("#verifyMessages div.aui-message"));

        return messages.size() == 1 && messages.get(0).getAttribute("class").contains("success");
    }
}
