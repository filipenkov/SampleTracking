package com.atlassian.rpc.jsonrpc;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.voorhees.I18nAdapter;

import java.io.Serializable;

/**
 * Bridge to SAL internationalisation services
 */
class SALI18nAdapter implements I18nAdapter
{
    private final I18nResolver i18nResolver;

    public SALI18nAdapter(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    @Override
    public String getText(String key, Serializable... arguments)
    {
        return i18nResolver.getText(key, arguments);
    }

    @Override
    public String getText(String key)
    {
        return i18nResolver.getText(key);
    }
}
