import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SauceOnDemandTest {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=300&os=windows 2008&browser=firefox&browser-version=4.";
    private int code;
    private WebDriver selenium;

    @Before
    public void setUp() throws Exception {
        String driver = System.getenv("SELENIUM_DRIVER");
        if (driver == null || driver.equals("")) {
            System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        }

        System.setProperty("SELENIUM_STARTING_URL", "http://www.amazon.com/");
        //selenium = SeleniumFactory.createWebDriver();
        DesiredCapabilities capabillities = DesiredCapabilities.firefox();
        capabillities.setCapability("version", "4");
        capabillities.setCapability("platform", Platform.XP);
        this.selenium = new RemoteWebDriver(
                new URL("http://rossco_9_9:44f0744c-1689-4418-af63-560303cbb37b@ondemand.saucelabs.com:80/wd/hub"),
                capabillities);
    }

    @After
    public void tearDown() throws Exception {
        selenium.quit();
    }

    /**
     *
     */
    @Test
    public void fullRun() throws Exception {
        selenium.get("http://www.amazon.com/");
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more", selenium.getTitle());


    }

    /**
     *
     */
    @Test
    public void failure() throws Exception {
        selenium.get("http://www.amazon.com/");
        assertEquals("Blah", selenium.getTitle());

    }
}
