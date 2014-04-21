package com.atlassian.labs.hipchat;

import com.atlassian.labs.hipchat.components.ConfigurationManager;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseStatusException;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.httpclient.HttpStatus;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

public class HipChatApiClient {
    public static enum BackgroundColour {
        YELLOW("yellow"),
        RED("red"),
        GREEN("green"),
        PURPLE("purple");

        public String value;

        BackgroundColour(String value) {
            this.value = value;
        }
    }

    private static String API_BASE_URL = "https://api.hipchat.com";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ConfigurationManager configurationManager;
    private RequestFactory<Request<?, Response>> requestFactory;

    public HipChatApiClient(ConfigurationManager configurationManager, RequestFactory<Request<?, Response>> requestFactory) {
        this.configurationManager = configurationManager;
        this.requestFactory = requestFactory;
    }

    public boolean isAuthTokenValid(String token) throws ResponseException {
        if (Strings.isNullOrEmpty(token)) {
            return false;
        }

        try {
            token = URLEncoder.encode(token, Charsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
           throw new RuntimeException(e);
        }

        String url = API_BASE_URL + "/v1/rooms/list?auth_token=" + token;
        Request<?, Response> request = requestFactory.createRequest(Request.MethodType.GET, url);

        try {
            request.execute();
        } catch (ResponseStatusException e) {
            if (e.getResponse().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                return false;
            }
            throw e;
        }

        return true;
    }

    public Collection<Room> getRooms() throws ResponseException {
        Preconditions.checkState(!Strings.isNullOrEmpty(configurationManager.getHipChatApiToken()), "The HipCHat API OAuth token can not be empty");

        try {
            String url = API_BASE_URL + "/v1/rooms/list?auth_token=" + URLEncoder.encode(configurationManager.getHipChatApiToken(), Charsets.UTF_8.toString());
            Request<?, Response> request = requestFactory.createRequest(Request.MethodType.GET, url);

            JsonParser jsonParser = OBJECT_MAPPER.getJsonFactory().createJsonParser(request.execute());

            // skip root node and go to the rooms array directly
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                if ("rooms".equals(jsonParser.getCurrentName())) {
                    jsonParser.nextValue();
                    return jsonParser.<List<Room>>readValueAs(new TypeReference<List<Room>>() {
                    });
                }
            }
        } catch (IOException e) {
            throw new ResponseException(e);
        }

        throw new ResponseException("Unable to parse API response, can not find the rooms JSON property");
    }

    public void notifyRoom(String room, String msg) throws ResponseException {
        notifyRoom(room, msg, BackgroundColour.YELLOW);
    }

    public void notifyRoom(String room, String msg, BackgroundColour colour) throws ResponseException {
        Preconditions.checkState(!Strings.isNullOrEmpty(configurationManager.getHipChatApiToken()), "The HipCHat API OAuth token can not be empty");

        String url = API_BASE_URL + "/v1/rooms/message";
        Request<?, Response> request = requestFactory.createRequest(Request.MethodType.POST, url);
        request.addRequestParameters(
                "auth_token", configurationManager.getHipChatApiToken(),
                "room_id", room,
                "from", "JIRA",
                "message", msg,
                "format", "json"
        );
        if (colour != null) {
            request.addRequestParameters("color", colour.value);
        }
        request.execute();
    }

    /**
     * A simple mapping class for HipCHat room. There are only needed properties.
     */
    public static class Room {

        private final Long roomId;
        private final String name;

        @JsonCreator
        public Room(@JsonProperty("room_id") Long roomId, @JsonProperty("name") String name) {
            this.roomId = roomId;
            this.name = name;
        }

        public Long getRoomId() {
            return roomId;
        }

        public String getName() {
            return name;
        }
    }
}