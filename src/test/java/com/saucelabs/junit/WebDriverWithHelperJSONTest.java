package com.saucelabs.junit;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * This WebDriverWithHelperParametersTest modifies WebDriverWithHelperTest by adding JUnit
 * Parameterized runners and a three argument constructor to loop through an array and
 * run tests against all of the browser/platform combinations specified in the array.
 *
 * Each array element (browser/platform combination) runs as a new Sauce job that displays in
 * the Jenkins project's Sauce Jobs Report with pass/fail results. A job detail report with
 * video is provided for each of these Sauce jobs.
 *
 * The pass/fail result for each test is created via the <a href="https://github.com/saucelabs/sauce-java/tree/master/junit">Sauce JUnit</a> helper classes,
 * which use the Sauce REST API to mark each Sauce job (each test) as passed/failed.
 *
 * In order to use the {@link com.saucelabs.junit.SauceOnDemandTestWatcher} to see if the tests pass or fail
 * in the Sauce Jobs Report in your Jenkins projects, this test must implement the
 * {@link com.saucelabs.common.SauceOnDemandSessionIdProvider} interface as discussed in the code comments below.
 *
 * @author Ross Rowe
 * @author Bernie Cohen - modified to support parameterized testing against multiple environments
 */
@RunWith(Parameterized.class)
public class WebDriverWithHelperJSONTest implements SauceOnDemandSessionIdProvider {

    private WebDriver webDriver;
    private static DesiredCapabilities capabilities;
    private static Platform ANDROID, LINUX, MAC, UNIX, VISTA, WINDOWS, XP, platformValue;
    private String browser, browserVersion, platform, sessionId = "";


    // Create an array of available platforms from the "private static Platform" declaration above
    Platform[] platformValues = Platform.values();


    public Platform setPlatformCapabilities(String platformParam) {

        String platformVal = platformParam;

        for (int p=0; p<platformValues.length; p++) {
            platformValue = platformValues[p++];
            if (platformValue.toString() == platformVal) break;
        }

        return platformValue;

    }


    /**
     * Constructs a {@link com.saucelabs.common.SauceOnDemandAuthentication} instance using the supplied Sauce
     * user name and access key. To use the authentication supplied by environment variables or
     * from an external file, use the no-arg {@link com.saucelabs.common.SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();


    /**
     * JUnit Rule that marks Sauce Jobs as passed/failed when the test succeeds or fails.
     */
    public @Rule
    SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);


    /**
     * JUnit Rule that records the test name of the current test. When this is referenced
     * during the creation of {@link org.openqa.selenium.remote.DesiredCapabilities}, the test method name is assigned
     * to the Sauce Job name and recorded in Jenkins Console Output and in the Sauce Jobs
     * Report in the Jenkins project's home page.
     */
    public @Rule TestName testName = new TestName();


    /**
     * JUnit annotation that runs each test once for each item in a Collection.
     *
     * Feel free to add as many additional parameters as you like to the capabilitiesParams array.
     *
     * Note: If you add parameters for the MAC platform, make sure that you have Mac minutes in
     * your <a href="https://saucelabs.com/login">Sauce account</a> or the test will fail.
     */
    @Parameters
    public static Collection<Object[]> data() {
                                             
        String json = System.getenv("SAUCE_ONDEMAND_BROWSERS");
        if (json == null) {
			   json = System.getenv("bamboo_SAUCE_ONDEMAND_BROWSERS");  
		} 
        List<Object[]> browsers = new ArrayList<Object[]>();
        JSONArray browserArray = null;
        try {
            browserArray = new JSONArray(json);
            for (int i =0;i<browserArray.length();i++) {
                JSONObject browserJSON = browserArray.getJSONObject(i);
                browsers.add(new Object[]{browserJSON.get("browser"), browserJSON.get("browser-version"), browserJSON.get("os")});

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return browsers;

    }


    public WebDriverWithHelperJSONTest(String s1, String s2, String s3) {
        browser = s1;
        browserVersion = s2;
        platform = s3;
    }


    /**
     * Creates a new {@link org.openqa.selenium.remote.RemoteWebDriver} instance that is used to run WebDriver tests
     * using Sauce.
     *
     * @throws Exception thrown if an error occurs constructing the WebDriver
     */
    @Test
    public void validateTitle() throws Exception {

        capabilities = new DesiredCapabilities(browser, browserVersion, setPlatformCapabilities(platform));
        capabilities.setCapability("name", this.getClass().getName() + "." + testName.getMethodName());
        this.webDriver = new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
        this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();

        if (browserVersion == "") browserVersion = "unspecified";
        String browserName = String.format("%-19s", browser).replaceAll(" ", ".").replaceFirst("[.]", " ");
        String browserVer = String.format("%-19s", browserVersion).replaceAll(" ", ".");
        System.out.println("@Test validateTitle() testing browser/version: " + browserName + browserVer + "platform: " + platform);

        webDriver.get("https://saucelabs.com/test/guinea-pig");
        assertEquals("I am a page title - Sauce Labs", webDriver.getTitle());

        webDriver.quit();
    }


    @Override
    public String getSessionId() {
            return sessionId;
    }

}