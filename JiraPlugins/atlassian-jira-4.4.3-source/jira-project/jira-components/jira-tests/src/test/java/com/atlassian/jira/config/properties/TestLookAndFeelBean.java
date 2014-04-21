package com.atlassian.jira.config.properties;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 */
public class TestLookAndFeelBean extends MockControllerTestCase
{

    @Test
    public void testMissingColorsWillDefault()
    {
        ApplicationProperties applicationProperties = getMock(ApplicationProperties.class);
        expect(applicationProperties.getDefaultBackedString(anyString())).andStubReturn(null);
        
        LookAndFeelBean lookAndFeel = instantiate(LookAndFeelBean.class);

        assertEquals(LookAndFeelBean.DefaultColours.TOP_HIGHLIGHTCOLOUR, lookAndFeel.getTopHighlightColor());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_TEXTHIGHLIGHTCOLOUR, lookAndFeel.getTopTextHighlightColor());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_SEPARATOR_BGCOLOUR, lookAndFeel.getTopSeparatorBackgroundColor());

        assertEquals(LookAndFeelBean.DefaultColours.MENU_BGCOLOUR, lookAndFeel.getMenuBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_TEXTCOLOUR, lookAndFeel.getMenuTxtColour());

        assertEquals(LookAndFeelBean.DefaultColours.MENU_SEPARATOR, lookAndFeel.getMenuSeparatorColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_BGCOLOUR, lookAndFeel.getMenuBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_TEXTCOLOUR, lookAndFeel.getMenuTxtColour());

        assertEquals(LookAndFeelBean.DefaultColours.TEXT_ACTIVELINKCOLOR, lookAndFeel.getTextActiveLinkColour());
        assertEquals(LookAndFeelBean.DefaultColours.TEXT_LINKCOLOR, lookAndFeel.getTextLinkColour());
        assertEquals(LookAndFeelBean.DefaultColours.TEXT_HEADINGCOLOR, lookAndFeel.getTextHeadingColour());
    }
}
