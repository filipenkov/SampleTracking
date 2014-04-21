package com.atlassian.support.tools.scheduler.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;

public class RenderingUtils {
	public static String render(TemplateRenderer renderer,  String template, Map<String,Object> map) throws RenderingException, IOException
	{
		Map<String, Object> context = new HashMap<String, Object>();
		context.putAll(map);

		StringWriter writer = new StringWriter();
		renderer.render(template, context, writer);

		return writer.toString();
	}

}
