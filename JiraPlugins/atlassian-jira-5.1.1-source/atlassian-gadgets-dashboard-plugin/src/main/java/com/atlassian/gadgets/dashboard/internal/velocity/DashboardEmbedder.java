package com.atlassian.gadgets.dashboard.internal.velocity;

import java.util.List;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.rest.representations.DashboardRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.GadgetRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.internal.rest.representations.UserPrefRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.UserPrefRepresentation.EnumValueRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.UserPrefsRepresentation;
import com.atlassian.gadgets.dashboard.internal.util.JavaScript;
import com.atlassian.templaterenderer.annotations.HtmlSafe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility for embedding dashboard state information in HTML as JSON.
 */
public class DashboardEmbedder
{
    private final RepresentationFactory representationFactory;
    
    public DashboardEmbedder(RepresentationFactory representationFactory)
    {
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
    }
    
    @HtmlSafe
    public String json(Dashboard dashboard, GadgetRequestContext gadgetRequestContext, boolean writable)
    {
        DashboardRepresentation representation = representationFactory.createDashboardRepresentation(dashboard, gadgetRequestContext, writable);
        JSONObject json = new JSONObject();
        try
        {
            put(json, "id", representation.getId());
            put(json, "title", representation.getTitle());
            put(json, "writable", writable);
            put(json, "layout", representation.getLayout().toString());
            if (!representation.getGadgets().isEmpty())
            {
                put(json, "gadgets", gadgetsToJsonArray(representation.getGadgets()));
            }
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
        return json.toString();
    }

    private JSONArray gadgetsToJsonArray(List<GadgetRepresentation> gadgets) throws JSONException
    {
        JSONArray array = new JSONArray();
        for (GadgetRepresentation gadget : gadgets)
        {
            array.put(gadgetToJsonObject(gadget));
        }
        return array;
    }

    private JSONObject gadgetToJsonObject(GadgetRepresentation gadget) throws JSONException
    {
        JSONObject json = new JSONObject();
        put(json, "id", gadget.getId());
        put(json, "title", gadget.getTitle());
        put(json, "titleUrl", gadget.getTitleUrl());
        put(json, "gadgetSpecUrl", gadget.getGadgetSpecUrl());
        put(json, "height", gadget.getHeight());
        put(json, "width", gadget.getWidth());
        put(json, "color", gadget.getColor());
        put(json, "column", gadget.getColumn());
        put(json, "colorUrl", gadget.getColorUrl());
        put(json, "gadgetUrl", gadget.getGadgetUrl());
        put(json, "isMaximizable", gadget.isMaximizable());
        put(json, "renderedGadgetUrl", gadget.getRenderedGadgetUrl());
        put(json, "hasNonHiddenUserPrefs", gadget.getHasNonHiddenUserPrefs());
        put(json, "userPrefs", userPrefsToJsonObject(gadget.getUserPrefs()));
        put(json, "loaded", gadget.isLoaded());
        put(json, "errorMessage", gadget.getErrorMessage());
        return json;
    }

    private JSONObject userPrefsToJsonObject(UserPrefsRepresentation userPrefs) throws JSONException
    {
        if (userPrefs == null)
        {
            return null;
        }
        JSONObject json = new JSONObject();
        put(json, "action", userPrefs.getAction());
        put(json, "fields", fieldsToJsonArray(userPrefs.getFields()));
        return json;
    }

    private JSONArray fieldsToJsonArray(List<UserPrefRepresentation> fields) throws JSONException
    {
        JSONArray array = new JSONArray();
        for (UserPrefRepresentation userPref : fields)
        {
            array.put(userPrefToJsonObject(userPref));
        }
        return array;
    }

    private JSONObject userPrefToJsonObject(UserPrefRepresentation userPref) throws JSONException
    {
        JSONObject json = new JSONObject();
        put(json, "name", userPref.getName());
        put(json, "value", userPref.getValue());
        put(json, "type", userPref.getType());
        put(json, "displayName", userPref.getDisplayName());
        put(json, "required", userPref.isRequired());
        if (!userPref.getOptions().isEmpty())
        {
            put(json, "options", optionsToJsonArray(userPref.getOptions()));
        }
        return json;
    }

    private JSONArray optionsToJsonArray(List<EnumValueRepresentation> options) throws JSONException
    {
        JSONArray array = new JSONArray();
        for (EnumValueRepresentation option : options)
        {
            array.put(optionToJsonObject(option));
        }
        return array;
    }

    private JSONObject optionToJsonObject(EnumValueRepresentation option) throws JSONException
    {
        JSONObject json = new JSONObject();
        put(json, "value", option.getValue());
        put(json, "label", option.getLabel());
        put(json, "selected", option.isSelected());
        return json;
    }
    
    private void put(JSONObject json, String key, String value) throws JSONException
    {
        if (value == null)
        {
            return;
        }
        json.put(key, escape(value));
    }
    
    private void put(JSONObject json, String key, boolean value) throws JSONException
    {
        json.put(key, value);
    }
    
    private void put(JSONObject json, String key, Boolean value) throws JSONException
    {
        if (value == null)
        {
            return;
        }
        json.put(key, value);
    }
    
    private void put(JSONObject json, String key, JSONArray array) throws JSONException
    {
        if (array == null)
        {
            return;
        }
        json.put(key, array);
    }
    
    private void put(JSONObject json, String key, JSONObject value) throws JSONException
    {
        if (value == null)
        {
            return;
        }
        json.put(key, value);
    }
    
    private void put(JSONObject json, String key, Integer value) throws JSONException
    {
        if (value == null)
        {
            return;
        }
        json.put(key, value);
    }
    
    /**
     * Puts an empty string if {@code color} is {@code null}, {@code color.toString} otherwise.
     * 
     * Note: ZParse blows up if the color attribute isn't present and I'm not sure how to make it not blow up.  If we
     *       can figure that out then this can be changed to omit the color attribute completely if the value is null. 
     */
    private void put (JSONObject json, String key, Color color) throws JSONException
    {
        json.put(key, color == null ? "" : color.toString());
    }

    /**
     * Escapes json string values to handle unicode and the '&lt;' character for embedding in html.
     */
    private JSONString escape(final String str)
    {
        return new EscapedJsonString(str);
    }
    
    private static final class EscapedJsonString implements JSONString
    {
        private final String str;
        
        public EscapedJsonString(String str)
        {
            this.str = str;
        }
        
        public String toJSONString()
        {
            return '"' + JavaScript.escape(str) + '"';
        }
    }
}
