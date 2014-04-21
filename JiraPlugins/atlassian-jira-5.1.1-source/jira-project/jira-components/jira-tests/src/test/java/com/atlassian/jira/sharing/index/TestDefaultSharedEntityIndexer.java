package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.sharing.MockSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.UnimplementedSharedEntityAccessor;
import com.atlassian.jira.sharing.index.DefaultSharedEntityIndexer.EntityDocumentFactory;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.MockUser;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestDefaultSharedEntityIndexer extends MockControllerTestCase
{
    private static final String TEST_DEFAULT_SHARED_ENTITY_INDEXER = "TestDefaultSharedEntityIndexer";
    private final TypeDescriptor<?> typeDescriptor = SharedEntity.TypeDescriptor.Factory.get().create(TEST_DEFAULT_SHARED_ENTITY_INDEXER);

    private final User user = new MockUser("testClear");
    private final SharedEntity entity = new MockSharedEntity(1L, typeDescriptor, user, SharePermissions.PRIVATE)
    {
        @Override
        public String getName()
        {
            return "indexMeName";
        }

        @Override
        public String getDescription()
        {
            return "indexMeDescription";
        }
    };

    @Before
    public void setUp()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Test
    public void testGetSearcher() throws Exception
    {
        final SharedEntityAccessor.Factory accessorFactory = getMock(SharedEntityAccessor.Factory.class);
        expect(accessorFactory.getSharedEntityAccessor(typeDescriptor)).andReturn(new UnimplementedSharedEntityAccessor());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.getSearcher(typeDescriptor);
    }

    @Test
    public void testClear() throws Exception
    {
        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.clear(typeDescriptor);
    }

    @Test
    public void testGetIndexPaths() throws Exception
    {
        final SharedEntityIndexer indexer = mockController.instantiate(DefaultSharedEntityIndexer.class);
        assertSame(Collections.EMPTY_LIST, indexer.getAllIndexPaths());
    }

    @Test
    public void testOptimize() throws Exception
    {
        @SuppressWarnings("unchecked")
        final DirectoryFactory directoryFactory = getMock(DirectoryFactory.class);
        expect(directoryFactory.get(typeDescriptor)).andReturn(new RAMDirectory());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.optimize(typeDescriptor);
    }

    @Test
    public void testShutdown() throws Exception
    {
        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.shutdown(typeDescriptor);
    }

    @Test
    public void testIndex() throws Exception
    {
        final EntityDocumentFactory documentFactory = getMock(EntityDocumentFactory.class);
        expect(documentFactory.get(entity)).andReturn(new DefaultSharedEntityIndexer.EntityDocument()
        {
            public Document getDocument()
            {
                return new Document();
            }

            public Term getIdentifyingTerm()
            {
                return new Term("test", "23");
            }

            public TypeDescriptor<?> getType()
            {
                return typeDescriptor;
            }
        });

        @SuppressWarnings("unchecked")
        final DirectoryFactory directoryFactory = getMock(DirectoryFactory.class);
        expect(directoryFactory.get(typeDescriptor)).andReturn(new RAMDirectory());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.index(entity);
    }

    @Test
    public void testDeindex() throws Exception
    {
        final EntityDocumentFactory documentFactory = getMock(EntityDocumentFactory.class);
        expect(documentFactory.get(entity)).andReturn(new DefaultSharedEntityIndexer.EntityDocument()
        {
            public Document getDocument()
            {
                return new Document();
            }

            public Term getIdentifyingTerm()
            {
                return new Term("test", "23");
            }

            public TypeDescriptor<?> getType()
            {
                return typeDescriptor;
            }
        });

        @SuppressWarnings("unchecked")
        final DirectoryFactory directoryFactory = getMock(DirectoryFactory.class);
        expect(directoryFactory.get(typeDescriptor)).andReturn(new RAMDirectory());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.deIndex(entity).await();
    }

    /**
     * Test DefaultEntityDocumentFactory inner class
     */
    @Test
    public void testDefaultEntityDocumentFactoryGet()
    {
        final Document expectedDocument = new Document();
        final SharedEntityDocumentFactory sharedEntityDocumentFactory = getMock(SharedEntityDocumentFactory.class);
        expect(sharedEntityDocumentFactory.create(entity)).andReturn(expectedDocument);

        final ShareTypeFactory expectedShareTypeFactory = getMock(ShareTypeFactory.class);

        replay();

        final DefaultSharedEntityIndexer.DefaultEntityDocumentFactory documentFactoryUnderTest = new DefaultSharedEntityIndexer.DefaultEntityDocumentFactory(
            expectedShareTypeFactory)
        {
            @Override
            SharedEntityDocumentFactory createDocumentFactory(final ShareTypeFactory shareTypeFactory)
            {
                assertSame(expectedShareTypeFactory, shareTypeFactory);
                return sharedEntityDocumentFactory;
            }
        };

        final DefaultSharedEntityIndexer.EntityDocument entityDocument = documentFactoryUnderTest.get(entity);
        assertNotNull(entityDocument);
        final Term term = entityDocument.getIdentifyingTerm();
        assertNotNull(term);
        assertEquals("id:1", term.toString());

        // cant call this because deep down in the Field builders is invokes the UserManager code and up comes JIRA
        final Document document = entityDocument.getDocument();
        assertNotNull(document);
        assertSame(expectedDocument, document);
    }

    //We need to call the correct constructor. Both constructors have the same number of arguments
    private DefaultSharedEntityIndexer getIndexerInstance()
    {
        try
        {
            final Constructor<DefaultSharedEntityIndexer> indexerConstructor = DefaultSharedEntityIndexer.class.getDeclaredConstructor(
                QueryFactory.class, SharedEntityAccessor.Factory.class, EntityDocumentFactory.class, DirectoryFactory.class);

            return mockController.instantiateAndReplayNice(DefaultSharedEntityIndexer.class, indexerConstructor);
        }
        catch (final NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

}
