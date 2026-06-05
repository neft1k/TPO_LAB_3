package youtube;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BaseUiTest {

    private static final Map<String, String> DOTENV = loadDotEnv();

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    protected void setUp() {
        String browser = getConfig("browser", "YOUTUBE_BROWSER", "chrome").toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(getConfig("headless", "YOUTUBE_HEADLESS", "false"));
        switch (browser) {
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "chrome":
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                driver = new ChromeDriver(chromeOptions);
                break;
        }
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    @AfterEach
    protected void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openYouTubeHome() {
        driver.get("https://www.youtube.com/");
        acceptConsentIfPresent();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='search_query']")));
    }

    protected void acceptConsentIfPresent() {
        By acceptButton = By.xpath(
                "//button[contains(.,'Accept all') or contains(.,'I agree') or contains(.,'Принять')]"
        );
        try {
            WebElement button = new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.elementToBeClickable(acceptButton));
            button.click();
        } catch (Exception ignored) {
        }
    }

    protected String getConfig(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String dotEnvValue = DOTENV.get(envName);
        if (dotEnvValue != null && !dotEnvValue.isBlank()) {
            return dotEnvValue;
        }

        return defaultValue;
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path dotEnvPath = Paths.get(".env");
        if (!Files.exists(dotEnvPath)) {
            return values;
        }

        try {
            for (String rawLine : Files.readAllLines(dotEnvPath)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();

                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                values.put(key, value);
            }
        } catch (IOException ignored) {
        }

        return values;
    }
}
