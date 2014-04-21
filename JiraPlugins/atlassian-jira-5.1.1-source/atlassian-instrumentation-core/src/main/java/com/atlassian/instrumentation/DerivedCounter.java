package com.atlassian.instrumentation;

/**
 * A DerivedCounter is a marker interface for a Gauge that should be treated as a Counter
 * in terms of rate of change recording but that allows is value to decrease.
 *
 * For example the rate of change in disk space usage could be represented by a DerivedCounter, because disk space
 * usage can logically go up and down.
 */
public interface DerivedCounter extends Gauge
{
}
