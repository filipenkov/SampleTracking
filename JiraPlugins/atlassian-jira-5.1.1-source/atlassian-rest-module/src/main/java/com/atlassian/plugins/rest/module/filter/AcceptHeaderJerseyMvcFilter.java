package com.atlassian.plugins.rest.module.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.HashSet;
import java.util.Set;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;

/**
 * <p>This is a filter to "fix" user agents with broken {@code Accept} headers.</p> <p>When a client accepts {@code
 * text/html} we want it to be first in the header. This is because, it is likely that it is a browser and we then
 * believe that HTML is the best view to render.</p> <p>This was introduced specifically to make Jersey MVC work with
 * webkit based browsers which by default have their accept header set to {@code application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*\/*;q=0.5}</p>
 * <p>The "fix" is to prepend {@code text/html} content type in the {@code Accept} header if it exists anywhere (else)
 * in the header.</p>
 * <p/>
 * <p>When a client accepts {@link MediaType#WILDCARD} (IE) we want to prepend it with {@code text/html}.
 */
@Provider
public class AcceptHeaderJerseyMvcFilter implements ContainerRequestFilter
{
    static final Set<String> ACCEPTED_CONTENT_TYPES = new HashSet<String>()
    {{
            add(TEXT_PLAIN);
            add(TEXT_HTML);
            add(TEXT_HTML);
            add(APPLICATION_JSON);
            add(APPLICATION_XML);
            add(APPLICATION_ATOM_XML);
        }};


    public ContainerRequest filter(final ContainerRequest request)
    {
        final MultivaluedMap<String, String> requestHeaders = request.getRequestHeaders();
        final String acceptHeader = requestHeaders.getFirst(ACCEPT); // there should only be one Accept header

        String fixedHeader = addAppXmlWhenWildcardOnly(acceptHeader);
        fixedHeader = moveTextHtmlToFront(fixedHeader);

        if (acceptHeader != null && !acceptHeader.equals(fixedHeader))
        {
            requestHeaders.putSingle(ACCEPT, fixedHeader);
        }

        return request;
    }

    private String addAppXmlWhenWildcardOnly(final String acceptHeader)
    {
        if (StringUtils.contains(acceptHeader, WILDCARD))
        {
            for (String contentType : ACCEPTED_CONTENT_TYPES)
            {
                if (StringUtils.contains(acceptHeader, contentType))
                {
                    return acceptHeader;
                }
            }
            return APPLICATION_XML + "," + acceptHeader;
        }
        return acceptHeader;
    }

    private String moveTextHtmlToFront(final String acceptHeader)
    {
        if ((StringUtils.contains(acceptHeader, TEXT_HTML) || StringUtils.contains(acceptHeader, WILDCARD))
                && !StringUtils.startsWith(acceptHeader, TEXT_HTML))
        {
            return TEXT_HTML + "," + acceptHeader;
        }
        return acceptHeader;
    }
}
