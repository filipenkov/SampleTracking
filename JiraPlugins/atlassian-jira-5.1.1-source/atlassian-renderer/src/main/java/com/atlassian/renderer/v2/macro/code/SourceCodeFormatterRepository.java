package com.atlassian.renderer.v2.macro.code;

import java.util.Collection;

public interface SourceCodeFormatterRepository
{
    SourceCodeFormatter getSourceCodeFormatter(String language);

    Collection getAvailableLanguages();
}
