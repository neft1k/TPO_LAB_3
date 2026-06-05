package youtube.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import youtube.BaseUiTest;

public class YouTubeUseCaseTest extends BaseUiTest {

    @Test
    void openHomePage() {
        openYouTubeHome();
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='search_query']"))
        );
        Assertions.assertTrue(searchInput.isDisplayed());
    }

    @Test
    void homePageTitleShouldContainYouTube() {
        openYouTubeHome();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("youtube"));
    }

    @Test
    void searchShouldShowResults() {
        search("Travis Scott");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//ytd-video-renderer")
        ));
        int results = driver.findElements(By.xpath("//ytd-video-renderer")).size();
        Assertions.assertTrue(results > 0);
    }

    @Test
    void searchUrlShouldContainQueryParameter() {
        search("Travis Scott");
        Assertions.assertTrue(driver.getCurrentUrl().contains("search_query="));
    }

    @Test
    void firstResultShouldContainWatchLink() {
        search("Travis Scott");
        WebElement firstVideo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        String href = firstVideo.getAttribute("href");
        Assertions.assertNotNull(href);
        Assertions.assertTrue(href.contains("/watch"));
    }

    @Test
    void openFirstVideo() {
        search("Travis Scott");
        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        firstVideo.click();

        wait.until(ExpectedConditions.urlContains("/watch"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/watch"));
    }

    @Test
    void openedVideoShouldContainTitle() {
        openFirstVideoBySearch("Travis Scott");
        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1//yt-formatted-string"))
        );
        Assertions.assertTrue(title.isDisplayed());
    }

    @Test
    void openedVideoShouldContainVideoTag() {
        openFirstVideoBySearch("Travis Scott");
        WebElement video = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//video"))
        );
        Assertions.assertTrue(video.isDisplayed());
    }

    @Test
    void fromWatchPageShouldOpenChannel() {
        openFirstVideoBySearch("Travis Scott");
        WebElement channelLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-watch-metadata//ytd-channel-name//a)[1]")
                )
        );
        channelLink.click();
        waitForChannelUrl();
        Assertions.assertTrue(isChannelUrl(driver.getCurrentUrl()));
    }

    @Test
    void channelVideosTabShouldOpen() {
        openChannelFromWatchPage("Travis Scott");
        String root = getChannelRootUrl();
        driver.get(root + "/videos");
        wait.until(ExpectedConditions.urlContains("/videos"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/videos"));
    }

    @Test
    void channelPlaylistsTabShouldOpen() {
        openChannelFromWatchPage("Travis Scott");
        String root = getChannelRootUrl();
        driver.get(root + "/playlists");
        wait.until(ExpectedConditions.urlContains("/playlists"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/playlists"));
    }

    @Test
    void backFromWatchPageShouldReturnToSearchResults() {
        search("Travis Scott");
        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        firstVideo.click();
        wait.until(ExpectedConditions.urlContains("/watch"));
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("search_query="));
        Assertions.assertTrue(driver.getCurrentUrl().contains("search_query="));
    }

    @Test
    void openShorts() {
        openYouTubeHome();
        driver.get("https://www.youtube.com/shorts");
        wait.until(ExpectedConditions.urlContains("/shorts"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/shorts"));
    }


    @Test
    void directResultsUrlShouldOpen() {
        driver.get("https://www.youtube.com/results?search_query=Travis Scott");
        acceptConsentIfPresent();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ytd-video-renderer")));
        int count = driver.findElements(By.xpath("//ytd-video-renderer")).size();
        Assertions.assertTrue(count > 0);
    }
    

    @Test
    void searchChangeQueryThenOpenVideoAndBack() {
        openYouTubeHome();

        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='search_query']"))
        );
        searchInput.sendKeys("Travis Scott");
        searchInput.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("search_query="));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ytd-video-renderer")));
        int firstSearchCount = driver.findElements(By.xpath("//ytd-video-renderer")).size();
        Assertions.assertTrue(firstSearchCount > 0);

        WebElement searchInputSecond = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='search_query']"))
        );
        searchInputSecond.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchInputSecond.sendKeys(Keys.BACK_SPACE);
        searchInputSecond.sendKeys("Eminem");
        searchInputSecond.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.urlContains("search_query="));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ytd-video-renderer")));
        int secondSearchCount = driver.findElements(By.xpath("//ytd-video-renderer")).size();
        Assertions.assertTrue(secondSearchCount > 0);

        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        firstVideo.click();
        wait.until(ExpectedConditions.urlContains("/watch"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/watch"));

        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("search_query="));
        Assertions.assertTrue(driver.getCurrentUrl().contains("search_query="));
    }

    @Test
    void directResultsOpenVideoOpenChannelAndOpenAboutTab() {
        driver.get("https://www.youtube.com/results?search_query=Travis Scott");
        acceptConsentIfPresent();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ytd-video-renderer")));
        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        firstVideo.click();
        wait.until(ExpectedConditions.urlContains("/watch"));

        WebElement video = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//video"))
        );
        Assertions.assertTrue(video.isDisplayed());

        WebElement channelLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-watch-metadata//ytd-channel-name//a)[1]")
                )
        );
        channelLink.click();
        waitForChannelUrl();
        Assertions.assertTrue(isChannelUrl(driver.getCurrentUrl()));

        String root = getChannelRootUrl();
        driver.get(root + "/about");
        wait.until(ExpectedConditions.urlContains("/about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/about"));
    }

    @Test
    void searchInputShouldContainEnteredQuery() {
        search("Travis Scott");
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='search_query']"))
        );
        String value = searchInput.getAttribute("value");
        Assertions.assertNotNull(value);
        Assertions.assertTrue(value.contains("Travis Scott"));
    }

    @Test
    void searchShouldShowAtLeastThreeResults() {
        search("Travis Scott");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//ytd-video-renderer"), 2));
        int results = driver.findElements(By.xpath("//ytd-video-renderer")).size();
        Assertions.assertTrue(results >= 3);
    }

    @Test
    void secondResultShouldContainWatchLink() {
        search("Travis Scott");
        WebElement secondVideo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[2]")
                )
        );
        String href = secondVideo.getAttribute("href");
        Assertions.assertNotNull(href);
        Assertions.assertTrue(href.contains("/watch"));
    }

    @Test
    void thirdResultShouldContainWatchLink() {
        search("Travis Scott");
        WebElement thirdVideo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[3]")
                )
        );
        String href = thirdVideo.getAttribute("href");
        Assertions.assertNotNull(href);
        Assertions.assertTrue(href.contains("/watch"));
    }

    @Test
    void openedVideoUrlShouldContainVideoId() {
        openFirstVideoBySearch("Travis Scott");
        Assertions.assertTrue(driver.getCurrentUrl().contains("v="));
    }

    @Test
    void openedVideoTitleShouldNotBeEmpty() {
        openFirstVideoBySearch("Travis Scott");
        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1//yt-formatted-string"))
        );
        Assertions.assertFalse(title.getText().trim().isEmpty());
    }

    @Test
    void openedVideoShouldContainChannelLink() {
        openFirstVideoBySearch("Travis Scott");
        WebElement channelLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("(//ytd-watch-metadata//ytd-channel-name//a)[1]")
                )
        );
        Assertions.assertTrue(channelLink.isDisplayed());
    }

    @Test
    void channelAboutTabShouldOpen() {
        openChannelFromWatchPage("Travis Scott");
        String root = getChannelRootUrl();
        driver.get(root + "/about");
        wait.until(ExpectedConditions.urlContains("/about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/about"));
    }


    @Test
    void directResultsFirstVideoShouldOpen() {
        driver.get("https://www.youtube.com/results?search_query=Travis Scott");
        acceptConsentIfPresent();
        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        firstVideo.click();
        wait.until(ExpectedConditions.urlContains("/watch"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/watch"));
    }

    @Test
    void openHistoryPage() {
        driver.get("https://www.youtube.com/feed/history");
        acceptConsentIfPresent();

        wait.until(d ->
                d.getCurrentUrl().contains("/feed/history")
                        || d.getCurrentUrl().contains("accounts.google.com")
                        || !d.findElements(By.xpath("//*[contains(text(),'Sign in') or contains(text(),'Войти')]")).isEmpty()
        );

        boolean historyOpened = driver.getCurrentUrl().contains("/feed/history");
        boolean loginRequested = driver.getCurrentUrl().contains("accounts.google.com")
                || !driver.findElements(By.xpath("//*[contains(text(),'Sign in') or contains(text(),'Войти')]")).isEmpty();

        Assertions.assertTrue(historyOpened || loginRequested);
    }

    @Test
    void clickSignInShouldOpenLoginPage() {
        openYouTubeHome();

        WebElement signInLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//a[contains(@href,'ServiceLogin') or contains(@href,'accounts.google.com')])[1]")
                )
        );
        signInLink.click();

        wait.until(d -> isLoginRequested());
        Assertions.assertTrue(isLoginRequested());
    }

    @Test
    void clickLikeShouldRequestLogin() {
        openFirstVideoBySearch("Travis Scott");

        WebElement likeButton = findLikeButtonOrLoginLink();
        Assertions.assertNotNull(likeButton);
        safeClick(likeButton);

        wait.until(d -> isLoginRequested());
        Assertions.assertTrue(isLoginRequested());
    }

    @Test
    void openPlaylistPage() {
        search("Travis Scott playlist");

        String listId = getFirstPlaylistIdOnPage();
        if (listId == null) {
            openChannelFromWatchPage("Travis Scott");
            String root = getChannelRootUrl();
            driver.get(root + "/playlists");
            wait.until(ExpectedConditions.urlContains("/playlists"));
            listId = getFirstPlaylistIdOnPage();
        }

        Assertions.assertNotNull(listId);
        driver.get("https://www.youtube.com/playlist?list=" + listId);
        wait.until(ExpectedConditions.urlContains("list="));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/playlist"));
    }

    private void search(String text) {
        openYouTubeHome();
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='search_query']"))
        );
        searchInput.sendKeys(text);
        searchInput.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("search_query="));
    }

    private void openFirstVideoBySearch(String query) {
        search(query);
        WebElement firstVideo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-video-renderer//a[contains(@href,'/watch')])[1]")
                )
        );
        firstVideo.click();
        wait.until(ExpectedConditions.urlContains("/watch"));
    }

    private void openChannelFromWatchPage(String query) {
        openFirstVideoBySearch(query);
        WebElement channelLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//ytd-watch-metadata//ytd-channel-name//a)[1]")
                )
        );
        channelLink.click();
        waitForChannelUrl();
    }

    private void waitForChannelUrl() {
        wait.until(d -> isChannelUrl(d.getCurrentUrl()));
    }

    private boolean isChannelUrl(String url) {
        return url.contains("/@")
                || url.contains("/channel/")
                || url.contains("/c/")
                || url.contains("/user/");
    }

    private String getChannelRootUrl() {
        String url = driver.getCurrentUrl().split("\\?")[0];
        url = url.replaceAll("/videos$", "");
        url = url.replaceAll("/playlists$", "");
        url = url.replaceAll("/shorts$", "");
        url = url.replaceAll("/about$", "");
        url = url.replaceAll("/featured$", "");
        return url.replaceAll("/+$", "");
    }

    private String getFirstWatchHrefOnPage() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@href,'/watch')]")));
        for (WebElement link : driver.findElements(By.xpath("//a[contains(@href,'/watch')]"))) {
            String href = link.getAttribute("href");
            if (href == null) {
                continue;
            }
            if (!href.contains("/watch")) {
                continue;
            }
            if (href.contains("list=")) {
                continue;
            }
            if (href.startsWith("/watch")) {
                return "https://www.youtube.com" + href;
            }
            return href;
        }
        return null;
    }

    private String getFirstPlaylistIdOnPage() {
        for (int i = 0; i < 5; i++) {
            for (WebElement link : driver.findElements(By.xpath("//a[contains(@href,'list=')]"))) {
                String href = link.getAttribute("href");
                if (href == null || !href.contains("list=")) {
                    continue;
                }
                String listId = href.substring(href.indexOf("list=") + 5);
                int ampIndex = listId.indexOf('&');
                if (ampIndex != -1) {
                    listId = listId.substring(0, ampIndex);
                }
                if (!listId.isBlank()) {
                    return listId;
                }
            }
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private WebElement findLikeButtonOrLoginLink() {
        By[] locators = new By[] {
                By.xpath("(//segmented-like-dislike-button-view-model//button)[1]"),
                By.xpath("(//segmented-like-dislike-button-renderer//button)[1]"),
                By.xpath("(//like-button-view-model//button)[1]"),
                By.xpath("(//ytd-watch-metadata//button[contains(@aria-label,'like') or contains(@aria-label,'Like') or contains(@aria-label,'Нравится') or contains(@aria-label,'нравится')])[1]"),
                By.xpath("(//ytd-watch-metadata//button[@aria-pressed])[1]"),
                By.xpath("(//ytd-watch-metadata//a[contains(@href,'ServiceLogin') or contains(@href,'accounts.google.com')])[1]")
        };

        for (int i = 0; i < 4; i++) {
            for (By locator : locators) {
                if (!driver.findElements(locator).isEmpty()) {
                    WebElement element = driver.findElement(locator);
                    if (element.isDisplayed()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                        return element;
                    }
                }
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 250);");
            sleep(700);
        }
        return null;
    }

    private void safeClick(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isLoginRequested() {
        return driver.getCurrentUrl().contains("accounts.google.com")
                || driver.getCurrentUrl().contains("ServiceLogin")
                || !driver.findElements(By.xpath("//*[contains(text(),'Sign in') or contains(text(),'Войти')]")).isEmpty()
                || !driver.findElements(By.xpath("//input[@type='email']")).isEmpty();
    }
}
