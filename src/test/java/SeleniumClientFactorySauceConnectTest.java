import com.saucelabs.selenium.client.factory.SeleniumFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openqa.selenium.WebDriver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SeleniumClientFactorySauceConnectTest {

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

        String driver = System.getenv("SELENIUM_DRIVER");
        if (driver == null || driver.equals("")) {
            System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        }
        this.selenium = SeleniumFactory.createWebDriver();

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
        selenium.get("http://" + hostName + ":" + PORT);
        // if the server really hit our Jetty, we should see the same title that includes the secret code.
        assertEquals("test" + secret, selenium.getTitle());
    }
}
