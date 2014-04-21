package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.event.api.EventPublisher;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * DirectoryCacheFactory that returns DbCachingRemoteDirectoryCache instance
 * wrapped with TransactionInterceptor.
 */
public class TransactionalDirectoryCacheFactory extends DirectoryCacheFactoryImpl
{
    private final TransactionInterceptor transactionInterceptor;

    public TransactionalDirectoryCacheFactory(DirectoryDao directoryDao,
                                              SynchronisationStatusManager synchronisationStatusManager,
                                              EventPublisher eventPublisher,
                                              TransactionInterceptor transactionInterceptor)
    {
        super(directoryDao, synchronisationStatusManager, eventPublisher);
        this.transactionInterceptor = transactionInterceptor;
    }

    public DirectoryCacheChangeOperations createDirectoryCacheChangeOperations(RemoteDirectory remoteDirectory, InternalRemoteDirectory internalDirectory)
    {
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(new TransactionAttributeSourceAdvisor(transactionInterceptor));
        proxyFactory.setInterfaces(new Class[] { DirectoryCacheChangeOperations.class });
        proxyFactory.setTarget(super.createDirectoryCacheChangeOperations(remoteDirectory, internalDirectory));
        return (DirectoryCacheChangeOperations) proxyFactory.getProxy();
    }
}
