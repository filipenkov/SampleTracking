/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 5, 2002
 * Time: 5:40:10 PM
 * To change this template use Options | File Templates.
 */
package mock.atlassian.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimpleJob implements Job
{
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        System.out.println("SimpleJob.execute");
    }
}
