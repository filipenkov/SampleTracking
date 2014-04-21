package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.macro.code.formatter.AbstractFormatter;

public class ActionScriptFormatter extends AbstractFormatter
{
    private static final String KEYWORDS =
            "\\b(break|call|chr|continue|delete|do|duplicateMovieClip|else|escape|eval|for|" +
            "fscommand|function|getProperty|getTimer|getURL|getVersion|gotoAndPlay|gotoAndStop|" +
            "if|ifFrameLoaded|in|int|isFinite|isNaN|length|loadMo|vie|loadVariables|maxscroll|" +
            "mbchr|mblength|mbord|mbsubstring|NaN|new|newline|nextFrame|nextScene|null|o|ClipEvent|" +
            "load|unload|enterFrame|mouseMove|mouseDown|mouseUp|keyDown|keyUp|data|on|press|" +
            "release|releas|Outside|rollOver|rollOut|dragOver|dragOut|keyPress|ord|parseFloat|" +
            "parseInt|play|prevFrame|prevScene|pri|t|printAsBitmap|random|removeMovieClip|return|" +
            "scroll|set|setProperty|startDrag|stop|stopAllSounds|stopD|ag|substring|targetPath|" +
            "tellTarget|this|toggleHighQuality|trace|typeof|unescape|unloadMovie|updateAfter|Event|" +
            "var|void|while|with)\\b";

    private static final String OBJECTS =
            "\\b(Array|Boolean|Color|Date|Infinity|Key|Math|Mouse|MovieClip|Number|Object|Selection|" +
            "Sound|String|XML|XMLSocket)\\b";
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "actionscript" };

    public ActionScriptFormatter()
    {
        addReplacement(KEYWORDS, KEYWORD_REPLACEMENT);
        addReplacement(OBJECTS, OBJECT_REPLACEMENT);
    }

    public String[] getSupportedLanguages()
    {
        return SUPPORTED_LANGUAGES;
    }
}