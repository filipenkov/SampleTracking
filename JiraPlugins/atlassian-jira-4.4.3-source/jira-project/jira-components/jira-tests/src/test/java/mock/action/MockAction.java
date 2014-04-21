/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package mock.action;

import com.atlassian.jira.util.ErrorCollection;
import webwork.action.ActionSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MockAction extends ActionSupport implements ErrorCollection
{
    String foo;

    public String getFoo()
    {
        return foo;
    }

    public void setFoo(String foo)
    {
        this.foo = foo;
    }

    public Collection getFlushedErrorMessages()
    {
        Collection errors = getErrorMessages();
        errorMessages = new ArrayList();
        return errors;
    }

    public void addErrorCollection(ErrorCollection errors)
    {
        addErrorMessages(errors.getErrorMessages());
        addErrors(errors.getErrors());
    }

    public void addErrorMessages(Collection errorMessages)
    {
        getErrorMessages().addAll(errorMessages);
    }

    public void addErrors(Map errors)
    {
        getErrors().putAll(errors);
    }

    public boolean hasAnyErrors()
    {
        return invalidInput();
    }

    @Override
    public void addError(String field, String message, Reason reason)
    {
    }

    @Override
    public void addErrorMessage(String message, Reason reason)
    {
    }

    @Override
    public void addReasons(Set<Reason> reasons)
    {
    }

    @Override
    public void addReason(Reason reason)
    {
    }

    @Override
    public void setReasons(Set<Reason> reasons)
    {
    }

    @Override
    public Set<Reason> getReasons()
    {
        return null;
    }
}
