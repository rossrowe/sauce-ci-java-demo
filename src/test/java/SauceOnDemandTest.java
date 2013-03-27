import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SauceOnDemandTest {

    private WebDriver webDriver;

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        String version = Utils.readPropertyOrEnv("SELENIUM_VERSION", "17");
        if (!version.equals(""))
            capabilities.setCapability("version", version);
        capabilities.setCapability("platform", Utils.readPropertyOrEnv("SELENIUM_PLATFORM", "XP"));
        capabilities.setCapability("browserName", Utils.readPropertyOrEnv("SELENIUM_BROWSER", "firefox"));
        String username = Utils.readPropertyOrEnv("SAUCE_USER_NAME", "rossco_9_9");
        String accessKey = Utils.readPropertyOrEnv("SAUCE_API_KEY", "44f0744c-1689-4418-af63-560303cbb37b");
        this.webDriver = new RemoteWebDriver(
                new URL("http://" + username + ":" + accessKey + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
    }

    /**
     *
     */
    @Test
    public void basic() throws Exception {

        String sessionId = ((RemoteWebDriver) webDriver).getSessionId().toString();
        System.out.println("SauceOnDemandSessionID=" + sessionId + " job-name=basic");
        webDriver.get("http://www.amazon.com/");
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more", webDriver.getTitle());

    }

}
