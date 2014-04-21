package com.atlassian.crowd.embedded.admin;

import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ConfigurationControllerTest
{
    private static final Date CREATED_DATE = new Date(0);

    private static final Directory DIRECTORY = new ImmutableDirectory(
            1L,
            "Directory",
            true,
            "Description",
            "SHA-1",
            DirectoryType.CUSTOM,
            "Implementation class",
            CREATED_DATE,
            new Date(),
            EnumSet.allOf(OperationType.class),
            ImmutableMap.of("key1", "value1")
    );

    private ConfigurationController configurationController;
    private Directory mockDirectory;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object command;
    private BindException errors;
    private DirectoryManager directoryManager;
    private I18nResolver i18nResolver;
    private HtmlEncoder encoder;
    private CrowdDirectoryService directoryService;

    @Before
    public void setUp() throws Exception
    {
        mockDirectory = mock(Directory.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        command = mock(Object.class);
        errors = new BindException(command, "command");
        directoryManager = mock(DirectoryManager.class);
        directoryService = mock(CrowdDirectoryService.class);
        i18nResolver = mock(I18nResolver.class);
        encoder = mock(HtmlEncoder.class);

        configurationController = new ConfigurationController()
        {
            @Override
            protected Directory createDirectory(Object command)
            {
                return mockDirectory;
            }
        };

        configurationController.setDirectoryManager(directoryManager);
        configurationController.setI18nResolver(i18nResolver);
        configurationController.setHtmlEncoder(encoder);
        configurationController.setCrowdDirectoryService(directoryService);

        when(mockDirectory.getId()).thenReturn(0L);
        when(mockDirectory.getName()).thenReturn("MOCK_DIRECTORY");
        when(mockDirectory.getAttributes()).thenReturn(new HashMap<String, String>());
    }

    @Test
    public void testCreateUpdatedDirectory_UpdatesValues() throws Exception
    {
        final ImmutableDirectory.Builder newDirectoryBuilder = ImmutableDirectory.newBuilder(DIRECTORY);
        newDirectoryBuilder.setName("New Directory");

        final Directory updatedDirectory = configurationController.createUpdatedDirectory(DIRECTORY, newDirectoryBuilder.toDirectory());

        assertEquals("New Directory", updatedDirectory.getName());
    }

    @Test
    public void testCreateUpdatedDirectory_PreservesCreatedDate() throws Exception
    {
        final ImmutableDirectory.Builder newDirectoryBuilder = ImmutableDirectory.newBuilder(DIRECTORY);
        newDirectoryBuilder.setCreatedDate(new Date());

        final Directory updatedDirectory = configurationController.createUpdatedDirectory(DIRECTORY, newDirectoryBuilder.toDirectory());

        assertEquals(CREATED_DATE, updatedDirectory.getCreatedDate());
    }

    @Test
    public void testCreateUpdatedDirectory_CreatesAttributes() throws Exception
    {
        final ImmutableDirectory.Builder newDirectoryBuilder = ImmutableDirectory.newBuilder(DIRECTORY);
        newDirectoryBuilder.setAttributes(ImmutableMap.of("key2", "value2"));

        final Directory updatedDirectory = configurationController.createUpdatedDirectory(DIRECTORY, newDirectoryBuilder.toDirectory());

        assertEquals(ImmutableMap.of("key1", "value1", "key2", "value2"), updatedDirectory.getAttributes());
    }

    @Test
    public void testCreateUpdatedDirectory_UpdatesAttributes() throws Exception
    {
        final ImmutableDirectory.Builder newDirectoryBuilder = ImmutableDirectory.newBuilder(DIRECTORY);
        newDirectoryBuilder.setAttributes(ImmutableMap.of("key1", "value2"));

        final Directory updatedDirectory = configurationController.createUpdatedDirectory(DIRECTORY, newDirectoryBuilder.toDirectory());

        assertEquals(ImmutableMap.of("key1", "value2"), updatedDirectory.getAttributes());
    }

    @Test
    public void testCreateUpdatedDirectory_PreservesAttributes() throws Exception
    {
        final ImmutableDirectory.Builder newDirectoryBuilder = ImmutableDirectory.newBuilder(DIRECTORY);
        newDirectoryBuilder.setAttributes(ImmutableMap.<String, String>of());

        final Directory updatedDirectory = configurationController.createUpdatedDirectory(DIRECTORY, newDirectoryBuilder.toDirectory());

        assertEquals(ImmutableMap.of("key1", "value1"), updatedDirectory.getAttributes());
    }

    private void setSubmissionMode(String mode) {
        when(request.getParameter(mode)).thenReturn(mode);
    }

    @Test
    public void testOnSubmit_SavesNewDirectoryAndReturnsSuccess() throws Exception
    {
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);

        configurationController.setTransactionTemplate(transactionTemplate);

        setSubmissionMode("save");
        
        when(directoryManager.searchDirectories(any(EntityQuery.class))).thenReturn(Lists.<Directory>newArrayList());
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                TransactionCallback callback = (TransactionCallback) args[0];
                return callback.doInTransaction();
            }
        });
        when(directoryService.addDirectory(mockDirectory)).thenReturn(mockDirectory);

        configurationController.onSubmit(request, response, command, errors);

        verify(directoryService, times(1)).addDirectory(mockDirectory);
        assertThat(errors.getErrorCount(), is(0));
    }

    @Test
    public void testOnSubmit_AddsErrorWhenDirectoryNameIsInUse() throws Exception
    {
        String errorMessage = "ERROR: name in use";
        List<Directory> existingDirectories = Lists.newArrayList(mock(Directory.class));

        setSubmissionMode("save");

        when(directoryManager.searchDirectories(any(EntityQuery.class))).thenReturn(existingDirectories);
        when(i18nResolver.getText(eq("embedded.crowd.save.directory.failed"), any(Serializable.class))).thenReturn(errorMessage);
        when(encoder.encode(anyString())).thenReturn(errorMessage);
        
        configurationController.onSubmit(request, response, command, errors);

        assertThat(errors.getGlobalError().getDefaultMessage(), is(errorMessage));
    }

    @Test
    public void testOnSubmit_TestsDirectoryConnection() throws Exception
    {
        setSubmissionMode("test");

        ModelAndView modelAndView = configurationController.onSubmit(request, response, command, errors);

        verify(directoryService, times(1)).testConnection(mockDirectory);
        assertThat(modelAndView.getModelMap().containsKey("testSuccessful"), is(true));
    }

    @Test
    public void testOnSubmit_AddsErrorWhenConnectionFails() throws Exception
    {
        String errorMessage = "FUNKY FAIL";

        setSubmissionMode("test");

        doThrow(new OperationFailedException(errorMessage)).when(directoryService).testConnection(mockDirectory);
        when(encoder.encode(errorMessage)).thenReturn(errorMessage);
        when(i18nResolver.getText(eq("embedded.crowd.connection.test.failed"), any(Serializable.class))).thenReturn(errorMessage);

        configurationController.onSubmit(request, response, command, errors);

        assertThat(errors.getGlobalError().getDefaultMessage(), is(errorMessage));
    }
}
