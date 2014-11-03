import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import org.junit.*;
import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.junit.Assert.assertEquals;


/**
 * This SauceOnDemandTest shows you how to run your test code on the Sauce cloud of Selenium
 * servers using the environment variables that were created when you configured for the Sauce
 * plugin for Jenkins.
 *
 * For discussions about SauceOnDemandSessionIdProvider, SauceOnDemandAuthentication,
 * SauceOnDemandTestWatcher, @Rule and TestName, see the comments in WebDriverWithHelperTest.java.
 *
 * @author Ross Rowe
 */
public class SauceOnDemandTest implements SauceOnDemandSessionIdProvider {

    private WebDriver webDriver;
    private String sessionId;

    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

    public @Rule
    SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    public @Rule TestName testName = new TestName();


   /**
     * Creates a new {@link RemoteWebDriver} instance used to run WebDriver tests using
     * Sauce.
     *
     * @throws Exception thrown if an error occurs constructing the WebDriver
     */
    @Before
    public void setUp() throws Exception {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        String version = Utils.readPropertyOrEnv("SELENIUM_VERSION", "");
        if (!version.equals("")) {
            capabilities.setCapability("version", version);
        }
        capabilities.setCapability("platform", Utils.readPropertyOrEnv("SELENIUM_PLATFORM", "XP"));
        capabilities.setCapability("browserName", Utils.readPropertyOrEnv("SELENIUM_BROWSER", "firefox"));
        String username = Utils.readPropertyOrEnv("SAUCE_USER_NAME", "");
        String accessKey = Utils.readPropertyOrEnv("SAUCE_API_KEY", "");
        this.webDriver = new RemoteWebDriver(
                new URL("http://" + username + ":" + accessKey + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
        this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();

    }

    @Test
    public void validateTitle() throws Exception {
        webDriver.get("https://saucelabs.com/test/guinea-pig");
        assertEquals("I am a page title - Sauce Labs", webDriver.getTitle());
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

}
