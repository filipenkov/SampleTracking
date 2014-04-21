package com.atlassian.jira.template;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.util.InjectableComponent;
import org.apache.velocity.VelocityContext;

import java.util.Map;

/**
 * Represents a fluent, easy-to-use façade over the {@link org.apache.velocity.app.VelocityEngine} used by the JIRA web
 * application.
 *
 * <h3>Usage</h3>
 *     <h4>File Templates</h4>
 *     <ul>
 *     <li>To render a file template, applying a map of parameters as html: <br/>
 *     {@code engine.render(file("path/to/file")).applying(parameters).asHtml()}
 *     </li>
 *     <li>if there are no params to the template you can omit the applying call: <br/>
 *     {@code engine.render(file("path/to/file")).asHtml()}
 *           </li>
 *     <li>To render the template as plain text: <br/>
 *     {@code engine.render(file("path/to/file")).asPlainText()}
 *           </li>
 *     </ul>
 *     <h4>Fragments</h4>
 *     <ul>
 *     <li>
 *     To render a vtl fragment stored in a string, applying a map of parameters as html: <br/>
 *     {@code engine.render(fragment("vtl-fragment")).applying(parameters).asHtml()}
 *     </li>
 *     <li>
 *     if there are no params to the template you can omit the applying call: <br/>
 *     {@code engine.render(fragment("vtl-fragment")).asHtml()}
 *     </li>
 *     <li>
 *     To render the template as plain text: <br/>
 *     {@code engine.render(file("path/to/file")).asPlainText()}
 *     </li>
 *     </ul>
 * @since v5.1
 */
@ExperimentalApi
@InjectableComponent
public interface VelocityTemplatingEngine
{
    public RenderRequest render(final TemplateSource source);

    @ExperimentalApi
    public interface RenderRequest
    {
        public String asPlainText();

        public String asHtml();

        public RenderRequest applying(Map<String, Object> parameters);

        public RenderRequest applying(VelocityContext context);
    }

}
