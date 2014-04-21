package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.plugin.jql.function.CascadeOptionFunction;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Element;
import electric.xml.Elements;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates a {@link com.atlassian.query.clause.TerminalClause} that represents a cascade select custom field.
 *
 * Cascade Select is split across two different {@link com.atlassian.jira.issue.search.parameters.lucene.StringParameter}s.
 * This XML handler first checks if the element is the parent option, and then walks the tree to find the child option if there is one.
 *
 * If the element is the child option, null is returned.
 *
 * @since v4.0
 */
public class CascadeSelectParameterClauseXmlHandler implements ClauseXmlHandler
{
    private static final Logger log = Logger.getLogger(CascadeSelectParameterClauseXmlHandler.class);
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

    public CascadeSelectParameterClauseXmlHandler(final JqlSelectOptionsUtil jqlSelectOptionsUtil)
    {
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    }

    public ConversionResult convertXmlToClause(final Element el)
    {
        final String xmlFieldId = el.getName();
        final String clauseName = UpgradeTask_Build604.DocumentConstantToClauseNameResolver.getClauseName(xmlFieldId);

         if (clauseName == null)
         {
             log.warn("Trying to generate a clause for field with id '" + xmlFieldId + "' and no corresponding clause name could be found.");
             return new FailedConversionResult(xmlFieldId);
         }

        if (!isParent(el))
        {
            return new NoOpConversionResult();
        }

        Element child = getChild(el);

        if (child != null)
        {
            final Option childOption = getOptionFromElement(child);
            final Option parentOption = getOptionFromElement(el);

            if (childOption == null || parentOption == null)
            {
                return new FailedConversionNoValuesResult(xmlFieldId);
            }
            else
            {
                return new FullConversionResult(new TerminalClauseImpl(clauseName, Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, parentOption.getOptionId().toString(), childOption.getOptionId().toString())));
            }
        }
        else
        {
            final Option parentOption = getOptionFromElement(el);

            if (parentOption == null)
            {
                return new FailedConversionNoValuesResult(xmlFieldId);
            }
            else
            {
                return new FullConversionResult(new TerminalClauseImpl(clauseName, Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, parentOption.getOptionId().toString())));
            }
        }
    }

    public boolean isSafeToNamifyValue()
    {
        return false;
    }

    private Option getOptionFromElement(Element el)
    {
        Long id = parseLong(el);
        return jqlSelectOptionsUtil.getOptionById(id);
    }

    private long parseLong(final Element child)
    {
        return Long.parseLong(child.getAttribute("value"));
    }

    Element getParentElement(Element el)
    {
        try
        {
            return el.getParentElement();
        }
        catch (ClassCastException ignored)
        {
            return null;
        }
    }

    private Element getChild(final Element el)
    {
        if (getParentElement(el) != null && getParentElement(getParentElement(el)) != null)
        {
            Element root = getParentElement(getParentElement(el));
            final Elements elements = root.getElements();

            while (elements.hasMoreElements())
            {
                Element paramElement = elements.next();
                final NodeList valueElements = paramElement.getElementsByTagName(el.getName());
                for (int i = 0; i < valueElements.getLength(); ++i)
                {
                    Element valueElement = (Element)valueElements.item(0);
                    if (!isParent(valueElement))
                    {
                        return valueElement;
                    }
                }
            }
        }
        return null;
    }

    private boolean isParent(Element el)
    {
        String name = el.getAttribute("name");
        return !name.endsWith(":1");
    }
}