package org.example.e2e.thymeleaf;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public abstract class BrowserTestSupport {

    private static final Path ARTIFACTS = Paths.get("target", "playwright");

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    protected int port;

    protected BrowserContext context;
    protected Page page;
    private boolean tracingSaved;
    private String testName;

    @RegisterExtension
    final AfterTestExecutionCallback saveArtifactsOnFailure = extension -> {
        testName = extension.getRequiredTestClass().getSimpleName() + "-" + extension.getDisplayName()
                .replaceAll("[^\\p{L}\\p{N}_-]", "");

        if (extension.getExecutionException().isPresent() && page != null && context != null) {
            page.screenshot(new Page.ScreenshotOptions().setPath(ARTIFACTS.resolve(testName + "-failed.png")).setFullPage(true));
            context.tracing().stop(new Tracing.StopOptions().setPath(ARTIFACTS.resolve(testName + "-failed-trace.zip")));
            tracingSaved = true;
        }
    };

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL("http://localhost:" + port)
                .setLocale("en-US"));
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
        tracingSaved = false;
        testName = "unknown-test-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void closePage() {
        if (context != null) {
            if (!tracingSaved) {
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(ARTIFACTS.resolve(testName + "-success-trace.zip")));
            }
            context.close();
        }
    }

    protected void loginAs(String username, String password) {
        page.navigate("/login");
        page.getByLabel("Username").fill(username);
        page.getByLabel("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Registered races")))
                .isVisible();
    }
}
