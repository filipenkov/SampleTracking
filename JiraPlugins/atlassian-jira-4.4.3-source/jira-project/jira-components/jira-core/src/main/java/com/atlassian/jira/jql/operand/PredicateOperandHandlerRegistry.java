package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.SecureUserTokenManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import java.util.List;

/**
 * Has the standard handlers for dealing with history predicates
 *
 * @since v4.3
 */
public class PredicateOperandHandlerRegistry
{
    private static final Logger log = Logger.getLogger(PredicateOperandHandlerRegistry.class);

    private final JqlFunctionHandlerRegistry functionRegistry;
    private final I18nHelper.BeanFactory factory;
    private  final JiraAuthenticationContext authContext;


    public PredicateOperandHandlerRegistry(final JqlFunctionHandlerRegistry functionRegistry, final I18nHelper.BeanFactory factory, final JiraAuthenticationContext authContext)
    {
        this.authContext = notNull("authContext", authContext);
        this.factory = notNull("factory", factory);
        this.functionRegistry = notNull("functionRegistry", functionRegistry);
    }

    public PredicateOperandHandler getHandler(User searcher, Operand operand)
    {
        if (operand instanceof SingleValueOperand)
        {
            return new SingleValuePredicateOperandHandler(searcher,(SingleValueOperand)operand);
        }
        else if (operand instanceof EmptyOperand)
        {
            return new EmptyPredicateOperandHandler(searcher, (EmptyOperand)operand);
        }
        else if (operand instanceof MultiValueOperand)
        {
            return new MultiValuePredicateOperandHandler(searcher,this, (MultiValueOperand)operand);
        }
        else if (operand instanceof FunctionOperand)
        {
            return new FunctionPredicateOperandHandler(searcher, (FunctionOperand)operand, functionRegistry);
        }
        else
        {
            log.warn(String.format("Unknown operand type '%s' with name '%s'", operand.getClass(), operand.getDisplayString()));
            return null;
        }

    }


    final static class SingleValuePredicateOperandHandler
            implements PredicateOperandHandler
    {
        private final SingleValueOperand singleValueOperand;
        private final User searcher;

        SingleValuePredicateOperandHandler(User searcher, SingleValueOperand singleValueOperand)
        {
            this.singleValueOperand = singleValueOperand;
            this.searcher = searcher;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            if (singleValueOperand.getLongValue() == null)
            {
                return Collections.singletonList(new QueryLiteral(singleValueOperand, singleValueOperand.getStringValue()));
            }
            else
            {
                return Collections.singletonList(new QueryLiteral(singleValueOperand, singleValueOperand.getLongValue()));
            }
        }
        
        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isList()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return false;
        }
    }

    final static class EmptyPredicateOperandHandler implements PredicateOperandHandler
    {

        private final EmptyOperand emptyOperand;
        private final User searcher;

        EmptyPredicateOperandHandler(User searcher, EmptyOperand emptyOperand)
        {
            this.searcher = searcher;
            this.emptyOperand = emptyOperand;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            return Collections.singletonList(new QueryLiteral(emptyOperand));
        }

        @Override
        public boolean isEmpty()
        {
            return true;
        }

        @Override
        public boolean isList()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return false;
        }
    }

    final static class MultiValuePredicateOperandHandler implements PredicateOperandHandler
    {
        private final PredicateOperandHandlerRegistry handlerRegistry;
        private final MultiValueOperand operand;
        private final User searcher;

        MultiValuePredicateOperandHandler(User searcher, PredicateOperandHandlerRegistry handlerRegistry, MultiValueOperand operand)
        {
            this.searcher = searcher;
            this.handlerRegistry = handlerRegistry;
            this.operand = operand;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            List<QueryLiteral> valuesList = new ArrayList<QueryLiteral>();
            for (Operand subOperand : operand.getValues())
            {
                final List<QueryLiteral> vals = handlerRegistry.getHandler(searcher, subOperand).getValues();
                if (vals != null)
                {
                    valuesList.addAll(vals);
                }
            }
            return valuesList;
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isList()
        {
            return true;
        }

        @Override
        public boolean isFunction()
        {
            return false;
        }
    }

    final static class FunctionPredicateOperandHandler implements PredicateOperandHandler
    {

        private final FunctionOperand operand;
        private final User searcher;
        private final JqlFunctionHandlerRegistry functionRegistry;

        FunctionPredicateOperandHandler(User searcher, FunctionOperand operand, JqlFunctionHandlerRegistry functionRegistry)
        {
            this.searcher = searcher;
            this.operand = operand;
            this.functionRegistry = functionRegistry;
        }

        @Override
        public List<QueryLiteral> getValues()
        {
            FunctionOperandHandler handler = functionRegistry.getOperandHandler(operand);
            return handler.getValues(new QueryCreationContextImpl(searcher), operand, null);
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isList()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return true;
        }
    }
}
