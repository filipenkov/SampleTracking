package com.atlassian.jira.web.action.admin.vcs;

import alt.java.io.FileImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.LockException;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepository;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.vcs.cvsimpl.ValidationException;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import net.sf.statcvs.input.LogSyntaxException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.IOException;

@WebSudoRequired
public class RepositoryTest extends RepositoryActionSupport
{
    private String message;

    public RepositoryTest(RepositoryManager repositoryManager, CvsRepositoryUtil cvsRepositoryUtil)
    {
        super(repositoryManager, cvsRepositoryUtil);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getId() == null)
        {
            message = "Please specifiy a Repository to test.";
        }
        else
        {
            Repository repository = null;
            try
            {
                repository = getRepositoryManager().getRepository(getId());
                if (repository == null)
                {
                    message = "Could not retrieve repository with id '" + getId() + "'.";
                    return getResult();
                }
            }
            catch (GenericEntityException e)
            {
                message = "Error occurred while retrieving repository with id '" + getId() + "'.";
                return getResult();
            }

            if (RepositoryManager.CVS_TYPE.equals(repository.getType()))
            {
                CvsRepository cvsRepository = (CvsRepository) repository;
                try
                {
                    getCvsRepositoryUtil().checkLogFilePath(new FileImpl(cvsRepository.getCvsLogFilePath()), cvsRepository.fetchLog());
                    getCvsRepositoryUtil().checkCvsRoot(cvsRepository.getCvsRoot());
                }
                catch (ValidationException e)
                {
                    message = "Problem with repository\n" + ExceptionUtils.getStackTrace(e);
                    return getResult();
                }

                try
                {
                    testRepository(cvsRepository.getName(), cvsRepository.getCvsLogFilePath(), cvsRepository.getCvsRoot(), cvsRepository.getModuleName(), cvsRepository.getPassword(), cvsRepository.getCvsTimeout(), cvsRepository.fetchLog());
                }
                catch (AuthenticationException e)
                {
                    final String errorMessage = "Error authenticating with the repository.";
                    log.error(errorMessage, e);
                    Throwable cause = e.getUnderlyingThrowable();
                    if (cause != null)
                        log.error("Caused by: "+cause.getMessage(), cause);

                    message = errorMessage + "\n" + ExceptionUtils.getStackTrace(e) + (cause != null ? "\n" + ExceptionUtils.getStackTrace(cause) : "");
                }
                catch (LogSyntaxException e)
                {
                    final String errorMessage = "Error while parsing cvs log.";
                    log.error(errorMessage, e);
                    message = errorMessage + "\n" + ExceptionUtils.getStackTrace(e);
                }
                catch (LockException e)
                {
                    // Get the first message of the exception as it is a NestableException
                    final String errorMessage = "Error obtaing lock.";
                    log.error(errorMessage, e);
                    message = errorMessage + "\n" + ExceptionUtils.getStackTrace(e);
                }
                catch (IOException e)
                {
                    final String errorMessage = e.getMessage();
                    log.error(errorMessage, e);
                    message = errorMessage + "\n" + ExceptionUtils.getStackTrace(e);
                }
                catch (CommandException e)
                {
                    final String errorMessage = "Error occured while retrieving cvs log.";
                    log.error(errorMessage, e);
                    message = errorMessage + "\n" + ExceptionUtils.getStackTrace(e);
                }
                catch (Throwable t)
                {
                    final String errorMEssage = "Error occurred while obtaining cvs or parsing the cvs log.";
                    log.error(errorMEssage, t);
                    message = errorMEssage + "\n" + ExceptionUtils.getStackTrace(t);
                }

                if (message != null)
                {
                    return getResult();
                }
            }
            else
            {
                message = "Unknown repository type.";
            }
        }

        return super.doExecute();
    }


    public String getMessage()
    {
        return message;
    }
}