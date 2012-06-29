import com.saucelabs.selenium.client.factory.SeleniumFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Platform;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Ross Rowe
 */
public class SauceConnectTest {

    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?username=rossco_9_9&access-key=44f0744c-1689-4418-af63-560303cbb37b&max-duration=300&os=windows 2008&browser=firefox&browser-version=4.";
    public static final int PORT = 5000;

    private WebDriver selenium;
    private Server server;
    int jettyLocalPort;
    final int secret = new Random().nextInt();
    private String hostName;

    @Before
    public void setUp() throws Exception {
        //String driver = System.getenv("SELENIUM_DRIVER");
        //if (driver == null || driver.equals("")) {
        System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
        //}
        hostName = getHostName();
        //hostName = "localhost";

        System.setProperty("SELENIUM_PORT", "4445");
        //System.setProperty("SELENIUM_HOST", "localhost");
        System.setProperty("SELENIUM_STARTING_URL", "http://" + hostName + ":" + PORT);
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("version", "4");
        capabilities.setCapability("platform", Platform.XP);
        //this.selenium = SeleniumFactory.createWebDriver();
        this.selenium = new RemoteWebDriver(new URL("http://rossco_9_9:44f0744c-1689-4418-af63-560303cbb37b@" + hostname + ":4445/wd/hub"),
            capabilities);
        
        
        //this.selenium = new FirefoxDriver();
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
        System.out.println("Started Jetty at "+ jettyLocalPort);

    }

    private String getHostName() {

        try {
            // Replace eth0 with your interface name
            NetworkInterface i = null;

            i = NetworkInterface.getByName("eth0");


            if (i != null) {

                Enumeration<InetAddress> iplist = i.getInetAddresses();

                InetAddress addr = null;

                while (iplist.hasMoreElements()) {
                    InetAddress ad = iplist.nextElement();
                    byte bs[] = ad.getAddress();
                    if (bs.length == 4 && bs[0] != 127) {
                        addr = ad;
                        // You could also display the host name here, to
                        // see the whole list, and remove the break.
                        break;
                    }
                }

                if (addr != null) {
                    return addr.getCanonicalHostName();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;

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
