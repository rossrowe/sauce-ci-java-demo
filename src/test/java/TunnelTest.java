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
     * Start a web server locally, and have Sauce OnDemand connect to the local server.
     */
    @Test
    public void fullRun() throws Exception {
        this.code = new Random().nextInt();
        // start the Jetty locally and have it respond our secret code.
         // start the Jetty locally and have it respond our secret code.
       
        try {
            // start a tunnel
            System.out.println("Starting a tunnel");

            String originalUrl = System.getenv("SELENIUM_STARTING_URL");
            try {
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
                selenium.findElement(By.linkText("Books")).click();
				selenium.findElement(By.linkText("Kindle")).click();
				selenium.findElement(By.linkText("Movies & TV")).click();
				selenium.findElement(By.cssSelector("span.text")).click();
				selenium.findElement(By.linkText("MP3 Music Store")).click();
				selenium.findElement(By.linkText("Music CDs")).click();
 				selenium.stop();
            } finally {
                if (originalUrl != null && !originalUrl.equals("")) {
                     System.setProperty("SELENIUM_STARTING_URL", originalUrl);
                }
            }
        } finally {
            //server.stop();
        }
    }
}
