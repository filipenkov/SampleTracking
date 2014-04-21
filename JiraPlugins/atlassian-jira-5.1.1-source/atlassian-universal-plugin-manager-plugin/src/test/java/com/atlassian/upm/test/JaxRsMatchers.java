package com.atlassian.upm.test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class JaxRsMatchers
{
    private JaxRsMatchers()
    {
    }

    public static Matcher<? super Response> ok()
    {
        return new ResponseStatusMatcher(Status.OK);
    }

    public static Matcher<? super Response> created()
    {
        return new ResponseStatusMatcher(Status.CREATED);
    }

    public static Matcher<? super Response> accepted()
    {
        return new ResponseStatusMatcher(Status.ACCEPTED);
    }

    public static Matcher<? super Response> noContent()
    {
        return new ResponseStatusMatcher(Status.NO_CONTENT);
    }

    public static Matcher<? super Response> notFound()
    {
        return new ResponseStatusMatcher(Status.NOT_FOUND);
    }

    public static Matcher<? super Response> notModified()
    {
        return new ResponseStatusMatcher(Status.NOT_MODIFIED);
    }

    public static Matcher<? super Response> badRequest()
    {
        return new ResponseStatusMatcher(Status.BAD_REQUEST);
    }

    public static Matcher<? super Response> unauthorized()
    {
        return new ResponseStatusMatcher(Status.UNAUTHORIZED);
    }

    public static Matcher<? super Response> internalError()
    {
        return new ResponseStatusMatcher(Status.INTERNAL_SERVER_ERROR);
    }

    private static final class ResponseStatusMatcher extends TypeSafeDiagnosingMatcher<Response>
    {
        private final Status expectedStatus;

        private ResponseStatusMatcher(Status expectedStatus)
        {
            this.expectedStatus = expectedStatus;
        }

        @Override
        protected boolean matchesSafely(Response response, Description mismatchDescription)
        {
            if (response.getStatus() != expectedStatus.getStatusCode())
            {
                mismatchDescription.appendText("status was ").appendValue(Status.fromStatusCode(response.getStatus()));
                return false;
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText("status of ").appendValue(expectedStatus);
        }
    }
}
