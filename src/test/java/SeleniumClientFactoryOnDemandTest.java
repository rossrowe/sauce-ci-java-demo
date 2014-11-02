import com.saucelabs.selenium.client.factory.SeleniumFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SeleniumClientFactoryOnDemandTest {

    private WebDriver webDriver;

    /**
     * Create a WebDriver instance using Selenium Client Factory.  We don't have to refer to the environment
     * variables set by the CI plugin, as that's handled by the Selenium Client Factory logic.  We also don't have
     * to output the Sauce OnDemand Session id.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        webDriver = SeleniumFactory.createWebDriver();
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
    }

    /**
     *
     */
    @Test
    public void fullRun() throws Exception {
        webDriver.get("https://saucelabs.com/test/guinea-pig");
        assertEquals("I am a page title - Sauce Labs", webDriver.getTitle());
    }
}
