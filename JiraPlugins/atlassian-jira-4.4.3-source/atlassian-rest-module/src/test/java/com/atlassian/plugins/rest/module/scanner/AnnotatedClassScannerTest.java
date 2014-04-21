package com.atlassian.plugins.rest.module.scanner;

import com.atlassian.rest.test.jar.AnAnnotation;
import com.atlassian.rest.test.jar.AnnotatedClass;
import com.atlassian.rest.test.jar.ClassWithAnnotatedMethod;
import com.atlassian.rest.test.jar2.AnotherAnnotatedClass;
import com.atlassian.rest.test.jar2.jar3.YetAnotherAnnotatedClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.osgi.framework.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AnnotatedClassScannerTest
{
    private AnnotatedClassScanner scanner;
    private Bundle bundle;

    @Before
    public void setUp() throws Exception
    {
        bundle = mock(Bundle.class);
        when(bundle.getLocation()).thenReturn(getTestJar().getAbsolutePath());
        when(bundle.loadClass(AnnotatedClass.class.getName())).thenReturn(AnnotatedClass.class);
        when(bundle.loadClass(AnotherAnnotatedClass.class.getName())).thenReturn(AnotherAnnotatedClass.class);
        when(bundle.loadClass(YetAnotherAnnotatedClass.class.getName())).thenReturn(YetAnotherAnnotatedClass.class);
        when(bundle.loadClass(ClassWithAnnotatedMethod.class.getName())).thenReturn(ClassWithAnnotatedMethod.class);

        scanner = new AnnotatedClassScanner(bundle, AnAnnotation.class);
    }

    @Test
    public void testScanWithNoBasePackage()
    {
        testScan(Arrays.asList(AnnotatedClass.class, AnotherAnnotatedClass.class, YetAnotherAnnotatedClass.class, ClassWithAnnotatedMethod.class));
    }

    @Test
    public void testScanWithBasePackage()
    {
        testScan(Arrays.asList(AnnotatedClass.class, ClassWithAnnotatedMethod.class), "com.atlassian.rest.test.jar");
    }

    @Test
    public void testScanWithBasePackageUsingSlashes()
    {
        testScan(Arrays.asList(AnnotatedClass.class, ClassWithAnnotatedMethod.class), "com/atlassian/rest/test/jar");
    }

    @Test
    public void testScanWithBasePackageAndSubPackages()
    {
        testScan(Arrays.asList(AnotherAnnotatedClass.class, YetAnotherAnnotatedClass.class), "com/atlassian/rest/test/jar2");
    }

    @Test
    public void testScanWithMultipleBasePackagesAndSubPackages()
    {
        testScan(Arrays.asList(AnnotatedClass.class, ClassWithAnnotatedMethod.class, AnotherAnnotatedClass.class, YetAnotherAnnotatedClass.class), "com/atlassian/rest/test/jar", "com/atlassian/rest/test/jar2");
    }

    @Test
    public void testScanWithMultipleBasePackages()
    {
        testScan(Arrays.asList(AnnotatedClass.class, ClassWithAnnotatedMethod.class, YetAnotherAnnotatedClass.class), "com/atlassian/rest/test/jar", "com/atlassian/rest/test/jar2/jar3");
    }

    @Test
    public void testGetBundleFileEscapesBundleLocationBeforeResolvingFile()
    {
        final String bundlePath = "some%20path";
        when(bundle.getLocation()).thenReturn("file:" + bundlePath);

        final File bundleFile = scanner.getBundleFile(bundle);

        assertEquals(bundlePath.replace("%20", " "), bundleFile.getPath());
    }

    public void testScan(List<Class<?>> expected, String... packages)
    {
        final Set<Class<?>> classes = scanner.scan(packages);
        assertCollectionEquals(expected, new ArrayList<Class<?>>(classes));
    }

    private static void assertCollectionEquals(List<Class<?>> expected, List<Class<?>> actual)
    {
        try
        {
            assertEquals(expected.size(), actual.size());
        }
        catch (AssertionError error)
        {
            System.err.println("Expected " + expected + " but got " + actual);
            throw error;
        }
        for (Class<?> aClass : expected)
        {
            try
            {
                assertTrue(actual.contains(aClass));
            }
            catch (AssertionError error)
            {
                System.err.println("Expected <" + aClass + "> in " + actual);
                throw error;
            }
        }
    }

    private File getTestJar()
    {
        return new File(this.getClass().getClassLoader().getResource("atlassian-rest-test-jar.jar").getFile());
    }
}
