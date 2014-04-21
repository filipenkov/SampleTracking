package com.atlassian.modzdetector;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;
import junit.framework.TestCase;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Unit test for ModzDetector.
 */
public class TestModzDetector extends TestCase
{
    private ResourceAccessor mockResourceAccessor;
    private static final String MAGIC_STRING = "MagicString";
    private static final HashAlgorithm MAGIC_HASH_ALGORITHM = new HashAlgorithm()
    {
        public String getHash(final InputStream stream)
        {
            return MAGIC_STRING;
        }

        public String getHash(final byte[] bytes)
        {
            return MAGIC_STRING;
        }
    };

    private static final HashAlgorithm IDENTITY_HASH_ALGORITHM = new HashAlgorithm()
    {
        public String getHash(final InputStream stream)
        {
            try
            {
                return IOUtils.toString(stream);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        public String getHash(final byte[] bytes)
        {
            return new String(bytes); // default encoding is ok today
        }
    };

    public void setUp() throws Exception
    {
        super.setUp();
        mockResourceAccessor = new ResourceAccessor()
        {
            public InputStream getResourceByPath(final String resourceName)
            {
                return new ByteArrayInputStream(resourceName.getBytes());
            }

            public InputStream getResourceFromClasspath(final String resourceName)
            {
                return getClass().getClassLoader().getResourceAsStream(resourceName);
            }

        };
    }

    public void testCheckResourceDegenerateCases() throws CannotCheckResource
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor);

        try
        {
            modzDetector.checkResource(null, null, null, null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException yay)
        {
        }
        try
        {
            modzDetector.checkResource("cp.myResouce", "myResource", null, null);
            fail("Expected CannotCheckResource");
        }
        catch (CannotCheckResource yay)
        {
        }
        final String myResource = "myResource";
        final ModzDetector.ResourceType resourceType = modzDetector.checkResource("cp."+ myResource, myResource, "12345", null);
        assertRemoved(myResource, resourceType);
    }

    private void assertRemoved(final String myResource, final ModzDetector.ResourceType resourceType)
    {
        Modifications mods = new Modifications();
        resourceType.handle(mods);
        assertTrue(mods.removedFiles.size() == 1);
        assertTrue(mods.removedFiles.get(0).equals(myResource));
        assertTrue(mods.modifiedFiles.size() == 0);
    }

    public void testCheckResourceModified() throws Exception
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM);

        final String myResource = "myResource";
        ByteArrayInputStream bytes = new ByteArrayInputStream(new byte[]{0, 1, 2, 3});
        final ModzDetector.ResourceType resourceType = modzDetector.checkResource("cp."+ myResource, myResource, "12345", bytes);
        Modifications mods = new Modifications();
        resourceType.handle(mods);
        assertTrue(mods.removedFiles.size() == 0);
        assertTrue(mods.modifiedFiles.size() == 1);
        assertTrue(mods.modifiedFiles.get(0).equals(myResource));
    }

    public void testCheckResourceUnModified() throws Exception
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM);

        final String myResource = "myResource";
        ByteArrayInputStream bytes = new ByteArrayInputStream(new byte[]{0, 1, 2, 3});
        final ModzDetector.ResourceType resourceType = modzDetector.checkResource("cp."+ myResource, myResource, MAGIC_STRING, bytes);
        Modifications mods = new Modifications();
        resourceType.handle(mods);
        assertTrue(mods.removedFiles.size() == 0);
        assertTrue(mods.modifiedFiles.size() == 0);
    }

    public void testCheckResourceFromKey() throws CannotCheckResource
    {
        final String classpathStreamContents = "classpath input stream contents";
        final InputStream classpathStream = getStringInputStream(classpathStreamContents);
        final String fileSystemStreamContents = "filesystem input stream contents";
        final InputStream fileSystemStream = getStringInputStream(fileSystemStreamContents);
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, IDENTITY_HASH_ALGORITHM, new StreamMapper()
        {
            public InputStream mapStream(String prefix, String resourceName)
            {
                if (HashRegistry.PREFIX_CLASSPATH.equals(prefix))
                {
                    return classpathStream;
                } else if (HashRegistry.PREFIX_FILESYSTEM.equals(prefix))
                {
                    return fileSystemStream;
                }
                return null;
            }

			public String getResourcePath(String resourceName) {
				return resourceName;
			}

			public String getResourceKey(File file) 
			{
				return null;
			}
        });
        assertNoChanges(fileSystemStreamContents, modzDetector, HashRegistry.PREFIX_FILESYSTEM + "key");
        assertNoChanges(classpathStreamContents, modzDetector, HashRegistry.PREFIX_CLASSPATH + "key");
    }

    public void testBadRegistryFormat()
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM);
        try
        {
            modzDetector.checkResource("bad.thing", null);
            fail("unexpected success during bad registry check");
        }
        catch (CannotCheckResource expected)
        {
        }
    }

    public void testCheckRegistry()
    {
        final AtomicBoolean haveThrown = new AtomicBoolean(false);
        final AtomicInteger invocationCount = new AtomicInteger(0);
        final ModzDetector.ResourceType invocationCountingResourceType = new ModzDetector.ResourceType("dummy")
        {
            void handle(final Modifications mods)
            {
                invocationCount.incrementAndGet();
            }
        };

        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM)
        {
            ResourceType checkResource(final String propertyKey, final String hash) throws CannotCheckResource
            {
                if (!haveThrown.getAndSet(true))
                {
                    throw new CannotCheckResource("first call fails");
                } else
                {
                    return invocationCountingResourceType;
                }
            }
        };
        final Properties registry = new Properties();
        // contents do not matter
        registry.setProperty("1", "1");
        registry.setProperty("2", "2");
        registry.setProperty("3", "3");
        modzDetector.checkRegistry(new Modifications(), registry);
        assertEquals("Should have one call for every key minus one exception thrown", registry.size() - 1, invocationCount.get());
    }

    public void testGetModifiedFiles() throws IOException, ModzRegistryException
    {
        final AtomicReference receivedMods = new AtomicReference(null);
        final AtomicReference receivedRegistry = new AtomicReference(null);
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM)
        {
            void checkRegistry(final Modifications mods, final Properties registry)
            {
                receivedMods.set(mods);
                receivedRegistry.set(registry);
            }
        };

        final Properties registry = new Properties();
        registry.setProperty("1", "1");
        registry.setProperty("2", "2");
        registry.setProperty("3", "3");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        registry.store(baos, "HASH TEST");
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        final Modifications modifiedFiles = modzDetector.getModifiedFiles(bais);
        assertTrue(receivedMods.get() == modifiedFiles);
        assertTrue(receivedRegistry.get().equals(registry));
    }

    public void testResourceStreamClosed() throws CannotCheckResource
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM);
        final AtomicBoolean closed = new AtomicBoolean(false);

        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0])
        {
            public void close() throws IOException
            {
                closed.set(true);
                super.close();
            }
        };
        modzDetector.checkResource("fs.test", "test", "hash", bais);
        assertTrue(closed.get());
    }

    public void testRegistryException()
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM);
        try
        {
            modzDetector.getModifiedFiles(createExplodingInputStream());
            fail("expected a ModzRegistryException");
        }
        catch (ModzRegistryException expected)
        {
            assertTrue(IOException.class.isAssignableFrom(expected.getCause().getClass()));
        }
    }

    public void testRegistryNull()
    {
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM);
        try
        {
            modzDetector.getModifiedFiles(null);
            fail("expected a ModzRegistryException");
        }
        catch (ModzRegistryException expected)
        {
        }
    }

    public void testGetAddedFiles() throws Exception
    {

        final File tempDir = new File("___testdirectory");
        nuke(tempDir); // in case it was there from last time
        tempDir.deleteOnExit();

        tempDir.mkdirs();
        
        ModzDetector modzDetector = new ModzDetector(mockResourceAccessor, MAGIC_HASH_ALGORITHM, new MockStreamMapper(tempDir));
        Properties registry = new Properties();
        registry.setProperty("foo", "foo");
        ByteArrayOutputStream regBaos = new ByteArrayOutputStream();
        registry.store(regBaos, "test registry");
        ByteArrayInputStream regBais = new ByteArrayInputStream(regBaos.toByteArray());
        List<String> addedFiles1 = modzDetector.getAddedFiles(regBais, tempDir, ModzDetector.OPEN_FILTER);
        StringBuffer addeds = new StringBuffer();
        for (String s : addedFiles1)
        {
            addeds.append(s).append("\n");
        }
        assertTrue("expected there to be no added files under my new dir! but there was:\n" + addeds, addedFiles1.isEmpty());
        // add a couple of files in it to be reported as added
        assertTrue("couldn't create bar file in temp dir", new File(tempDir, "bar").createNewFile());
        assertTrue("couldn't create baz file in temp dir", new File(tempDir, "baz").createNewFile());

        List<String> addedFiles = modzDetector.getAddedFiles(regBais, tempDir, ModzDetector.OPEN_FILTER);
        assertTrue(addedFiles.contains("bar"));
        assertTrue(addedFiles.contains("baz"));
        assertTrue(addedFiles.size() == 2);

    }

    private void nuke(File f)
    {
        if (f.isDirectory())
        {
            for (File file : f.listFiles())
            {
                nuke(file);
            }
        }
        else
        {
            if (!f.delete())
            {
                System.out.println("cannot nuke " + f.getAbsolutePath() + " , soz");
            }
        }

    }

    private void assertNoChanges(final String fileSystemStreamContents, final ModzDetector modzDetector, final String key)
            throws CannotCheckResource
    {
        Modifications mods = new Modifications();
        final ModzDetector.ResourceType fileSystemResourceType = modzDetector.checkResource(key, fileSystemStreamContents);
        fileSystemResourceType.handle(mods);
        assertTrue("modified files: " + mods.modifiedFiles.toString(), mods.modifiedFiles.isEmpty());
        assertTrue("removed files: " + mods.removedFiles.toString(), mods.removedFiles.isEmpty());
    }

    private InputStream getStringInputStream(final String s)
    {
        return new ByteArrayInputStream(s.getBytes());
    }

    InputStream createExplodingInputStream()
    {
        return (InputStream) Enhancer.create(InputStream.class, new MethodInterceptor()
        {
            public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy)
                    throws Throwable
            {
                if (Arrays.asList(method.getExceptionTypes()).contains(IOException.class))
                {
                    throw new IOException(method.getName() + " called");
                }

                return methodProxy.invoke(o, objects);
            }
        });
    }

    private static class MockStreamMapper implements StreamMapper
    {
        private final File tempDir;

        public MockStreamMapper(File tempDir)
        {
            this.tempDir = tempDir;
        }

        public InputStream mapStream(String prefix, String resourceName)
        {
            try
            {
                return new FileInputStream(resourceName);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }

		public String getResourcePath(String resourceName) {
			return resourceName.substring(3);
		}

		public String getResourceKey(File file) 
		{
            String tempDirPath = tempDir.getAbsolutePath() + "/";
            return HashRegistry.PREFIX_FILESYSTEM + file.getAbsolutePath().substring(tempDirPath.length());
		}
    }
}
