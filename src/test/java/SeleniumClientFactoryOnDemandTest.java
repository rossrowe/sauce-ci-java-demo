import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;

import com.saucelabs.selenium.client.factory.SeleniumFactory;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.Test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.Assert.assertEquals;

/**
 * This SeleniumClientFactoryOnDemandTest is the simplest way to run Selenium tests
 * on the Sauce cloud of Selenium servers. The <a href="https://github.com/infradna/selenium-client-factory">Selenium Factory </a>uses the
 * environment variables that you configured for the Sauce plugin for Jenkins, eliminating
 * the need to manually configure an instance of DesiredCapabilities.
 *
 * For discussions about SauceOnDemandSessionIdProvider, SauceOnDemandAuthentication,
 * SauceOnDemandTestWatcher, and @Rule, see the comments in WebDriverWithHelperTest.java.
 *
 * @author Ross Rowe
 */
public class SeleniumClientFactoryOnDemandTest implements SauceOnDemandSessionIdProvider {

    private WebDriver webDriver;
    private String sessionId;

    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

    public @Rule
    SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    public @Rule TestName testName = new TestName();


    /**
     * Create a WebDriver instance using Selenium Client Factory and run a test. We don't have
     * to refer to the environment variables set by the Sauce plugin because that's handled
     * automatically by Selenium Client Factory logic.
     */
    @Test
    public void validateTitle() throws Exception {
        webDriver = SeleniumFactory.createWebDriver();
        this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();

        webDriver.get("http://www.amazon.com/");
        assertEquals("Amazon.com: Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more", webDriver.getTitle());
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
