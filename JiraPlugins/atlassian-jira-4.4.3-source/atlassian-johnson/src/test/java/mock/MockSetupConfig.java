package mock;

import com.atlassian.johnson.setup.SetupConfig;

import java.util.Map;

public class MockSetupConfig implements SetupConfig
{
    public static boolean IS_SETUP = false;

    public void init(Map map)
    {
    }

    public boolean isSetupPage(String uri)
    {
        return uri.indexOf("setup") >= 0;
    }

    public boolean isSetup()
    {
        return IS_SETUP;
    }
}
