package com.atlassian.event.legacy;
/**
 * <p>This package contains an implementation of a {@link com.atlassian.event.spi.ListenerHandler} that can deal with
 * the legacy event classes (i.e. {@link com.atlassian.event.Event}, {@link com.atlassian.event.EventListener}.</p>
 *
 * <p>It also contains the {@link SpringContextEventPublisher} which will actually publish events
 * to the Spring context it is defined in. This implementation allows full backward compatibility, as the behavior of the
 * {@link com.atlassian.event.api.EventPublisher} will then be the same  as the 'old' implementation.
 *
 * <p>One shouldn't rely on this implementation apart for backward compatibility reasons. This implementation will be
 * removed at the same time as the classes mentioned above.</p>
 */