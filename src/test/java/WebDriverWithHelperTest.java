import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import org.junit.*;
import org.junit.rules.TestName;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.junit.Assert.assertEquals;


/**
 * This WebDriverWithHelperTest test shows you how to run your Selenium tests with
 * <a href="http://saucelabs.com/ondemand">Sauce OnDemand</a>.
 *
 * This test uses {@link RemoteWebDriver} and also includes the <a href="https://github.com/saucelabs/sauce-java/tree/master/junit">Sauce JUnit</a>
 * helper classes, which use the Sauce REST API to mark each Sauce Job (each test) as passed/failed.
 *
 * In order to use the {@link SauceOnDemandTestWatcher} to see if the tests pass or fail
 * in the Sauce Jobs Report in your Jenkins projects, each test must implement the
 * {@link SauceOnDemandSessionIdProvider} interface as discussed in the code comments below.
 *
 * @author Ross Rowe
 */
public class WebDriverWithHelperTest implements SauceOnDemandSessionIdProvider {

    private WebDriver webDriver;
    private String sessionId;

    /**
     * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied Sauce
     * user name and access key. To use the authentication supplied by environment variables or
     * from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

    /**
     * JUnit Rule which marks Sauce Jobs as passed/failed when the test succeeds or fails.
     */
    public @Rule
    SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    /**
     * JUnit Rule that records the test name of the current test. When this is referenced
     * during the creation of {@link DesiredCapabilities}, the test method name is assigned
     * to the Sauce Job name and recorded in Jenkins Console Output and in the Sauce Jobs
     * Report in the Jenkins project's home page.
     */
    public @Rule TestName testName = new TestName();


    /**
     * Creates a new {@link RemoteWebDriver} instance to be used to run WebDriver tests
     * using Sauce.
     *
     * @throws Exception thrown if an error occurs constructing the WebDriver
     */
    @Before
    public void setUp() throws Exception {

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("version", "17.0.1");
        capabilities.setCapability("platform", Platform.XP);
        capabilities.setCapability("name", this.getClass().getName() + "." + testName.getMethodName());
        this.webDriver = new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
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
