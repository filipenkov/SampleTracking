package com.atlassian.jira.easymock;

import org.easymock.classextension.EasyMock;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * Utilities for creating mocks.
 *
 * @since 4.4
 */
public class EasyMockAnnotations
{
    /**
     * Creates a new mock for every field of the object that is marked with {@link Mock}, using EasyMock's create*
     * methods.
     *
     * @param object an object
     */
    public static void initMocks(Object object)
    {
        initMocks(object, new EasyMockFactory());
    }

    /**
     * Creates a new mock for every field of the object that is marked with {@link Mock}, using the provided mock
     * factory.
     *
     * @param object an object
     * @param mockFactory the MockFactory that will actually create the mocks
     */
    public static void initMocks(Object object, MockFactory mockFactory)
    {
        doWithFields(object.getClass(), new CreateMockCallback(object, mockFactory), new AnnotatedField());
    }

    /**
     * Replays every mock object that is a field of this class by calling EasyMock.replay()
     *
     * @param object an object
     */
    public static void replayMocks(Object object)
    {
        replayMocks(object, new EasyMockReplayer());
    }

    /**
     * Replays every mock object that is a field of this class by calling {@link MockAction#doWithMock(Mock, Class,
     * Object)} on the provided mock action.
     *
     * @param object an object
     * @param mockAction the MockAction to use for replaying mocks
     */
    public static void replayMocks(Object object, MockAction mockAction)
    {
        doWithFields(object.getClass(), new PerformActionCallback(mockAction, object), new AnnotatedField());
    }

    private EasyMockAnnotations()
    {
    }

    private static class CreateMockCallback implements ReflectionUtils.FieldCallback
    {
        private final Object object;
        private final MockFactory mockFactory;

        public CreateMockCallback(Object object, MockFactory mockFactory)
        {
            this.object = object;
            this.mockFactory = mockFactory;
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
        {
            makeAccessible(field);
            Mock annotation = field.getAnnotation(Mock.class);
            Class<?> type = field.getType();
            setField(field, object, mockFactory.createMock(annotation, type));
        }
    }

    /**
     * Performs a given action on each mock field.
     */
    private static class PerformActionCallback implements ReflectionUtils.FieldCallback
    {
        private final MockAction action;
        private final Object testClass;

        public PerformActionCallback(MockAction action, Object testClass)
        {
            this.action = action;
            this.testClass = testClass;
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
        {
            makeAccessible(field);
            Mock annotation = field.getAnnotation(Mock.class);
            Class<?> type = field.getType();
            action.doWithMock(annotation, type, field.get(testClass));
        }
    }

    private static class AnnotatedField implements ReflectionUtils.FieldFilter
    {
        @Override
        public boolean matches(Field field)
        {
            return field.getAnnotation(Mock.class) != null;
        }
    }

    /**
     * Create mocks using EasyMock ClassExtension's create* methods.
     */
    private static class EasyMockFactory implements MockFactory
    {
        @Override
        public Object createMock(Mock mock, Class<?> mockClass)
        {
            if (mock.value() == MockType.NICE) { return EasyMock.createNiceMock(mockClass); }
            if (mock.value() == MockType.STRICT) { return EasyMock.createStrictMock(mockClass); }

            return EasyMock.createMock(mockClass);
        }
    }

    /**
     * Replay mocks using EasyMock.
     */
    private static class EasyMockReplayer implements MockAction
    {
        @Override
        public void doWithMock(Mock mock, Class<?> mockClass, Object mockObject)
        {
            if (mockObject != null)
            {
                EasyMock.replay(mockObject);
            }
        }
    }
}
