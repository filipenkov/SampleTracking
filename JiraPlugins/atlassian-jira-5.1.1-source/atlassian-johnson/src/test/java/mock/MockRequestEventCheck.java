package mock;

import com.atlassian.johnson.event.RequestEventCheck;
import com.atlassian.johnson.JohnsonEventContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class MockRequestEventCheck implements RequestEventCheck
{
    public void check(JohnsonEventContainer eventContainer, HttpServletRequest request)
    {

    }

    public void init(Map map)
    {

    }
}
