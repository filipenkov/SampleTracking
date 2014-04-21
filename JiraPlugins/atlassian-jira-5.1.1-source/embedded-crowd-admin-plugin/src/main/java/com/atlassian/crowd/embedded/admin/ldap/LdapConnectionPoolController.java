package com.atlassian.crowd.embedded.admin.ldap;

import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.impl.DefaultConnectionPoolProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class LdapConnectionPoolController extends SimpleFormController
{
    private CrowdDirectoryService crowdDirectoryService;
    private TransactionTemplate transactionTemplate;
    private I18nResolver i18nResolver;
    private HtmlEncoder htmlEncoder;

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        return crowdDirectoryService.getStoredConnectionPoolProperties();
    }

    public void setCrowdDirectoryService(CrowdDirectoryService crowdDirectoryService)
    {
        this.crowdDirectoryService = crowdDirectoryService;
    }

    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("systemPoolProperties", crowdDirectoryService.getSystemConnectionPoolProperties());
        model.put("htmlEncoder", htmlEncoder);
        return model;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception
    {
        saveLdapProperties((ConnectionPoolProperties) command);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("saveSuccessful", true);
        model.put("htmlEncoder", htmlEncoder);
        return showForm(request, response, errors, model);
    }

    private void saveLdapProperties(final ConnectionPoolProperties poolProperties)
    {
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction()
            {
                crowdDirectoryService.setConnectionPoolProperties(poolProperties);
                return null;
            }
        });
    }

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception
    {
        if (errors.hasErrors())
        {
            DefaultConnectionPoolProperties properties = (DefaultConnectionPoolProperties) command;

            for (Object error : errors.getFieldErrors())
            {
                FieldError fieldError = (FieldError) error;

                // We only validate for validity of supported protocol and authentication
                // If an invalid value was entered, revert back to the original value obtained from the database
                if (fieldError.getField().equals("supportedProtocol"))
                {
                    properties.setSupportedProtocol(crowdDirectoryService.getStoredConnectionPoolProperties().getSupportedProtocol());
                }

                if (fieldError.getField().equals("supportedAuthentication"))
                {
                    properties.setSupportedAuthentication(crowdDirectoryService.getStoredConnectionPoolProperties().getSupportedAuthentication());
                }

            }

            errors.addError(new ObjectError("poolProperties", i18nResolver.getText("embedded.crowd.connection.pool.save.fail")));
        }
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate)
    {
        this.transactionTemplate = transactionTemplate;
    }

    public void setI18nResolver(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    public void setHtmlEncoder(HtmlEncoder htmlEncoder)
    {
        this.htmlEncoder = htmlEncoder;
    }
}
