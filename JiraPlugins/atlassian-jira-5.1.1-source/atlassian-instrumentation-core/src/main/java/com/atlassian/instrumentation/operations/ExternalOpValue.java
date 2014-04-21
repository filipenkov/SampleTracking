package com.atlassian.instrumentation.operations;

/**
 * ExternalOpValue can be used to read values from "external" places in the system and
 * provide them as {@link OpSnapshot} values.
 *
 * @see com.atlassian.instrumentation.operations.ExternalOpInstrument
 */
public interface ExternalOpValue
{
    /**
     * @return the external snapshot value
     */
    OpSnapshot getSnapshot();
}
