package org.example.e2e.thymeleaf;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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

    void registerRace(String raceName, Integer year) {
        addRaceLink().click();
        submitForm(raceName, year);
    }

    void submitForm(String raceName, Integer year) {
        page.getByLabel("Name").fill(raceName);

        if (year != null) {
            page.getByLabel("Year").fill(String.valueOf(year));
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add")).click();
    }
}
