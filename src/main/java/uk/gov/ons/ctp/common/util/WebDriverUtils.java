package uk.gov.ons.ctp.common.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class WebDriverUtils {
  private static final String LOCATION_ROOT = "src/test/resources/";

  public static WebDriver getWebDriver(
      final WebDriverType webDriverType, final boolean isHeadless, final String loggingLevel) {

    WebDriver driver;
    final String os = System.getProperty("os.name").toLowerCase();

    switch (webDriverType) {
      case EDGE:
        setupEdgeOSWebdriver(os);
        driver = getEdgeDriver(isHeadless, os, loggingLevel);
        break;

      case CHROME:
        setupChromeOSWebdriver(os);
        driver = getChromeDriver(isHeadless, os);
        break;

      default:
        setupFirefoxOSWebdriver(os);
        driver = getFirefoxDriver(isHeadless, os, loggingLevel);
    }
    driver.manage().timeouts().implicitlyWait(1, TimeUnit.MICROSECONDS);
    return driver;
  }

  private static EdgeDriver getEdgeDriver(
      final boolean isHeadless, final String os, final String logingLevel) {
    EdgeOptions options = new EdgeOptions();

    if (os.contains("linux")) {
      options.setCapability("headless", isHeadless);
      options.setCapability("binary", "/usr/bin/edge");
    }
    return new EdgeDriver(options);
  }

  private static FirefoxDriver getFirefoxDriver(
      final boolean isHeadless, final String os, final String loggingLevel) {
    // Does not need the deprecated DesiredCapabilities....
    FirefoxOptions options = new FirefoxOptions();
    if (os.contains("linux")) {
      options.setBinary("/usr/bin/firefox");
    }
    options.setHeadless(isHeadless);
    options.setLogLevel(FirefoxDriverLogLevel.valueOf(loggingLevel));
    options.setAcceptInsecureCerts(false);
    return new FirefoxDriver(options);
  }

  private static ChromeDriver getChromeDriver(final boolean isHeadless, final String os) {
    ChromeOptions options = new ChromeOptions();
    if (os.contains("linux")) {
      options.setBinary("/usr/bin/chrome");
    }
    options.setHeadless(isHeadless);
    options.setAcceptInsecureCerts(true);
    options.merge(getChromeLoggingCapabilities());
    return new ChromeDriver(options);
  }

  private static void setupFirefoxOSWebdriver(final String os) {
    if (os.contains("mac")) {
      System.setProperty(
          "webdriver.gecko.driver", LOCATION_ROOT + "geckodriver/geckodriver-v0.26.0.macos");
    } else if (os.contains("linux")) {
      System.setProperty(
          "webdriver.gecko.driver", LOCATION_ROOT + "geckodriver/geckodriver-v0.26.0.linux");
    } else {
      System.err.println(
          "Unsupported platform - gecko driver not available for platform [" + os + "]");
      System.exit(1);
    }
  }

  private static void setupChromeOSWebdriver(final String os) {
    if (os.contains("mac")) {
      System.setProperty(
          "webdriver.chrome.driver",
          LOCATION_ROOT + "chromedriver/chromedriver.79.0.3945.36.macos");
    } else if (os.contains("linux")) {
      System.setProperty(
          "webdriver.chrome.driver",
          LOCATION_ROOT + "chromedriver/chromedriver.79.0.3945.36.linux");
    } else {
      System.err.println(
          "Unsupported platform - gecko driver not available for platform [" + os + "]");
      System.exit(1);
    }
  }

  private static void setupEdgeOSWebdriver(final String os) {
    if (os.contains("mac")) {
      System.setProperty(
          "webdriver.gecko.driver", LOCATION_ROOT + "geckodriver/geckodriver-v0.26.0.macos");
    } else if (os.contains("linux")) {
      System.setProperty(
          "webdriver.gecko.driver", LOCATION_ROOT + "geckodriver/geckodriver-v0.26.0.linux");
    } else {
      System.err.println(
          "Unsupported platform - gecko driver not available for platform [" + os + "]");
      System.exit(1);
    }
  }

  private static DesiredCapabilities getChromeLoggingCapabilities() {
    DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
    desiredCapabilities.setCapability(CapabilityType.LOGGING_PREFS, getLoggingPreferences());
    return desiredCapabilities;
  }

  private static LoggingPreferences getLoggingPreferences() {
    LoggingPreferences logs = new LoggingPreferences();
    logs.enable(LogType.BROWSER, Level.ALL);
    logs.enable(LogType.CLIENT, Level.OFF);
    logs.enable(LogType.DRIVER, Level.ALL);
    logs.enable(LogType.PERFORMANCE, Level.OFF);
    logs.enable(LogType.PROFILER, Level.OFF);
    logs.enable(LogType.SERVER, Level.OFF);
    return logs;
  }
}
