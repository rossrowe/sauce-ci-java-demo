import com.saucelabs.rest.Credential;
import com.saucelabs.selenium.client.factory.SeleniumFactory;
import com.thoughtworks.selenium.Selenium;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

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
public class TunnelTest {
    
    protected static final String DEFAULT_SAUCE_DRIVER = "sauce-ondemand:?max-duration=30&os=windows 2008&browser=firefox&browser-version=4.";
    private int code;

    /**
     * Start a web server locally, set up an SSH tunnel, and have Sauce OnDemand connect to the local server.
     */
    @Test
    public void fullRun() throws Exception {
        this.code = new Random().nextInt();
        // start the Jetty locally and have it respond our secret code.
         // start the Jetty locally and have it respond our secret code.
        Server server = new Server(8080);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentType("text/html");
                resp.getWriter().println("<html><head><title>test" + code + "</title></head><body>it works</body></html>");
            }
        }), "/");
        server.setHandler(handler);

        SocketConnector connector = new SocketConnector();
        server.addConnector(connector);
        server.start();
        System.out.println("Started Jetty at " + connector.getLocalPort());

        try {
            // start a tunnel
            System.out.println("Starting a tunnel");
            Credential c = new Credential();            

            String originalUrl = System.getenv("SELENIUM_STARTING_URL");
            try {
                String driver = System.getenv("SELENIUM_DRIVER");
                if (driver == null || driver.equals("")) {
                    System.setProperty("SELENIUM_DRIVER", DEFAULT_SAUCE_DRIVER);
                }

                System.setProperty("SELENIUM_STARTING_URL", "http://test" + code + ".org/");
                Selenium selenium = SeleniumFactory.create();
                selenium.start();
                selenium.open("/");
                // if the server really hit our Jetty, we should see the same title that includes the secret code.
                assertEquals("test" + code, selenium.getTitle());
                selenium.stop();
            } finally {
                if (originalUrl != null && !originalUrl.equals("")) {
                     System.setProperty("SELENIUM_STARTING_URL", originalUrl);
                }
            }
        } finally {
            server.stop();
        }
    }
}
