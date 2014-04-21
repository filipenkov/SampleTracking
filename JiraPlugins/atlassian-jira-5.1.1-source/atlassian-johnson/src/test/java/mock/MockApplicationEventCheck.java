package mock;

import com.atlassian.johnson.event.ApplicationEventCheck;
import com.atlassian.johnson.JohnsonEventContainer;

import javax.servlet.ServletContext;
import java.util.Map;

public class MockApplicationEventCheck implements ApplicationEventCheck
{
    public void check(JohnsonEventContainer eventContainer, ServletContext context)
    {

    }

    public void init(Map map)
    {

    }
}
