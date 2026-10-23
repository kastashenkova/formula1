package org.example.e2e.thymeleaf;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class RacePages {

    private final Page page;

    RacePages(Page page) {
        this.page = page;
    }

    Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Registered races"));
    }

    Locator addRaceLink() {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add race"));
    }

    Locator row(String raceName) {
        return page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(raceName));
    }

    Locator alert() {
        return page.locator(".notification");
    }

    void registerRace(String raceName, LocalDateTime raceDate) {
        addRaceLink().click();
        submitForm(raceName, raceDate);
    }

    void submitForm(String raceName, LocalDateTime raceDate) {
        page.getByLabel("Name").fill(raceName);

        if (raceDate != null) {
            String formattedDate = raceDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            page.getByLabel("Date").fill(formattedDate);
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
    }
}
