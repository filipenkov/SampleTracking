package com.atlassian.johnson.setup;

import com.atlassian.johnson.Initable;
import com.atlassian.johnson.JohnsonEventContainer;

/**
 * We need a way to handle MultiTenancy. In non-MT the ServletContext contains a plain-old JohnsonEventContainer.
 * In MT, however, we need a JohnsonEventContainer that can deal with MultiTenancy. The host application needs to
 * implement this class and then specify it in the johnson-config.xml via the container-factory element. This keeps
 * any dependency on multitenant-library out of atlassian-johnson.
 * @since v1.1
 */
public interface ContainerFactory extends Initable
{
    JohnsonEventContainer create();
}
