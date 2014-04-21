package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.Clause;
import electric.xml.Element;

/**
 * This is here so that Terminal XML nodes can be produced from XML fragments so that we can
 * incrementally transition from SearchParameters to JiraQuery based searches. This will allow a JiraQuery
 * terminal node to be able to participate in persisting a search request via the OLD XML method.
 *
 * NOTE: eventually when all SearchParameters have been destroyed we should just save the JQL and this will
 * only be used in an upgrade task.
 *
 * @since v4.0
 * @see com.atlassian.jira.upgrade.tasks.UpgradeTask_Build604
 */
public interface ClauseXmlHandler
{
    /**
     * Produce a clause from the legacy, SearchParameter, XML storage format.
     *
     * @param el XML element
     * @return a Clause that corresponds to the search that is described by the XML, must not be null.
     */
    ConversionResult convertXmlToClause(Element el);

    /**
     * Some search parameters have values which when running through "namification" could lose precision or change the 
     * original meaning of the value. This flag indicates whether it is safe for the upgrade task to namify the values.
     *
     * @return true if is safe to namify values produced by this handler; false otherwise.
     */
    boolean isSafeToNamifyValue();

    public interface ConversionResult
    {
        /**
         * @return the clause generated from conversion or null if there was an error.
         */
        Clause getClause();

        /**
         * @return on of the {@link com.atlassian.jira.upgrade.tasks.jql.ClauseXmlHandler.ConversionResultType}'s.
         */
        ConversionResultType getResultType();

        /**
         * @param i18nHelper used to i18n the message
         * @param savedFilterName the name of the filter this result was generated for.
         * @return a message explaining what happened as a result of the conversion.
         */
        String getMessage(I18nHelper i18nHelper, String savedFilterName);
    }

    public static class FullConversionResult implements ConversionResult
    {
        private final Clause clause;

        public FullConversionResult(final Clause clause)
        {
            this.clause = clause;
        }

        public Clause getClause()
        {
            return clause;
        }

        public ConversionResultType getResultType()
        {
            return ConversionResultType.FULL_CONVERSION;
        }

        public String getMessage(I18nHelper i18nHelper, final String savedFilterName)
        {
            return null;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final FullConversionResult that = (FullConversionResult) o;

            if (!clause.equals(that.clause))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return clause.hashCode();
        }
    }

    /**
     * A way for a handler to "opt-out" of doing any conversion.
     */
    public static class NoOpConversionResult implements ConversionResult
    {
        public Clause getClause()
        {
            return null;
        }

        public ConversionResultType getResultType()
        {
            return ConversionResultType.NOOP_CONVERSION;
        }

        public String getMessage(I18nHelper i18nHelper, final String savedFilterName)
        {
            return null;
        }
    }

    public static class FailedConversionResult implements ConversionResult
    {
        private final String oldXmlFieldName;

        public FailedConversionResult(final String oldXmlFieldName)
        {
            this.oldXmlFieldName = Assertions.notNull("oldXmlFieldName", oldXmlFieldName);
        }

        public Clause getClause()
        {
            return null;
        }

        public ConversionResultType getResultType()
        {
            return ConversionResultType.FAILED_CONVERSION;
        }

        public String getMessage(final I18nHelper i18nHelper, final String savedFilterName)
        {
            return i18nHelper.getText("jira.jql.upgrade.error.converting.to.jql", savedFilterName, oldXmlFieldName);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final FailedConversionResult that = (FailedConversionResult) o;

            if (!oldXmlFieldName.equals(that.oldXmlFieldName))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return oldXmlFieldName.hashCode();
        }
    }

    public static class FailedConversionNoValuesResult implements ConversionResult
    {
        private final String oldXmlFieldName;

        public FailedConversionNoValuesResult(final String oldXmlFieldName)
        {
            this.oldXmlFieldName = Assertions.notNull("oldXmlFieldName", oldXmlFieldName);
        }

        public Clause getClause()
        {
            return null;
        }

        public ConversionResultType getResultType()
        {
            return ConversionResultType.FAILED_CONVERSION;
        }

        public String getMessage(final I18nHelper i18nHelper, final String savedFilterName)
        {
            return i18nHelper.getText("jira.jql.upgrade.error.converting.to.jql.no.values", savedFilterName, oldXmlFieldName);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final FailedConversionNoValuesResult that = (FailedConversionNoValuesResult) o;

            if (!oldXmlFieldName.equals(that.oldXmlFieldName))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return oldXmlFieldName.hashCode();
        }
    }

    public static class BestGuessConversionResult implements ConversionResult
    {
        private final Clause clause;
        private final String oldXmlFieldName;
        private final String convertedClauseName;
        FieldManager fieldManager;

        public BestGuessConversionResult(final Clause clause, final String oldXmlFieldName, final String convertedClauseName)
        {
            this.clause = Assertions.notNull("clause", clause);
            this.convertedClauseName = Assertions.notNull("convertedClauseName", convertedClauseName);
            this.oldXmlFieldName = Assertions.notNull("oldXmlFieldName", oldXmlFieldName);
        }

        public Clause getClause()
        {
            return clause;
        }

        public ConversionResultType getResultType()
        {
            return ConversionResultType.BEST_GUESS_CONVERSION;
        }

        public String getMessage(final I18nHelper i18nHelper, final String savedFilterName)
        {
            String fieldName = getFieldNameForId(i18nHelper, oldXmlFieldName);
            return i18nHelper.getText("jira.jql.upgrade.best.guess.converting.to.jql", savedFilterName, fieldName, convertedClauseName);
        }

        String getFieldNameForId(final I18nHelper i18nHelper, final String oldXmlFieldName)
        {
            final FieldManager fieldManager = getFieldManager();
            final Field field = fieldManager.getField(oldXmlFieldName);
            if (field == null)
            {
                return oldXmlFieldName;
            }
            else
            {
                return i18nHelper.getText(field.getNameKey());
            }
        }

        // For testing only
        FieldManager getFieldManager()
        {
            if (fieldManager == null)
            {
                fieldManager = ComponentManager.getComponentInstanceOfType(FieldManager.class);
            }
            return fieldManager;
        }

        // For testing only
        public void setFieldManager(FieldManager fieldManager)
        {
            this.fieldManager = fieldManager;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final BestGuessConversionResult that = (BestGuessConversionResult) o;

            if (!clause.equals(that.clause))
            {
                return false;
            }
            if (!convertedClauseName.equals(that.convertedClauseName))
            {
                return false;
            }
            if (fieldManager != null ? !fieldManager.equals(that.fieldManager) : that.fieldManager != null)
            {
                return false;
            }
            if (!oldXmlFieldName.equals(that.oldXmlFieldName))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = clause.hashCode();
            result = 31 * result + oldXmlFieldName.hashCode();
            result = 31 * result + convertedClauseName.hashCode();
            result = 31 * result + (fieldManager != null ? fieldManager.hashCode() : 0);
            return result;
        }
    }


    public enum ConversionResultType
    {
        FULL_CONVERSION,
        BEST_GUESS_CONVERSION,
        FAILED_CONVERSION,
        NOOP_CONVERSION
    }
    
}
