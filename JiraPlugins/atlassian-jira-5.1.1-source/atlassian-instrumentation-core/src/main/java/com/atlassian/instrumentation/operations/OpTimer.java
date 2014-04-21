package com.atlassian.instrumentation.operations;

/**
 * OpTimer is an interface to represent the "timing" of a particular computing operation.
 * <p/>
 * The construction of an OpTimer object sets the timer running.  There is no "start" method.
 * <p/>
 * Once the end() method has been called, the OpTimer must not be called again.  It is a one short object.
 *
 * @since v4.0
 */
public interface OpTimer
{
    /**
     * The name of the timer.  This should be the same name as returned on the OpSnapshot objects created by this
     * interface.
     *
     * @return the name of the operation being timed
     */
    String getName();

    /**
     * This will return a snap shot of how long the timer has been running for.  This can be called many times. If end()
     * has not been called, then OpSnapshot returned reflects how long the timer has been running for. If end() has been
     * called, then it MUST return the same value as the end() method returned.
     *
     * @return a new OpSnapshot object will the invocation count = 1, the resultSetSize of Double.NAN and the
     *         milliseconds set to how long this timer has been running for.
     */
    OpSnapshot snapshot();

    /**
     * Call this in a finally{} block or at the end of the operation with the time taken by the operation
     *
     * @param timeInMillis Time taken by the operation
     * @return a new OpSnapshot representing this operation timing
     */
    OpSnapshot endWithTime(long timeInMillis);

    /**
     * Call this in a finally{} block or at the end of the operation to stop the clock
     *
     * @param resultSetSize the size of the operations result.
     * @return a new OpSnapshot representing this operation timing
     */
    OpSnapshot end(long resultSetSize);

    /**
     * Call this in a finally{} block or at the end of the operation to stop the clock
     * <p/>
     * The result set size will be set to Double.NAN.
     *
     * @return a new OpSnapshot representing this operation timing
     */
    OpSnapshot end();

    /**
     * Call this in a finally{} block or at the end of the operation to stop the clock
     *
     * @param heisenburgResultSetCalculator a class to calculate the time
     * @return a new OpSnapshot representing this operation timing
     */
    OpSnapshot end(HeisenburgResultSetCalculator heisenburgResultSetCalculator);

    /**
     * This interface can be provided to {@link OpTimerFactory} are creation time to have a call back
     * when {@link #end} is called
     */
    public interface OnEndCallback {
        /**
         * This callback method is called when the end() method has been called and the OpSnapshot created.
         *
         * @param opSnapshot the OpSnapshot that was created by the end() method
         */
        void onEndCalled(final OpSnapshot opSnapshot);
    }


    /**
     * This interface can be used to avoid HEISENBERG effects when trying to time how long an operation takes. It MAY
     * take some time to calculate how large a result set is and hence you MAY NOT want to include that time inside your
     * operation timing.  So you can provide and instance of this interface and the calculation will be done AFTER the
     * timing has been calculated.
     * <p/>
     * OR another design option is that you consider the overall timing of an operation to INCLUDE the profiling cost
     * and hence you would not use this interface;
     */
    public interface HeisenburgResultSetCalculator
    {
        public long calculate();
    }
}
