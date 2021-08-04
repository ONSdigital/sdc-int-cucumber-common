package uk.gov.ons.ctp.common.util;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This class facilitates the ability for Cucumber to wait for certain events to happen within the
 * UI Waits until an expected condition occurs Waits for a specific element to be displayed Waits
 * for the presence of a specific element Waits for a page to fully load
 */
public class Wait {

  private static final Logger log = LoggerFactory.getLogger(Wait.class);

  private WebDriver driver;
  private static int DEFAULT_WAIT = 5; // 5 SECONDS

  public Wait(WebDriver driver) {
    this.driver = driver;
  }

  private void logAndWaitUntilCondition(
      final ExpectedCondition<?> condition,
      final String timeoutMessage,
      final int timeout,
      final String elementName) {
    String currentPage = "";
    try {
      currentPage = driver.getCurrentUrl();
      waitUntilCondition(condition, timeoutMessage, timeout);
    } catch (RuntimeException timeoutException) {
      log.info("Current Page: " + currentPage);
      log.error("Timeout failed looking for: " + condition.toString());
      log.error("Current URL when errored: " + driver.getCurrentUrl());
      if (!elementName.isEmpty()) {
        log.error("Looking for class / field: " + elementName);
      }
      final Document doc = Jsoup.parse(driver.getPageSource());
      final Elements scripts = doc.getElementsByTag("script");
      scripts.remove();
      final Elements links = doc.getElementsByTag("link");
      links.remove();
      log.error("Current Page HTML (without scripts or links): " + "\r\n" + doc.html());
      throw new TimeoutException(elementName, timeoutException);
    }
  }

  private void waitUntilCondition(
      ExpectedCondition<?> condition, String timeoutMessage, int timeout) throws TimeoutException {
    WebDriverWait wait = new WebDriverWait(driver, timeout);
    wait.withMessage(timeoutMessage);
    wait.until(condition);
  }

  public void forLoading(int timeout) {
    ExpectedCondition<Object> condition =
        ExpectedConditions.jsReturnsValue("return document.readyState==\"complete\";");
    String timeoutMessage = "Page didn't load after " + timeout + " seconds.";
    logAndWaitUntilCondition(condition, timeoutMessage, timeout, "");
  }

  public void forLoading() {
    forLoading(DEFAULT_WAIT);
  }

  public void forElementToBeDisplayed(int timeout, WebElement webElement, String webElementName) {
    ExpectedCondition<WebElement> condition = ExpectedConditions.visibilityOf(webElement);
    String timeoutMessage =
        webElementName + " wasn't displayed after " + Integer.toString(timeout) + " seconds.";
    logAndWaitUntilCondition(condition, timeoutMessage, timeout, webElementName);
  }

  public void forElementToBeDisplayed(WebElement webElement, String webElementName) {
    forElementToBeDisplayed(DEFAULT_WAIT, webElement, webElementName);
  }

  public void forPresenceOfElements(int timeout, By elementLocator, String elementName) {
    ExpectedCondition<List<WebElement>> condition =
        ExpectedConditions.presenceOfAllElementsLocatedBy(elementLocator);
    String timeoutMessage =
        elementName
            + " elements were not displayed after "
            + Integer.toString(timeout)
            + " seconds.";
    logAndWaitUntilCondition(condition, timeoutMessage, timeout, elementName);
  }

  public void forPresenceOfElements(By elementLocator, String elementName) {
    forPresenceOfElements(DEFAULT_WAIT, elementLocator, elementName);
  }
}
