package com.atlassian.streams.spi;

import java.net.URI;

import com.atlassian.streams.api.StreamsException;
import com.atlassian.streams.api.common.Either;
import com.atlassian.streams.api.common.Option;

import static com.atlassian.streams.api.common.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Interface for handling comments on streams items
 */
public interface StreamsCommentHandler
{
    /**
     * Post a reply to an activity item
     *
     * @param itemPath path of the item to comment on
     * @param comment The comment for the item
     * @return An {@code Either} containing an error or the URL of the newly added comment
     * @throws StreamsException If an error occured, such as if the item doesn't exist, or the user doesn't have
     * permission to reply to that item
     */
    Either<PostReplyError, URI> postReply(Iterable<String> itemPath, String comment);

    static class PostReplyError
    {
        final Type type;
        final Option<Throwable> cause;

        public PostReplyError(Type type)
        {
            this(type, null);
        }

        public PostReplyError(Type type, Throwable cause)
        {
            this.type = checkNotNull(type, "type");
            this.cause = option(cause);
        }

        public Type getType()
        {
            return type;
        }

        public Option<Throwable> getCause()
        {
            return cause;
        }

        public enum Type
        {
            DELETED_OR_PERMISSION_DENIED(404, "comment.deleted.or.denied"),
            UNAUTHORIZED(401, "unauthorized"),
            FORBIDDEN(403, "forbidden"),
            CONFLICT(409, "conflict"),
            REMOTE_POST_REPLY_ERROR(500, "remote.error"),
            UNKNOWN_ERROR(500, "unknown.error");

            private final int statusCode;
            private final String subCode;

            Type(int statusCode, String subCode)
            {
                this.statusCode = statusCode;
                this.subCode = "streams.comment.action." + subCode;
            }

            public int getStatusCode()
            {
                return statusCode;
            }

            public String getSubCode()
            {
                return subCode;
            }

            public String asJsonString()
            {
                return "{\"subCode\" : \"" + subCode + "\"}";
            }


        }
    }
}
