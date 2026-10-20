package org.example.controller;

import jakarta.validation.groups.Default;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.streaming.dto.RaceRequestDto;
import org.streaming.dto.RaceResponseDto;
import org.streaming.service.RaceService;
import org.streaming.validation.OnCreate;

@Controller
public class WebRaceController {

    private final RaceService raceService;

    public WebRaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @GetMapping("/races")
    public String races(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<RaceResponseDto> races =
                raceService.getRaces(PageRequest.of(page, size));

        model.addAttribute("races", races);
        return "races/races";
    }

    @GetMapping("/races/new")
    public String newRace(Model model) {
        model.addAttribute("form", new RaceRequestDto(null, null, null));
        return "races/create-form";
    }

    @PostMapping("/races")
    public String createRace(
            @Validated({Default.class, OnCreate.class})
            @ModelAttribute("form") RaceRequestDto form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "races/create-form";
        }

        raceService.addRace(form);

        return "redirect:/races";
    }
}
