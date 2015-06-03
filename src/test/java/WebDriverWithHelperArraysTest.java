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
 * This WebDriverWithHelperArraysTest modifies WebDriverWithHelperTest to run the same tests
 * against many different browser/platform combinations by looping though an array. Unlike
 * WebDriverWithHelperParametersTest, which creates a separate Sauce job with a pass/fail result
 * for each element (browser/platform) in the array, this WebDriverWithHelperArraysTest creates
 * one Sauce job with one pass/fail result for the whole array.
 *
 * A detailed Sauce job report and video are only available for the last element in the array
 * with this WebDriverWithHelperArraysTest. If you want these reports for each element in the
 * array, see WebDriverWithHelperParametersTest.
 *
 * The pass/fail result displays in the Jenkins project's Sauce Jobs Report via <a href="https://github.com/saucelabs/sauce-java/tree/master/junit">Sauce JUnit</a>
 * helper classes, which use the Sauce REST API to mark each Sauce Job as passed/failed.
 *
 * In order to use the {@link SauceOnDemandTestWatcher} to see if the tests pass or fail
 * in the Sauce Jobs Report in your Jenkins projects, this test must implement the
 * {@link SauceOnDemandSessionIdProvider} interface as discussed in the code comments below.
 *
 * @author Ross Rowe
 * @author Bernie Cohen - modified to support testing against multiple environments by looping through an array

 */
public class WebDriverWithHelperArraysTest implements SauceOnDemandSessionIdProvider {

    private WebDriver webDriver;
    private String sessionId;
    private boolean done = false;
    private static DesiredCapabilities capabilities;
    private static Platform ANDROID, LINUX, MAC, UNIX, VISTA, WINDOWS, XP;
    private static Platform platformValue;
    private String browser, browserVersion, platform = "";
    private String lastSessionId;


    String[] browserArray = { "android", "chrome", "firefox", "htmlUnit", "internet explorer",
                              "ipad", "iphone", "opera", "safari" };


    // Create an array of available platforms from the "private static Platform" declaration above
    Platform[] platformValues = Platform.values();


    String[][] capabilitiesParams = {   { "chrome", "", "ANDROID" },
                                        { "firefox", "18.0.2", "LINUX" },
                                        { "firefox", "19.0", "LINUX" },
                                        { "firefox", "17.0.1", "WINDOWS" },
                                        { "firefox", "18.0.2", "WINDOWS" },
                                        { "firefox", "19.0", "WINDOWS" },
                                        { "firefox", "20.0", "WINDOWS" },
                                        { "firefox", "17.0.1", "XP" },
                                        { "firefox", "18.0.2", "XP" },
                                        { "firefox", "19.0", "XP" },
                                        { "firefox", "20.0", "XP" },
                                        { "internet explorer", "8.0.6001.18702", "XP" },
                                        { "opera", "12.12", "XP" },
                                        { "opera", "12.12", "WINDOWS" },                                        { "opera", "12.12", "WINDOWS" },
                                    };


    public Platform setPlatformCapabilities(String platformParam) {

        String platformVal = platformParam;

        for (int p=0; p<platformValues.length; p++) {
            platformValue = platformValues[p++];
            if (platformValue.toString() == platformVal) break;
        }

        return platformValue;

    }


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

        for (int i=0; i<capabilitiesParams.length; i++) {
            for (int j=0; ; ) {
                browser = capabilitiesParams[i][j];
                browserVersion = capabilitiesParams[i][++j];
                platform = capabilitiesParams[i][++j];
                break;
            }

            capabilities = new DesiredCapabilities(browser, browserVersion, setPlatformCapabilities(platform));
            capabilities.setCapability("name", this.getClass().getName() + "." + testName.getMethodName());
            this.webDriver = new RemoteWebDriver(
                    new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                    capabilities);
            this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();

            validateTitle();
            tearDown();

        }

        this.getSessionId();
        done = true;

    }

    @Test
    @Ignore
    public void validateTitle() throws Exception {
        if (!done) {
            try {

                if (browserVersion == "") browserVersion = "unspecified";
                if (browserVersion == null) browserVersion = "unspecified";
                String browserName = String.format("%-19s", browser).replaceAll(" ", ".").replaceFirst("[.]", " ");
                String browserVer = String.format("%-19s", browserVersion).replaceAll(" ", ".");
                System.out.println("@Test validateTitle() testing browser/version: " + browserName + browserVer + "platform: " + platform);

                webDriver.get("https://saucelabs.com/test/guinea-pig");
                assertEquals("I am a page title - Sauce Labs", webDriver.getTitle());

            } catch (Exception e) {
                System.err.println("Caught Exception in @Test validateTitle() testing browser/browser version: " + browser + " " + browserVersion + "   platform: " + platform + e.getMessage());
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!done) {
            webDriver.quit();
        }
    }

    @Override
    public String getSessionId() {
        if (!done) {
            lastSessionId = sessionId;
            return sessionId;
        } else {
            return lastSessionId;
        }
    }

}