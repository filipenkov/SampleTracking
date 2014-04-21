package it.common;

public class TestHomePage extends AbstractSupportTestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	public void testInternationalization() {
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();
		assertFalse("Text contains unescaped i18n string", this.client.getBodyText().contains("stp.properties"));
	}

	public void testVelocityOutput() {
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();
		assertFalse("Text contains $entry.value", this.client.getBodyText().contains("$entry.value"));
	}
}
