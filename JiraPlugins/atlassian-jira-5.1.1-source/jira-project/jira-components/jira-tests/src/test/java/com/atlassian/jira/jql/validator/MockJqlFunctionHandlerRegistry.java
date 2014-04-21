package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple mock interface for the OperandRegistry that uses a map to serve the handlers.
 *
 * @since v4.0
 */
public class MockJqlFunctionHandlerRegistry implements JqlFunctionHandlerRegistry
{
    private final Map<String, FunctionOperandHandler> handlers;

    public MockJqlFunctionHandlerRegistry(Map<String, FunctionOperandHandler> handlers)
    {
        this.handlers = handlers;
    }

    public MockJqlFunctionHandlerRegistry()
    {
        this (new HashMap<String, FunctionOperandHandler>());
    }

    public boolean registerFunctionHandler(final JqlFunction jqlFunction)
    {
        this.handlers.put(CaseFolding.foldString(jqlFunction.getFunctionName()), new FunctionOperandHandler(jqlFunction));
        return true;
    }

    public void unregisterFunctionHandler(final JqlFunction function)
    {
        handlers.remove(CaseFolding.foldString(function.getFunctionName()));
    }

    public FunctionOperandHandler getOperandHandler(final FunctionOperand operand)
    {
        return handlers.get(operand.getName());
    }

    public List<String> getAllFunctionNames()
    {
        return Collections.emptyList();
    }
}
