import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import org.junit.*;
import org.junit.rules.TestName;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * With Sauce Connect you can test your code against the Sauce cloud of Selenium servers when
 * your code is behind your firewall.
 *
 * This SauceConnectTest shows you how to launch a local Jetty server and run Selenium tests on
 * Sauce using the environment variables that you set when you configured the the Sauce plugin
 * for Jenkins.
 *
 * For discussions about SauceOnDemandSessionIdProvider, SauceOnDemandAuthentication,
 * SauceOnDemandTestWatcher, @Rule and TestName, see the comments in
 * WebDriverWithHelperTest.java.
 *
 * @author Ross Rowe
 */
public class SauceConnectTest implements SauceOnDemandSessionIdProvider {
    public static final int PORT = 5000;

    private WebDriver webDriver;
    private String sessionId;

    private String hostName;
    private Server server;
    int jettyLocalPort;
    final int secret = new Random().nextInt();

    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();

    public @Rule
    SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    public @Rule TestName testName = new TestName();


    @Before
    public void setUp() throws Exception {

        hostName = "localhost";

        // Construct DesiredCapabilities using the environment variables set by the Sauce plugin
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("version", Utils.readPropertyOrEnv("SELENIUM_VERSION", "17.0.1"));
        capabilities.setCapability("platform", Utils.readPropertyOrEnv("SELENIUM_PLATFORM", "XP"));
        capabilities.setCapability("browserName", Utils.readPropertyOrEnv("SELENIUM_BROWSER", "firefox"));
        String username = Utils.readPropertyOrEnv("SAUCE_USER_NAME", "");
        String accessKey = Utils.readPropertyOrEnv("SAUCE_API_KEY", "");
        this.webDriver = new RemoteWebDriver(new URL("http://" + username + ":" + accessKey + "@" + hostName + ":4445/wd/hub"),
                capabilities);
        this.sessionId = ((RemoteWebDriver)webDriver).getSessionId().toString();

        // Create a new local Jetty server
        server = new Server(PORT);

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentType("text/html");
                resp.getWriter().println("<html><head><title>test" + secret + "</title></head><body>it works</body></html>");
            }
        }), "/");

        server.setHandler(handler);

        SocketConnector connector = new SocketConnector();
        server.addConnector(connector);
        server.start();
        jettyLocalPort = connector.getLocalPort();
        System.out.println("Started Jetty at " + jettyLocalPort);

    }

    /**
     * Start a web server locally, have Sauce OnDemand connect to the local server and
     * validate that the titles match. If the server really hit our Jetty, we should see the
     * same title that includes the secret code.
     */
    @Test
    @Ignore
    public void validateTitle() throws Exception {
        webDriver.get("http://" + hostName + ":" + PORT);
        assertEquals("test" + secret, webDriver.getTitle());
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
        server.stop();
        server.join();
        //Thread.sleep(1000); // sleep for 1000 milliseconds

/*
        Note: In the unlikely event that you get an error similar to "java.net.BindException:
        Address already in use: JVM_Bind" when running multiple tests, that may mean that the
        server hasn't finished shutting down before trying to restart.

        If you get this error, try uncommenting the Thread.sleep(1000) line above. If Thread.sleep(1000)
        doesn't work, try increasing the Thread.sleep() value to 5000 or 10000.
*/

    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

}
