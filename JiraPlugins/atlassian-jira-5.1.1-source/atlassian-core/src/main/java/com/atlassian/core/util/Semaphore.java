/*
 * Atlassian Source Code Template.
 * User: Mike Cannon-Brookes
 * Date: 31/07/2002
 * Time: 19:33:40
 * CVS Revision: $Revision: 1.1 $
 * Last CVS Commit: $Date: 2002/12/02 05:26:32 $
 * Author of last CVS Commit: $Author: mike $
 *
 * Originally written by Doug Lea and released into the public domain.
 * This may be used for any purposes whatsoever without acknowledgment.
 * Thanks for the assistance and support of Sun Microsystems Labs,
 * and everyone contributing, testing, and using this code.
 *
 * History:
 * Date       Who                What
 * 11Jun1998  dl               Create public version
 *  5Aug1998  dl               replaced int counters with longs
 * 24Aug1999  dl               release(n): screen arguments
 * 31Jul2002  mcb              borrowed from Jive 2.5.4(license above says we can do this)
 */
package com.atlassian.core.util;


/**
 * Base class for counting semaphores.
 * Conceptually, a semaphore maintains a set of permits.
 * Each acquire() blocks if necessary
 * until a permit is available, and then takes it.
 * Each release adds a permit. However, no actual permit objects
 * are used; the Semaphore just keeps a count of the number
 * available and acts accordingly.
 * <p>
 * A semaphore initialized to 1 can serve as a mutual exclusion
 * lock.
 * <p>
 * Different implementation subclasses may provide different
 * ordering guarantees (or lack thereof) surrounding which
 * threads will be resumed upon a signal.
 * <p>
 * The default implementation makes NO
 * guarantees about the order in which threads will
 * acquire permits. It is often faster than other implementations.
 */
public class Semaphore
{

    /** current number of available permits **/
    protected long permits;

    /**
     * Create a Semaphore with the given initial number of permits.
     * Using a seed of one makes the semaphore act as a mutual exclusion lock.
     * Negative seeds are also allowed, in which case no acquires will proceed
     * until the number of releases has pushed the number of permits past 0.
     */
    public Semaphore(long initialPermits)
    {
        permits = initialPermits;
    }


    /**
     * Wait until a permit is available, and take one
     */
    public void acquire() throws InterruptedException
    {
        if (Thread.interrupted())
        {
            throw new InterruptedException();
        }

        synchronized (this)
        {
            try
            {
                while (permits <= 0)
                {
                    wait();
                }
                --permits;
            }
            catch (InterruptedException ex)
            {
                notify();
                throw ex;
            }
        }
    }

    /**
     * Wait at most msecs millisconds for a permit.
     */
    public boolean attempt(long msecs) throws InterruptedException
    {
        if (Thread.interrupted())
        {
            throw new InterruptedException();
        }

        synchronized (this)
        {
            if (permits > 0)
            {
                --permits;
                return true;
            }
            else if (msecs <= 0)
            {
                return false;
            }
            else
            {
                try
                {
                    long startTime = System.currentTimeMillis();
                    long waitTime = msecs;

                    for (; ;)
                    {
                        wait(waitTime);
                        if (permits > 0)
                        {
                            --permits;
                            return true;
                        }
                        else
                        {
                            waitTime = msecs - (System.currentTimeMillis() - startTime);
                            if (waitTime <= 0)
                            {
                                return false;
                            }
                        }
                    }
                }
                catch (InterruptedException ex)
                {
                    notify();
                    throw ex;
                }
            }
        }
    }

    /**
     * Release a permit
     */
    public synchronized void release()
    {
        ++permits;
        notify();
    }


    /**
     * Release N permits. <code>release(n)</code> is
     * equivalent in effect to:
     * <pre>
     *   for (int i = 0; i < n; ++i) release();
     * </pre>
     * <p>
     * But may be more efficient in some semaphore implementations.
     *
     * @exception IllegalArgumentException if n is negative.
     */
    public synchronized void release(long n)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("Negative argument");
        }

        permits += n;

        for (long i = 0; i < n; ++i)
        {
            notify();
        }
    }

    /**
     * Return the current number of available permits.
     * Returns an accurate, but possibly unstable value,
     * that may change immediately after returning.
     */
    public synchronized long permits()
    {
        return permits;
    }
}
