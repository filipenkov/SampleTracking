package com.atlassian.jira.util.log;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.jira.local.ListeningTestCase;

/**
 * Test for NShotLogger and OneShotLogger
 *
 * @since v3.13
 */
public class TestNShotLogger extends ListeningTestCase
{
    private CallCountingLogger callCountingLogger;
    private static final String IGNORE_THIS_LOG_MSG = "You can ignore these log messages.  There are generated ON PURPOSE by " + TestNShotLogger.class;
    private static final Exception IGNORE_THIS_THROWABLE = new Exception(IGNORE_THIS_LOG_MSG);

    @Test
    public void testConstruction() throws Exception
    {
        try
        {
            final NShotLogger logger = new NShotLogger(null, 1);
            fail("Should have barfed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            final NShotLogger logger = new NShotLogger(new CallCountingLogger(), 0);
            fail("Should have barfed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            final NShotLogger logger = new NShotLogger(new CallCountingLogger(), -1);
            fail("Should have barfed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    interface LogMethodClosure
    {
        public void performLogging(NShotLogger logger);
    }

    LogMethodClosure DEBUG = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.debug(IGNORE_THIS_LOG_MSG);
        }
    };
    LogMethodClosure DEBUG_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.debug(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };
    LogMethodClosure ERROR = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.error(IGNORE_THIS_LOG_MSG);
        }
    };
    LogMethodClosure ERROR_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.error(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };
    LogMethodClosure INFO = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.info(IGNORE_THIS_LOG_MSG);
        }
    };
    LogMethodClosure INFO_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.info(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };
    LogMethodClosure WARN = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.warn(IGNORE_THIS_LOG_MSG);
        }
    };
    LogMethodClosure WARN_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.warn(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };
    LogMethodClosure FATAL = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.fatal(IGNORE_THIS_LOG_MSG);
        }
    };
    LogMethodClosure FATAL_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.fatal(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };

    @Test
    public void testCallCountOnceInfo() throws Exception
    {
        final LogMethodClosure[] methods = new LogMethodClosure[] { DEBUG, DEBUG_THROWABLE, INFO, INFO_THROWABLE, FATAL, FATAL_THROWABLE, WARN, WARN_THROWABLE };
        for (final LogMethodClosure method : methods)
        {
            NShotLogger logger = createNewNShotLogger(1);
            _testCallCountOnce(logger, method);

            logger = createNewOneShotLogger();
            _testCallCountOnce(logger, method);
        }
    }

    public void _testCallCountOnce(final NShotLogger logger, final LogMethodClosure closure) throws Exception
    {
        assertHasOutputNTimes(0);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
    }

    @Test
    public void testCallCountMany() throws Exception
    {
        final LogMethodClosure[] methods = new LogMethodClosure[] { DEBUG, DEBUG_THROWABLE, INFO, INFO_THROWABLE, FATAL, FATAL_THROWABLE, WARN, WARN_THROWABLE };
        for (final LogMethodClosure method : methods)
        {
            final NShotLogger logger = createNewNShotLogger(5);
            _testCallCountMany(logger, method);
        }
    }

    public void _testCallCountMany(final NShotLogger logger, final LogMethodClosure closure) throws Exception
    {
        assertHasOutputNTimes(0);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
        closure.performLogging(logger);
        assertHasOutputNTimes(2);
        closure.performLogging(logger);
        assertHasOutputNTimes(3);
        closure.performLogging(logger);
        assertHasOutputNTimes(4);
        closure.performLogging(logger);
        assertHasOutputNTimes(5);
        closure.performLogging(logger);
        assertHasOutputNTimes(5);
        closure.performLogging(logger);
        assertHasOutputNTimes(5);
    }

    @Test
    public void testRightUnderlyingMethodsCalled()
    {
        final NShotLogger logger = createNewNShotLogger(100);

        logger.debug(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.debugCount.get());
        logger.debug(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.debugCount.get());

        logger.error(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.errorCount.get());
        logger.error(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.errorCount.get());

        logger.info(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.infoCount.get());
        logger.info(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.infoCount.get());

        logger.fatal(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.fatalCount.get());
        logger.fatal(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.fatalCount.get());

        logger.warn(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.warnCount.get());
        logger.warn(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.warnCount.get());

        assertEquals(10, callCountingLogger.callCount.get());
        assertEquals(2, callCountingLogger.debugCount.get());
        assertEquals(2, callCountingLogger.errorCount.get());
        assertEquals(2, callCountingLogger.infoCount.get());
        assertEquals(2, callCountingLogger.fatalCount.get());
        assertEquals(2, callCountingLogger.warnCount.get());

    }

    private void assertHasOutputNTimes(final long expectedCallCount)
    {
        assertEquals(expectedCallCount, callCountingLogger.callCount.get());
    }

    private NShotLogger createNewNShotLogger(final int maxTimes)
    {
        callCountingLogger = new CallCountingLogger();
        return new NShotLogger(callCountingLogger, maxTimes);
    }

    private NShotLogger createNewOneShotLogger()
    {
        callCountingLogger = new CallCountingLogger();
        return new OneShotLogger(callCountingLogger);
    }

    private static class CallCountingLogger extends Logger
    {

        private final AtomicLong callCount;
        private final AtomicLong debugCount;
        private final AtomicLong errorCount;
        private final AtomicLong infoCount;
        private final AtomicLong fatalCount;
        private final AtomicLong warnCount;
        private static final Logger log = Logger.getLogger(CallCountingLogger.class);

        public CallCountingLogger()
        {
            super("CallCountingLogger");
            callCount = new AtomicLong(0);
            debugCount = new AtomicLong(0);
            errorCount = new AtomicLong(0);
            infoCount = new AtomicLong(0);
            fatalCount = new AtomicLong(0);
            warnCount = new AtomicLong(0);
        }

        @Override
        public void debug(final Object o)
        {
            callCount.incrementAndGet();
            debugCount.incrementAndGet();
            log.debug(o);
        }

        @Override
        public void debug(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            debugCount.incrementAndGet();
            log.debug(o, throwable);
        }

        @Override
        public void error(final Object o)
        {
            callCount.incrementAndGet();
            errorCount.incrementAndGet();
            log.error(o);
        }

        @Override
        public void error(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            errorCount.incrementAndGet();
            log.error(o, throwable);
        }

        @Override
        public void fatal(final Object o)
        {
            callCount.incrementAndGet();
            fatalCount.incrementAndGet();
            log.fatal(o);
        }

        @Override
        public void fatal(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            fatalCount.incrementAndGet();
            log.fatal(o, throwable);
        }

        @Override
        public void info(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            infoCount.incrementAndGet();
            log.info(o, throwable);
        }

        @Override
        public void info(final Object o)
        {
            callCount.incrementAndGet();
            infoCount.incrementAndGet();
            log.info(o);
        }

        @Override
        public void warn(final Object o)
        {
            callCount.incrementAndGet();
            warnCount.incrementAndGet();
            log.warn(o);
        }

        @Override
        public void warn(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            warnCount.incrementAndGet();
            log.warn(o, throwable);
        }

        @Override
        public synchronized void addAppender(final Appender appender)
        {
            log.addAppender(appender);
        }

        @Override
        public void assertLog(final boolean b, final String s)
        {
            log.assertLog(b, s);
        }

        @Override
        public void callAppenders(final LoggingEvent event)
        {
            log.callAppenders(event);
        }

        @Override
        public boolean getAdditivity()
        {
            return log.getAdditivity();
        }

        @Override
        public synchronized Enumeration getAllAppenders()
        {
            return log.getAllAppenders();
        }

        @Override
        public synchronized Appender getAppender(final String s)
        {
            return log.getAppender(s);
        }

        @Override
        public Level getEffectiveLevel()
        {
            return log.getEffectiveLevel();
        }

        @Override
        public Priority getChainedPriority()
        {
            return log.getChainedPriority();
        }

        @Override
        public LoggerRepository getHierarchy()
        {
            return log.getHierarchy();
        }

        @Override
        public LoggerRepository getLoggerRepository()
        {
            return log.getLoggerRepository();
        }

        @Override
        public ResourceBundle getResourceBundle()
        {
            return log.getResourceBundle();
        }

        @Override
        public boolean isAttached(final Appender appender)
        {
            return log.isAttached(appender);
        }

        @Override
        public boolean isDebugEnabled()
        {
            return log.isDebugEnabled();
        }

        @Override
        public boolean isEnabledFor(final Priority priority)
        {
            return log.isEnabledFor(priority);
        }

        @Override
        public boolean isInfoEnabled()
        {
            return log.isInfoEnabled();
        }

        @Override
        public void l7dlog(final Priority priority, final String s, final Throwable throwable)
        {
            log.l7dlog(priority, s, throwable);
        }

        @Override
        public void l7dlog(final Priority priority, final String s, final Object[] objects, final Throwable throwable)
        {
            log.l7dlog(priority, s, objects, throwable);
        }

        @Override
        public synchronized void removeAllAppenders()
        {
            log.removeAllAppenders();
        }

        @Override
        public synchronized void removeAppender(final Appender appender)
        {
            log.removeAppender(appender);
        }

        @Override
        public synchronized void removeAppender(final String s)
        {
            log.removeAppender(s);
        }

        @Override
        public void setAdditivity(final boolean b)
        {
            log.setAdditivity(b);
        }

        @Override
        public void setLevel(final Level level)
        {
            log.setLevel(level);
        }

        @Override
        public void setPriority(final Priority priority)
        {
            log.setPriority(priority);
        }

        @Override
        public void setResourceBundle(final ResourceBundle resourceBundle)
        {
            log.setResourceBundle(resourceBundle);
        }
    }
}
