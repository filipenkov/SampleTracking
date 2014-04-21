package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.macro.Macro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.util.TextUtils;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class TestMacroRendererComponent extends TestCase
{
    private Mock mockMacroManager;
    private Mock mockMacro;
    private Mock mockSubRenderer;

    private MacroRendererComponent renderer;
    private TokenRendererComponent detokenizer;
    private RenderedContentStore store;

    protected void setUp() throws Exception
    {
        super.setUp();
        detokenizer = new TokenRendererComponent(null);
        store = new RenderedContentStore();

        mockMacro = new Mock(Macro.class);
        mockMacro.matchAndReturn("getBodyRenderMode", RenderMode.SIMPLE_TEXT);
        mockMacro.matchAndReturn("isInline", true);
        mockMacro.matchAndReturn("hasBody", true);

        mockMacroManager = new Mock(MacroManager.class);
        mockMacroManager.matchAndReturn("getEnabledMacro", "exists", mockMacro.proxy());
        mockMacroManager.matchAndReturn("getEnabledMacro", "absent", null);

        mockSubRenderer = new Mock(SubRenderer.class);

        renderer = new MacroRendererComponent((MacroManager) mockMacroManager.proxy(), (SubRenderer) mockSubRenderer.proxy());
    }

    public void testMacroNoBody()
    {
        testMacroSuccess("{exists}", "");
        testMacroSuccess("\\\\{exists}\\\\", "", "\\\\", "\\\\", makeEmptyParameterList());
    }

    public void testMacroBody()
    {
        testMacroSuccess("{exists}This is the body{exists}", "This is the body");
    }

    public void testMacroBodyMultiline()
    {
        testMacroSuccess("{exists}\nThis is the body\n{exists}", "\nThis is the body\n");
    }

    public void testMacroWithStuffAroundIt()
    {
        testMacroSuccess("this macro {exists} dude", "", "this macro ", " dude", makeEmptyParameterList());
        testMacroSuccess("this is a {exists}macro} dude", "", "this is a ", "macro} dude", makeEmptyParameterList());
        testMacroSuccess("{bob{exists}macro}", "", "{bob", "macro}", makeEmptyParameterList());
        testMacroSuccess("{bob{exists}macro}{exists}foo", "macro}", "{bob", "foo", makeEmptyParameterList());

        // say we're doing something like "{{{color:red}monospaced red text{color}}}"?
        testMacroSuccess("{{{exists}some text{exists}}}", "some text", "{{", "}}", makeEmptyParameterList());
    }

    public void testMacroNamedParams()
    {
        Map paramsMap = new HashMap();
        paramsMap.put(Macro.RAW_PARAMS_KEY, "cheese=Gouda|wine=Sauvignon Blanc");
        paramsMap.put("cheese", "Gouda");
        paramsMap.put("wine", "Sauvignon Blanc");

        testMacroSuccess("{exists:cheese=Gouda|wine=Sauvignon Blanc}", "", paramsMap);
    }

    public void testMacroNamedSpacedOutParams()
    {
        Map paramsMap = new HashMap();
        paramsMap.put(Macro.RAW_PARAMS_KEY, "  cheese  =  Gouda   |   wine   =   Sauvignon Blanc");
        paramsMap.put("cheese", "Gouda");
        paramsMap.put("wine", "Sauvignon Blanc");

        testMacroSuccess("{exists:  cheese  =  Gouda   |   wine   =   Sauvignon Blanc}", "", paramsMap);
    }

    public void testMacroNumberedParams()
    {
        Map paramsMap = new HashMap();
        paramsMap.put(Macro.RAW_PARAMS_KEY, "Gouda|Sauvignon Blanc|termites");
        paramsMap.put("0", "Gouda");
        paramsMap.put("1", "Sauvignon Blanc");
        paramsMap.put("2", "termites");

        testMacroSuccess("{exists:Gouda|Sauvignon Blanc|termites}", "", paramsMap);
    }

    public void testMacroMixedParams()
    {
        Map paramsMap = new HashMap();
        paramsMap.put(Macro.RAW_PARAMS_KEY, "Gouda|wine=Sauvignon Blanc|termites");
        paramsMap.put("0", "Gouda");
        paramsMap.put("wine", "Sauvignon Blanc");
        paramsMap.put("2", "termites");

        testMacroSuccess("{exists:Gouda|wine=Sauvignon Blanc|termites}", "", paramsMap);
    }

    public void testMacroParamsAndBody()
    {
        Map paramsMap = new HashMap();
        paramsMap.put(Macro.RAW_PARAMS_KEY, "Gouda|wine=Sauvignon Blanc|termites");
        paramsMap.put("0", "Gouda");
        paramsMap.put("wine", "Sauvignon Blanc");
        paramsMap.put("2", "termites");

        testMacroSuccess("{exists:Gouda|wine=Sauvignon Blanc|termites}I like chickens{exists}", "I like chickens", paramsMap);
    }

    public void testEscaped()
    {
        testMacroNotTriggered("\\{exists}");
        testMacroNotTriggered("\\{exists\\}");
        testMacroSuccess("{exists}Macro \\{exists} Text{exists}", "Macro \\{exists} Text");
        testMacroSuccess("{exists}Macro \\{exists\\} Text{exists}", "Macro \\{exists\\} Text");
        testMacroSuccess("{exists}Macro \\{exists\\} Text\\\\{exists}", "Macro \\{exists\\} Text\\\\");
    }

    public void testAdjacentMacros()
    {
        RenderContext expectedContext = new RenderContext();
        mockMacroManager.matchAndReturn("getEnabledMacro", "another", mockMacro.proxy());

        String input = "{exists}{another}";
        mockMacro.expectAndReturn("execute", C.args(C.eq(makeEmptyParameterList()), C.eq(""), C.eq(expectedContext)), "I like cheese");
        mockMacro.expectAndReturn("execute", C.args(C.eq(makeEmptyParameterList()), C.eq(""), C.eq(expectedContext)), "I like cheese");

        String tokenized = renderer.render(input, expectedContext);
        assertEquals("I like cheeseI like cheese", detokenizer.render(tokenized, expectedContext));

        mockMacro.verify();
        mockSubRenderer.verify();
    }

    public void testMacroThrowsException()
    {
        testMacroThrowsException("{exists}", new MacroException("Expected exception"), "exists: Expected exception", "");
        testMacroThrowsException("{exists}has a body{exists}", new MacroException("Expected exception"), "exists: Expected exception", "has a body");
        testMacroThrowsException("{exists}", new RuntimeException("Unexpected exception"), "Error formatting macro: exists: java.lang.RuntimeException: Unexpected exception", "");
        testMacroThrowsException("{exists}has a body{exists}", new RuntimeException("Unexpected exception"), "Error formatting macro: exists: java.lang.RuntimeException: Unexpected exception", "has a body");
    }

    public void testMacroThrowsError()
    {
        testMacroThrowsException("{exists}", new Error("Unexpected exception"), "Error formatting macro: exists: java.lang.Error: Unexpected exception", "");
        testMacroThrowsException("{exists}has a body{exists}", new Error("Unexpected exception"), "Error formatting macro: exists: java.lang.Error: Unexpected exception", "has a body");
    }
    
    public void testNonExistentMacroNoBody()
    {
        testMacroNotFound("{absent}", "");
    }

    public void testNonExistentMacroBody()
    {
        testMacroNotFound("{absent}Macro with a body{absent}", "Macro with a body");
    }
    
    public void testNonExistentMacroWithMacroInBody()
    {
        testMacroNotFound("{absent}{exists}{absent}", "{exists}");
        testMacroNotFound("{absent}{exists}Macro with a body{exists}{absent}", "{exists}Macro with a body{exists}");
    }

    public void testForceMacroNoBody()
    {
        
    }

    public void testNotMacroMarkup()
    {
        testMacroNotTriggered("{$var}");
        testMacroNotTriggered("{}");
        testMacroNotTriggered("{  }");
        testMacroNotTriggered("{monospaced");
        testMacroNotTriggered("{monospaced:");
        testMacroNotTriggered("{:}");
    }

    public void testMultipleBrackets()
    {
        testMacroNotTriggered("{{monospaced}}");

        // Macro inside monospacing. Hey, it could be possible!
        testMacroSuccess("{{{exists}}}", "", "{{", "}}", makeEmptyParameterList());
    }

    public void testMacroContainingEscapedMacro()
    {
        testMacroSuccess("{exists}Hi \\{exists} Joe{exists}", "Hi \\{exists} Joe");
        testMacroSuccess("{exists}Hi \\{exists\\} Joe{exists}", "Hi \\{exists\\} Joe");
    }

    public void testBodylessMacro()
    {
        Mock mockBodylessMacro = new Mock(Macro.class);
        mockBodylessMacro.matchAndReturn("getBodyRenderMode", RenderMode.SIMPLE_TEXT);
        mockBodylessMacro.matchAndReturn("isInline", true);
        mockBodylessMacro.matchAndReturn("hasBody", false);

        mockMacroManager.matchAndReturn("getEnabledMacro", "nobody", mockBodylessMacro.proxy());

        RenderContext expectedContext = new RenderContext();

        mockBodylessMacro.expectAndReturn("execute", C.args(C.eq(makeEmptyParameterList()), C.eq(""), C.eq(expectedContext)), "I like cheese");
        mockBodylessMacro.expectAndReturn("execute", C.args(C.eq(makeEmptyParameterList()), C.eq(""), C.eq(expectedContext)), "I like cheese");

        String tokenized = renderer.render("{nobody} not a body {nobody}", expectedContext);
        assertEquals("I like cheese not a body I like cheese", detokenizer.render(tokenized, expectedContext));

        mockBodylessMacro.verify();
        mockSubRenderer.verify();
    }

    // RNDR-4
    public void testMacroWithNullBodyRenderModeRendersInnerMacros()
    {
        Mock mockMacro = new Mock(Macro.class);
        mockMacro.matchAndReturn("getBodyRenderMode", null);
        mockMacro.matchAndReturn("isInline", false);
        mockMacro.matchAndReturn("hasBody", true);

        RenderContext expectedContext = new RenderContext();
        final String bodyContent = "{panel}this is inside the panel macro{panel} this is inside the test macro";
        final String renderedContent = "<div class=\"panel\"><div class=\"panelContent\">this is inside the panel macro</div></div> this is inside the test macro";

        mockMacro.expectAndReturn("execute", C.args(C.eq(makeEmptyParameterList()), C.eq(bodyContent), C.eq(expectedContext)), bodyContent);
        mockMacroManager.matchAndReturn("getEnabledMacro", "test", mockMacro.proxy());
        mockSubRenderer.expectAndReturn("render", C.args(C.eq(bodyContent), C.eq(expectedContext), C.eq(RenderMode.MACROS_ONLY)), renderedContent);

        String tokenized = renderer.render("{test}" + bodyContent + "{test}", expectedContext);
        assertEquals(renderedContent, detokenizer.render(tokenized, expectedContext));

        mockMacro.verify();
        mockSubRenderer.verify();
    }

    private void testMacroNotFound(String wiki, String body)
    {
        RenderContext expectedContext = new RenderContext();
        mockSubRenderer.expectAndReturn("render", C.args(C.eq(body), C.eq(expectedContext), C.IS_NULL), "I like fish");
        String tokenized = renderer.render(wiki, expectedContext);
        assertEquals(RenderUtils.blockError("Unknown macro: {absent}", "I like fish"), detokenizer.render(tokenized, expectedContext));
        mockSubRenderer.verify();
    }

    private void testMacroThrowsException(String wiki, Throwable exception, String message, String body)
    {
        mockMacro.reset();
        mockMacro.matchAndThrow("execute", C.ANY_ARGS, exception);
        mockMacro.matchAndReturn("getBodyRenderMode", RenderMode.PHRASES_IMAGES);
        mockMacro.matchAndReturn("hasBody", true);

        RenderContext context = new RenderContext();
        if (TextUtils.stringSet(body))
            mockSubRenderer.expectAndReturn("render", C.args(C.eq(body), C.eq(context), C.eq(RenderMode.PHRASES_IMAGES)), "rendered return");

        mockSubRenderer.expectAndReturn("render", C.args(C.eq(body), C.eq(context), C.IS_NULL), "rendered error");

        String tokenized = renderer.render(wiki, context);
        assertEquals(RenderUtils.blockError(message, "rendered error"), detokenizer.render(tokenized, context));
    }

    private void testMacroSuccess(String wiki, String body)
    {
        testMacroSuccess(wiki, body, makeEmptyParameterList());
    }

    private Map makeEmptyParameterList()
    {
        Map expectedParams = new HashMap();
        expectedParams.put(Macro.RAW_PARAMS_KEY, "");
        return expectedParams;
    }

    private void testMacroSuccess(String wiki, String body, Map expectedParams)
    {
        testMacroSuccess(wiki, body, "", "", expectedParams);
    }

    private void testMacroSuccess(String wiki, String body, String before, String after, Map expectedParams)
    {
        RenderContext expectedContext = new RenderContext();

        if (TextUtils.stringSet(body))
        {
            mockSubRenderer.expectAndReturn("render", C.args(C.eq(body), C.eq(expectedContext), C.eq(RenderMode.SIMPLE_TEXT)), "I like monkeys");
            mockMacro.expectAndReturn("execute", C.args(C.eq(expectedParams), C.eq("I like monkeys"), C.eq(expectedContext)), "I like cheese");
        }
        else
            mockMacro.expectAndReturn("execute", C.args(C.eq(expectedParams), C.eq(""), C.eq(expectedContext)), "I like cheese");


        String tokenized = renderer.render(wiki, expectedContext);
        assertEquals(before + "I like cheese" + after, detokenizer.render(tokenized, expectedContext));

        mockMacro.verify();
        mockSubRenderer.verify();
    }

    private void testMacroNotTriggered(String wiki)
    {
        assertEquals(wiki, renderer.render(wiki, new RenderContext()));
    }
}
