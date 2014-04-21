/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 1, 2004
 * Time: 5:22:03 PM
 */
package com.atlassian.jira.util;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.datetime.LocalDateFactory;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.LockObtainFailedException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple utility class for our common Lucene usage methods.
 */
public class LuceneUtils
{
    private static final Logger log = Logger.getLogger(LuceneUtils.class);

    private static final String LOCK_FILENAME_PREFIX = "Lock@";

    private static final DateFormatter dateFormatter = new DateFormatter();

    public static Directory getDirectory(final String path)
    {
        try
        {
            //return new SimpleFSDirectory(new File(path), new UtilConcurrentLockFactory());
            return FSDirectory.open(new File(path), new UtilConcurrentLockFactory()); // returns an NIOFSDirectory on *nix SimpleFSDirectory on Windows
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public static IndexReader getIndexReader(final String path) throws IndexException
    {
        try
        {
            return IndexReader.open(getDirectory(path), true);
        }
        catch (final IOException e)
        {
            log.error("Problem with path " + path + ": " + e.getMessage(), e);
            throw new IndexException("Problem with path " + path + ": " + e.getMessage(), e);
        }
    }

    public static IndexWriter getIndexWriter(final String path, final boolean create, final Analyzer analyzer) throws IndexException
    {
        try
        {
            createDirRobust(path);

            final IndexWriter indexWriter = new IndexWriter(getDirectory(path), analyzer, create, IndexWriter.MaxFieldLength.LIMITED);
            indexWriter.setUseCompoundFile(true);
            return indexWriter;
        }
        catch (final IOException e)
        {
            log.error("Problem with path " + path + ": " + e.getMessage(), e);
            throw new IndexException("Problem with path " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Create a directory (robustly) or throw appropriate Exception
     *
     * @param path Lucene index directory path
     * @throws IOException if cannot create directory, write to the directory, or not a directory
     */
    private static void createDirRobust(final String path) throws IOException
    {
        final File potentialPath = new File(path);
        if (!potentialPath.exists())
        {
            log.warn("Directory " + path + " does not exist - perhaps it was deleted?  Creating..");

            final boolean created = potentialPath.mkdirs();
            if (!created)
            {
                log.warn("Directory " + path + " could not be created.  Aborting index creation");
                throw new IOException("Could not create directory: " + path);
            }
        }
        if (!potentialPath.isDirectory())
        {
            log.warn("File " + path + " is not a directory.  Cannot create index");
            throw new IOException("File " + path + " is not a directory.  Cannot create index");
        }
        if (!potentialPath.canWrite())
        {
            log.warn("Dir " + path + " is not writable.  Cannot create index");
            throw new IOException("Dir " + path + " is not writable.  Cannot create index");
        }
    }

    /**
     * Given a {@link Collection} of paths that represent index directories checks if there are any existing
     * Lucene lock files for the passed paths. This method returns a {@link Collection} of file paths of any existing
     * Lucene lock files. If no lock files are found an empty collection is returned.
     * <p/>
     * A common usage of this methdo would be:
     * <pre>
     * Collection existingLockFilepaths = LuceneUtils.getStaleLockPaths(indexManager.getAllIndexPaths());
     * </pre>
     * </p>
     *
     * @param indexDirectoryPaths collection of index directory paths
     * @return collection of file paths of any existing Lucene lock files
     */
    public static Collection<String> getStaleLockPaths(final Collection<String> indexDirectoryPaths)
    {
        // A collection to which we will add all found lock file paths (if any)
        final Collection<String> existingLockFilepaths = new ArrayList<String>();

        try
        {
            // Get a path for each index directory
            if (indexDirectoryPaths != null)
            {
                for (final String indexDirectoryPath : indexDirectoryPaths)
                {
                    existingLockFilepaths.addAll(getLocks(indexDirectoryPath));
                }
            }
        }
        catch (final IOException e)
        {
            log.error("While trying to check for stale lock files: " + e.getMessage());
        }

        return existingLockFilepaths;
    }

    public static String localDateToString(LocalDate localDate)
    {
        return LocalDateFactory.toIsoBasic(localDate);
    }

    public static LocalDate stringToLocalDate(final String indexValue)
    {
        return LocalDateFactory.fromIsoBasicFormat(indexValue);
    }

    /**
     * Turns a given date-time (point in time) value into a String suitable for storing and searching in Lucene.
     * <p>
     * The date-time is stored with second precision using GMT eg 2011-05-05 10:59:39.000 EST (GMT+10) would return "20110505005939".
     * Note that the lexigraphical ordering of such Strings are in line with the ordering of the represented timestamps.
     *
     * @param date the date to be converted
     * @return a string in format <code>YYYYMMDDHHMMSS</code> in GMT timezone.
     */
    public static String dateToString(final Date date)
    {
        return dateFormatter.dateToString(date, Resolution.SECOND);
    }

    public static Date stringToDate(final String s)
    {
        if ((s != null) && (s.trim().length() > 0))
        {
            return dateFormatter.stringToDate(s);
        }
        return new Date();
    }

    private static Collection<String> getLocks(final String path) throws IOException
    {
        final Collection<String> locks = new ArrayList<String>();

        Directory dir = null;
        try
        {
            dir = getDirectory(path);
            // Check write lock
            final org.apache.lucene.store.Lock lock = dir.makeLock(IndexWriter.WRITE_LOCK_NAME);
            if (lock.isLocked())
            {
                locks.add(getLockFilepath(lock));
            }
        }
        finally
        {
            if (dir != null)
            {
                dir.close();
            }
        }

        return locks;
    }

    private static String getLockFilepath(final org.apache.lucene.store.Lock lock)
    {
        if (lock == null)
        {
            return "";
        }

        String filePath = lock.toString();
        if ((filePath != null) && filePath.startsWith(LOCK_FILENAME_PREFIX))
        {
            filePath = filePath.substring(LOCK_FILENAME_PREFIX.length());
        }

        return filePath;
    }

    static final class UtilConcurrentLockFactory extends LockFactory
    {
        private final ConcurrentMap<String, UtilConcurrentLock> map = CopyOnWriteMap.newHashMap();

        @Override
        public void clearLock(final String lockName) throws IOException
        {
            map.remove(lockName);
        }

        @Override
        public org.apache.lucene.store.Lock makeLock(final String lockName)
        {
            final UtilConcurrentLock result = new UtilConcurrentLock();
            try
            {
                return result;
            }
            finally
            {
                map.put(lockName, result);
            }
        }
    }

    static final class UtilConcurrentLock extends org.apache.lucene.store.Lock
    {
        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public boolean isLocked()
        {
            return lock.isLocked();
        }

        @Override
        public boolean obtain()
        {
            return lock.tryLock();
        }

        @Override
        public void release()
        {
            lock.unlock();
        }

        @Override
        public boolean obtain(final long timeout) throws LockObtainFailedException
        {
            try
            {
                return lock.tryLock(timeout, TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException e)
            {
                throw new LockObtainFailedException(e.toString());
            }
        }
    }

    /**
     * do not ctor
     */
    private LuceneUtils()
    {}

    @Immutable
    static class DateFormatter
    {
        private final DateTimeFormatter year;
        private final DateTimeFormatter month;
        private final DateTimeFormatter day;
        private final DateTimeFormatter hour;
        private final DateTimeFormatter minute;
        private final DateTimeFormatter second;
        private final DateTimeFormatter millisecond;

        DateFormatter()
        {
            final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
            {
                @Override
                public DateTimeFormatter toFormatter()
                {
                    return super.toFormatter().withZone(DateTimeZone.UTC);
                }
            };
            ISOChronology chronology = ISOChronology.getInstance();
            year = builder.appendYear(4, 4).toFormatter().withChronology(chronology);
            month = builder.appendMonthOfYear(2).toFormatter().withChronology(chronology);
            day = builder.appendDayOfMonth(2).toFormatter().withChronology(chronology);
            hour = builder.appendHourOfDay(2).toFormatter().withChronology(chronology);
            minute = builder.appendMinuteOfHour(2).toFormatter().withChronology(chronology);
            second = builder.appendSecondOfMinute(2).toFormatter().withChronology(chronology);
            millisecond = builder.appendMillisOfSecond(3).toFormatter().withChronology(chronology);
        }

        /**
         * Converts a string produced by <code>timeToString</code> or
         * <code>dateToString</code> back to a time, represented as a
         * Date object.
         *
         * @param dateString the date string to be converted
         * @return the parsed time as a Date object
         * @throws DateParsingException if <code>dateString</code> is not in the
         *  expected format
         */
        public Date stringToDate(final String dateString) throws DateParsingException
        {
            return stringToDateTime(dateString.trim()).toDate();
        }

        /**
         * Converts a string produced by <code>timeToString</code> or
         * <code>dateToString</code> back to a time, represented as a
         * Date object.
         *
         * @param dateString the date string to be converted
         * @return the parsed time as a Date object
         * @throws DateParsingException if <code>dateString</code> is not in the
         *  expected format
         */
        public DateTime stringToDateTime(final String dateString) throws DateParsingException
        {
            switch (dateString.length())
            {
                case 4:
                    return year.parseDateTime(dateString);
                case 6:
                    return month.parseDateTime(dateString);
                case 8:
                    return day.parseDateTime(dateString);
                case 10:
                    return hour.parseDateTime(dateString);
                case 12:
                    return minute.parseDateTime(dateString);
                case 14:
                    return second.parseDateTime(dateString);
                case 17:
                    return millisecond.parseDateTime(dateString);

                default:
                    throw new DateParsingException(dateString);
            }
        }

        /**
         * Converts a Date to a string suitable for indexing.
         *
         * @param date the date to be converted
         * @param resolution the desired resolution, see
         *  {@link Resolution}
         * @return a string in format <code>yyyyMMddHHmmssSSS</code> or shorter,
         *  depending on <code>resolution</code>; using GMT as timezone
         */
        public String dateToString(final Date date, final Resolution resolution)
        {
            return timeToString(date.getTime(), resolution);
        }

        /**
         * Converts a millisecond time to a string suitable for indexing.
         *
         * @param date the date expressed as milliseconds since January 1, 1970, 00:00:00 GMT
         * @param resolution the desired resolution, see
         *  {@link Resolution}
         * @return a string in format <code>yyyyMMddHHmmssSSS</code> or shorter,
         *  depending on <code>resolution</code>; using GMT as timezone
         */
        public String timeToString(final long date, final Resolution resolution)
        {
            switch (resolution)
            {
                case YEAR:
                    return year.print(date);
                case MONTH:
                    return month.print(date);
                case DAY:
                    return day.print(date);
                case HOUR:
                    return hour.print(date);
                case MINUTE:
                    return minute.print(date);
                case SECOND:
                    return second.print(date);
                case MILLISECOND:
                    return millisecond.print(date);
            }
            throw new IllegalArgumentException("unknown resolution " + resolution);
        }
    }

    static class DateParsingException extends RuntimeException
    {
        public DateParsingException(final String dateString)
        {
            super("Input is not valid date string: " + dateString);
        }
    }

    private static enum Resolution
    {
        YEAR,
        MONTH,
        DAY,
        HOUR,
        MINUTE,
        SECOND,
        MILLISECOND
    }
}
