package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

/**
 * Dependencies on cache flushing orders exist between the Daos and are enforced by this class
 * e.g. the {@link OfBizDirectoryDao} must be flushed before the {@link OfBizUserDao} as the {@link OfBizUserDao} queries
 * {@link OfBizDirectoryDao} for the list of directories when rebuilding its cache.
 *
 * @since v1.3.7
 */
public final class OfBizCacheFlushingManager
{

    public OfBizCacheFlushingManager(final EventPublisher eventPublisher, final OfBizUserDao ofBizUserDao,
                                     final OfBizDirectoryDao ofBizDirectoryDao, final OfBizGroupDao ofBizGroupDao,
                                     final OfBizInternalMembershipDao ofBizInternalMembershipDao,
                                     final OfBizApplicationDao ofBizApplicationDao)
    {
        final OfBizCacheFlushingManagerListener ofBizCacheFlushingManagerListener =
                new OfBizCacheFlushingManagerListener(
                        ofBizUserDao,
                        ofBizDirectoryDao,
                        ofBizGroupDao,
                        ofBizInternalMembershipDao,
                        ofBizApplicationDao
                );
        eventPublisher.register(ofBizCacheFlushingManagerListener);
    }

    public static final class OfBizCacheFlushingManagerListener
    {
        private final OfBizUserDao ofBizUserDao;
        private final OfBizDirectoryDao ofBizDirectoryDao;
        private final OfBizGroupDao ofBizGroupDao;
        private final OfBizInternalMembershipDao ofBizInternalMembershipDao;
        private final OfBizApplicationDao ofBizApplicationDao;

        private OfBizCacheFlushingManagerListener(final OfBizUserDao ofBizUserDao,
                                                  final OfBizDirectoryDao ofBizDirectoryDao,
                                                  final OfBizGroupDao ofBizGroupDao,
                                                  final OfBizInternalMembershipDao ofBizInternalMembershipDao,
                                                  final OfBizApplicationDao ofBizApplicationDao)
        {
            this.ofBizUserDao = ofBizUserDao;
            this.ofBizDirectoryDao = ofBizDirectoryDao;
            this.ofBizGroupDao = ofBizGroupDao;
            this.ofBizInternalMembershipDao = ofBizInternalMembershipDao;
            this.ofBizApplicationDao = ofBizApplicationDao;
        }

        /**
         * Listen for XMLRestoreFinishedEvents, which mean we need to clear any caches. Ensure the calls in this
         * method respect dependencies between the DAOs, e.g. the {@link OfBizDirectoryDao} must be flushed before the
         * {@link OfBizUserDao} as the {@link OfBizUserDao} queries {@link OfBizDirectoryDao} for the list of
         * directories when rebuilding its cache.
         *
         * @param event XMLRestoreFinishedEvent.
         */
        @SuppressWarnings ({ "UnusedDeclaration" })
        @EventListener
        public void onEvent(final XMLRestoreFinishedEvent event)
        {
            ofBizApplicationDao.flushCache();
            ofBizDirectoryDao.flushCache();
            ofBizUserDao.flushCache();
            ofBizGroupDao.flushCache();
            ofBizInternalMembershipDao.flushCache();
        }

    }

}
