package uk.gov.ons.ctp.common.util;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleWebDriverFactory {

  @Value("${webdriver.type}")
  private String driverTypeName;

  @Value("${webdriver.logging_level}")
  private String driverLoggingLevel;

  @Value("${webdriver.headless}")
  private Boolean headless;

  public void closeWebDriver(WebDriver driver) {
    driver.quit();
  }

  public WebDriver getWebDriver() {
    return createWebDriver();
  }

  private WebDriver createWebDriver() {
    final WebDriver webDriver =
        WebDriverUtils.getWebDriver(
            WebDriverType.valueOf(driverTypeName), headless, driverLoggingLevel);
    webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.MICROSECONDS);
    return webDriver;
  }

  @PostConstruct
  public void startup() {
    log.info("Running with Simple Web Driver factory");
  }
}
