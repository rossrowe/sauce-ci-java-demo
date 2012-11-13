import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
 * @author Ross Rowe
 */
public class SauceConnectTest {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?username=rossco_9_9&access-key=XXX&max-duration=300&os=windows 2008&browser=firefox&browser-version=4.";
    public static final int PORT = 5000;

    private WebDriver selenium;
    private Server server;
    int jettyLocalPort;
    final int secret = new Random().nextInt();
    private String hostName;

    @Before
    public void setUp() throws Exception {

        hostName = "localhost";
        //construct the DesiredCapabilities using the environment variables set by the Sauce CI plugin
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("version", Utils.readPropertyOrEnv("SELENIUM_VERSION", "4"));
        capabilities.setCapability("platform", Utils.readPropertyOrEnv("SELENIUM_PLATFORM", "XP"));
        capabilities.setCapability("browserName", Utils.readPropertyOrEnv("SELENIUM_BROWSER", "firefox"));
        String username = Utils.readPropertyOrEnv("SAUCE_USER_NAME", "");
        String accessKey = Utils.readPropertyOrEnv("SAUCE_ACCESS_KEY", "");
        this.selenium = new RemoteWebDriver(new URL("http://" + username + ":" + accessKey + "@" + hostName + ":4445/wd/hub"),
                capabilities);

        //create a local jetty server
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

    @After
    public void tearDown() throws Exception {
        selenium.quit();
    }

    /**
     * Start a web server locally, and have Sauce OnDemand connect to the local server.
     */
    @Test
    public void fullRun() throws Exception {
        String sessionId = ((RemoteWebDriver) selenium).getSessionId().toString();
        System.out.println("SauceOnDemandSessionID=" + sessionId);
        selenium.get("http://" + hostName + ":" + PORT);
        // if the server really hit our Jetty, we should see the same title that includes the secret code.
        assertEquals("test" + secret, selenium.getTitle());
    }
}
