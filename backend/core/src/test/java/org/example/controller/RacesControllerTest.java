package org.example.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.example.dto.race_session.RaceSessionResponseDto;
import org.example.service.race.RacesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RacesController.class)
public class RacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RacesService raceService;

    @Test
    @DisplayName("Should return list with with example race session")
    void getRaces_filledList_Success() throws Exception {
        RaceSessionResponseDto raceExample = new RaceSessionResponseDto(/*TODO*/);

        when(raceService.getRaces(0, 10)).thenReturn(List.of(raceExample));

        mockMvc.perform(get("/races")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(raceService, times(1)).getRaces(0, 10);
    }

    @Test
    @DisplayName("Should return empty list")
    void getRaces_emptyList_Success() throws Exception {
        when(raceService.getRaces(0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/races")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(raceService, times(1)).getRaces(0, 10);
    }

    @Test
    @DisplayName("Should return list paginated with default parameters")
    void getRaces_defaultParameters_Success() throws Exception {
        mockMvc.perform(get("/races"))
                .andExpect(status().isOk());

        verify(raceService, times(1)).getRaces(0, 10);
    }
}
