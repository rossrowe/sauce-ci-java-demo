import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class TunnelTest {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=300&os=windows 2008&browser=firefox&browser-version=4.";
    private int code;

    /**
     * Start a web server locally, and have Sauce OnDemand connect to the local server.
     */
    @Test
    public void fullRun() throws Exception {

        String driver = System.getenv("SELENIUM_DRIVER");
        if (driver == null || driver.equals("")) {
            System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        }

        System.setProperty("SELENIUM_STARTING_URL", "http://www.amazon.com/");
        Selenium selenium = SeleniumFactory.create();
        selenium.start();
        selenium.open("/");
        // if the server really hit our Jetty, we should see the same title that includes the secret code.
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more", selenium.getTitle());
        selenium.click("id=twotabsearchtextbox");
        selenium.type("id=twotabsearchtextbox", "bendis");
        selenium.click("css=input[type=\"image\"]");
        selenium.waitForPageToLoad("30000");
        //selenium.click("link=Scarlet, Book 1");
        //selenium.waitForPageToLoad("30000");
        selenium.click("link=New Releases");
        selenium.waitForPageToLoad("30000");
        //selenium.stop();
        selenium.stop();

    }
}
