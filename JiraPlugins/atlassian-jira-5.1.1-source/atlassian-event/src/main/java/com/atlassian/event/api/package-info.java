package com.atlassian.event.api;
/**
 * <p>This is the API package of Atlassian's event system. This will be kept backward compatible between major releases.</p>
 *
 * <p>In this package you can find the {@link com.atlassian.event.api.EventPublisher} which is the main interface one has
 * to interact with when working with Atlassian Event.</p>
 *
 * <p>The {@link com.atlassian.event.api.EventListener} annotation in this package help define methods on POJOs that can
 * handle events. Types of event handled by these methods is directly defined by the type of their <strong>only</strong>
 * parameter</p>
 *
 * <p>Finally the {@link com.atlassian.event.api.AsynchronousPreferred} annotation is to be used on event POJOs that can
 * be handled asynchronously. It is left up to the listeners to define whether they will do so, see
 * {@link com.atlassian.event.spi.ListenerInvoker} for more details on how this can be implemented.</p>
 */