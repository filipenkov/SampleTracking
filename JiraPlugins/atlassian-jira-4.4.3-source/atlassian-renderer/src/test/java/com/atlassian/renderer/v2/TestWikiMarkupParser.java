package com.atlassian.renderer.v2;

import com.atlassian.renderer.v2.components.MacroTag;
import com.atlassian.renderer.v2.components.WikiContentHandler;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.atlassian.renderer.v2.macro.code.CodeMacro;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.FullConstraintMatcher;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestWikiMarkupParser extends TestCase
{
    private Mock mockMacroManager = new Mock(MacroManager.class);
    private Mock mockMacroHandler = new Mock(WikiContentHandler.class);
    private WikiMarkupParser parser;

    protected void setUp() throws Exception
    {
        super.setUp();
        this.parser = new WikiMarkupParser((MacroManager)mockMacroManager.proxy(), (WikiContentHandler)mockMacroHandler.proxy());
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        parser = null;
        mockMacroHandler = null;
        mockMacroManager = null;
    }

    public void testSimpleText()
    {
        String content = "blah blah blah blah";
        mockMacroHandler.expect("handleText", C.args(C.IS_ANYTHING,C.eq(content)) );

        parser.parse(content);
        mockMacroHandler.verify();
    }

    public void testOneMacro()
    {
        String content = "blah blah {code} i am inside the code {code}";
        MacroTag macroTag = MacroTag.makeMacroTag(content, 10);

        mockMacroManager.matchAndReturn("getEnabledMacro",C.eq("code"), new CodeMacro());
        mockMacroHandler.expect("handleText", C.args(C.IS_ANYTHING,C.eq("blah blah ")));
        mockMacroHandler.expect("handleMacro", new FullConstraintMatcher(new Constraint[]{ C.IS_ANYTHING, C.eq(macroTag),
                C.eq(" i am inside the code "), C.IS_TRUE}));

        parser.parse(content);
        mockMacroHandler.verify();
    }

    public void testNestedMacros()
    {
        String content = "blah blah {code} i am inside the code {noformat} we don't care you are inside {noformat} {code}";
        MacroTag macroTag = MacroTag.makeMacroTag(content, 10);

        mockMacroManager.matchAndReturn("getEnabledMacro",C.eq("code"), new CodeMacro());
        mockMacroHandler.expect("handleText", C.args(C.IS_ANYTHING,C.eq("blah blah ")));
        mockMacroHandler.expect("handleMacro", new FullConstraintMatcher(new Constraint[]{ C.IS_ANYTHING, C.eq(macroTag),
                C.eq(" i am inside the code {noformat} we don't care you are inside {noformat} "), C.IS_TRUE}));

        parser.parse(content);
        mockMacroHandler.verify();
    }

    public void testNoEndMacroTag()
    {
        String content = "blah blah {code} i am inside the code macro with no end tag!";
        MacroTag macroTag = MacroTag.makeMacroTag(content, 10);

        mockMacroManager.matchAndReturn("getEnabledMacro",C.eq("code"), new CodeMacro());
        mockMacroHandler.expect("handleText", C.args(C.IS_ANYTHING,C.eq("blah blah ")));
        mockMacroHandler.expect("handleText", C.args(C.IS_ANYTHING,C.eq(" i am inside the code macro with no end tag!")));
        mockMacroHandler.expect("handleMacro", new FullConstraintMatcher(new Constraint[]{ C.IS_ANYTHING, C.eq(macroTag),
                C.eq(""), C.IS_FALSE}));

        parser.parse(content);
        mockMacroHandler.verify();
    }
}
