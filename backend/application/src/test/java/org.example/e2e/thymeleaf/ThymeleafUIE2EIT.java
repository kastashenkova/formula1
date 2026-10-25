package org.example.e2e.thymeleaf;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import org.example.support.E2ETest;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@E2ETest
class ThymeleafUIE2EIT extends BrowserTestSupport {

    // login/logout scenario
    @Test
    void correctPassword_ShouldRedirectToRacesPage() {
        loginAs("astashenkova.katya@gmail.com", "admin123");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Registered races"))).isVisible();
    }

    @Test
    void wrongPassword_ShouldShowLoginError() {
        page.navigate("/login");
        page.getByLabel("Username").fill("astashenkova.katya@gmail.com");
        page.getByLabel("Password").fill("wrong-password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();

        assertThat(page.locator(".notification.is-danger")).hasText("Invalid username or password");
    }

    @Test
    void logout_ShouldShowLogoutSuccessMessage() {
        loginAs("astashenkova.katya@gmail.com", "admin123");

        page.getByText("Logout").click();

        assertThat(page.locator(".notification.is-info")).hasText("You have been logged out");
    }

    // race registration scenario
    @Test
    void admin_ShouldRegisterRaceThroughForm() {
        loginAs("d.dzhos@ukma.edu.ua", "dmytro123");
        RacePages races = new RacePages(page);

        races.registerRace("New York 2026", LocalDateTime.of(2026, 10, 22, 15, 0));

        assertThat(page.locator(".notification.is-success")).containsText("New York 2026");
        assertThat(page.locator("body")).containsText("New York 2026");
    }

    @Test
    void invalidForm_ShouldShowMessagesNextToFields() {
        loginAs("d.dzhos@ukma.edu.ua", "dmytro123");

        new RacePages(page).registerRace("F1", null);

        assertThat(page.getByText("Race name must be from 3 to 100 characters")).isVisible();
        assertThat(page.getByText("Race date is required")).isVisible();
    }

    @Test
    void user_ShouldNotSeeAddButtonOrOpenForm() {
        loginAs("ast.katrya@gmail.com", "kateryna123");

        assertThat(new RacePages(page).addRaceLink()).hasCount(0);

        page.navigate("/races/new");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Access denied")))
                .isVisible();
    }

    // views scenario
    @Test
    void viewAsCards_ShouldSwitchToCardsView() {
        loginAs("ast.katrya@gmail.com", "kateryna123");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("View as cards")).click();

        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("View as table"))).isVisible();
    }

    @Test
    void viewAsTable_ShouldBeDefaultView() {
        loginAs("ast.katrya@gmail.com", "kateryna123");

        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("View as cards"))).isVisible();
        assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("Name"))).isVisible();
    }
}
