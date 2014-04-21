package com.atlassian.aui.javascript;

import com.atlassian.sal.api.ApplicationProperties;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class TestContextPathTransformer extends TestCase {
    private ApplicationProperties appProps;
    private ContextPathTransformer transformer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        appProps = mock(ApplicationProperties.class);
    }

    public void testWithContext() throws Exception
    {
        mockProps("http://foo.com/jira/");

        CharSequence result = transform("    var _context = \"%CONTEXT_PATH%\";");
        assertEquals("    var _context = \"\\/jira\";", result);
    }
    public void testEmptyContext() throws Exception
    {
        mockProps("http://foo.com/");

        CharSequence result = transform("    var _context = \"%CONTEXT_PATH%\";");
        assertEquals("    var _context = \"\";", result);
    }

    private void mockProps(String base) throws Exception {
        stub(appProps.getBaseUrl()).toReturn(base);
        transformer = new ContextPathTransformer(appProps);
    }

    private String transform(String javascript) {
        SearchAndReplaceDownloadableResource resource =
                (SearchAndReplaceDownloadableResource) transformer.transform(null, null, null, null);
        return resource.transform(javascript).toString();
    }

}
