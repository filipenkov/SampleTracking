package com.atlassian.plugins.rest.module.scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>Search for Java classes in the specified OSGi bundle that are annotated or have at least one method annotated with one or more of a set of annotations.</p>
 * <p>This implementation is <em>inspired</em> by the {@link com.sun.jersey.server.impl.container.config.AnnotatedClassScanner} in Jersey 1.0.1.</p>
 *
 * @see com.sun.jersey.server.impl.container.config.AnnotatedClassScanner
 */
public final class AnnotatedClassScanner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedClassScanner.class);

    private final Bundle bundle;
    private final Set<String> annotations;

    public AnnotatedClassScanner(Bundle bundle, Class<?>... annotations)
    {
        Validate.notNull(bundle);
        Validate.notEmpty(annotations, "You gotta scan for something!");

        this.bundle = bundle;
        this.annotations = getAnnotationSet(annotations);
    }

    public Set<Class<?>> scan(String... basePackages)
    {
        final File bundleFile = getBundleFile(bundle);
        if (!bundleFile.isFile() || !bundleFile.exists())
        {
            throw new RuntimeException("Could not identify Bundle at location <" + bundle.getLocation() + ">");
        }
        return indexJar(bundleFile, preparePackages(basePackages));
    }

    File getBundleFile(Bundle bundle)
    {
        final String bundleLocation = bundle.getLocation();
        final File bundleFile;
        if (bundleLocation.startsWith("file:"))
        {
            try
            {
                bundleFile = new File(URLDecoder.decode(new URL(bundleLocation).getFile(), "UTF-8"));
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException("Could not parse Bundle location as URL <" + bundleLocation + ">", e);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalStateException("Obviously something is wrong with your JVM... It doesn't support UTF-8 !?!", e);
            }
        }
        else
        {
            bundleFile = new File(bundleLocation);
        }
        return bundleFile;
    }

    private Set<String> preparePackages(String... packages)
    {
        final Set<String> packageNames = new HashSet<String>();
        for (String packageName : packages)
        {
            final String newPackageName = StringUtils.replaceChars(packageName, '.', '/');

            // make sure we have a trailing / to not confuse packages such as com.mycompany.package
            // and com.mycompany.package1 which would both start with com/mycompany/package once transformed
            if (!newPackageName.endsWith("/"))
            {
                packageNames.add(newPackageName + '/');
            }
            else
            {
                packageNames.add(newPackageName);
            }
        }

        return packageNames;
    }

    private Set<String> getAnnotationSet(Class... annotations)
    {
        final Set<String> formatedAnnotations = new HashSet<String>();
        for (Class cls : annotations)
        {
            formatedAnnotations.add("L" + cls.getName().replaceAll("\\.", "/") + ";");
        }
        return formatedAnnotations;
    }

    private Set<Class<?>> indexJar(File file, Set<String> packageNames)
    {
        // Make sure the set doesn't allow <code>null</code>
        final Set<Class<?>> classes = new HashSet<Class<?>>()
        {
            @Override
            public boolean add(Class<?> c)
            {
                return c != null && super.add(c);
            }
        };

        JarFile jar = null;
        try
        {
            jar = new JarFile(file);
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();)
            {
                final JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class"))
                {
                    if (packageNames.isEmpty())
                    {
                        classes.add(analyzeClassFile(jar, jarEntry));
                    }
                    else
                    {
                        for (String packageName : packageNames)
                        {
                            if (jarEntry.getName().startsWith(packageName))
                            {
                                classes.add(analyzeClassFile(jar, jarEntry));
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception while processing file, " + file, e);
        }
        finally
        {
            try
            {
                if (jar != null)
                {
                    jar.close();
                }
            }
            catch (IOException ex)
            {
                LOGGER.error("Error closing jar file, {}", jar.getName());
            }
        }
        return classes;
    }

    private Class analyzeClassFile(JarFile jarFile, JarEntry entry)
    {
        final AnnotatedClassVisitor visitor = new AnnotatedClassVisitor(annotations);
        getClassReader(jarFile, entry).accept(visitor, 0);
        return visitor.hasAnnotation() ? getClassForName(visitor.className) : null;
    }

    private ClassReader getClassReader(JarFile jarFile, JarEntry entry)
    {
        InputStream is = null;
        try
        {
            is = jarFile.getInputStream(entry);
            return new ClassReader(is);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Error accessing input stream of the jar file, " + jarFile.getName() + ", entry, " + entry.getName(), ex);
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            catch (IOException ex)
            {
                LOGGER.error("Error closing input stream of the jar file, {}, entry, {}, closed.", jarFile.getName(), entry.getName());
            }
        }
    }

    private Class getClassForName(String className)
    {
        try
        {
            return bundle.loadClass(className.replaceAll("/", "."));
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException("A class file of the class name, " + className + " is identified but the class could not be loaded", ex);
        }
    }

    private static final class AnnotatedClassVisitor implements ClassVisitor
    {

        private final Set<String> annotations;

        /**
         * The name of the visited class.
         */
        private String className;

        /**
         * True if the class has the correct scope
         */
        private boolean isScoped;
        /**
         * True if the class has the correct declared annotations
         */
        private boolean isAnnotated;

        public AnnotatedClassVisitor(Set<String> annotations)
        {
            this.annotations = annotations;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            className = name;
            isScoped = (access & Opcodes.ACC_PUBLIC) != 0;
            isAnnotated = false;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            isAnnotated |= annotations.contains(desc);
            return null;
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access)
        {
            // If the name of the class that was visited is equal to the name of this visited inner class then
            // this access field needs to be used for checking the scope of the inner class
            if (className.equals(name))
            {
                isScoped = (access & Opcodes.ACC_PUBLIC) != 0;
                // Inner classes need to be statically scoped
                isScoped &= (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            }
        }

        boolean hasAnnotation()
        {
            return isScoped && isAnnotated;
        }

        public void visitEnd()
        {
        }


        public void visitOuterClass(String string, String string0, String string1)
        {
        }

        public FieldVisitor visitField(int i, String string, String string0, String string1, Object object)
        {
            return null;
        }

        public void visitSource(String string, String string0)
        {
        }

        public void visitAttribute(Attribute attribute)
        {
        }

        public MethodVisitor visitMethod(int i, String string, String string0, String string1, String[] string2)
        {
            if (isAnnotated)
            {
                // the class has already been found annotated, no need to visit methods
                return null;
            }

            return new MethodVisitor()
            {
                public AnnotationVisitor visitAnnotationDefault()
                {
                    return null;
                }

                public AnnotationVisitor visitAnnotation(String desc, boolean visible)
                {
                    isAnnotated |= annotations.contains(desc);
                    return null;
                }

                public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible)
                {
                    return null;
                }

                public void visitAttribute(Attribute attr)
                {
                }

                public void visitCode()
                {
                }

                public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack)
                {
                }

                public void visitInsn(int opcode)
                {
                }

                public void visitIntInsn(int opcode, int operand)
                {
                }

                public void visitVarInsn(int opcode, int var)
                {
                }

                public void visitTypeInsn(int opcode, String type)
                {
                }

                public void visitFieldInsn(int opcode, String owner, String name, String desc)
                {
                }

                public void visitMethodInsn(int opcode, String owner, String name, String desc)
                {
                }

                public void visitJumpInsn(int opcode, Label label)
                {
                }

                public void visitLabel(Label label)
                {
                }

                public void visitLdcInsn(Object cst)
                {
                }

                public void visitIincInsn(int var, int increment)
                {
                }

                public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
                {
                }

                public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
                {
                }

                public void visitMultiANewArrayInsn(String desc, int dims)
                {
                }

                public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
                {
                }

                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
                {
                }

                public void visitLineNumber(int line, Label start)
                {
                }

                public void visitMaxs(int maxStack, int maxLocals)
                {
                }

                public void visitEnd()
                {
                }
            };
        }
    }
}
