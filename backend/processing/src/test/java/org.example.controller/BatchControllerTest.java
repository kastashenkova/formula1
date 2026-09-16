package org.example.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.BatchRequestDto;
import org.example.dto.BatchResponseDto;
import org.example.service.BatchService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BatchController.class)
@TestConstructor(autowireMode = ALL)
public class BatchControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private BatchService batchService;

    public BatchControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("Should return list with with example race session")
    void getRaces_filledList_Success() throws Exception {
        BatchResponseDto batchExample = new BatchResponseDto(
                UUID.randomUUID(),
                "exampleRace",
                2025,
                LocalDateTime.now(),
                null
        );

        when(batchService.getBatches(0, 10)).thenReturn(List.of(batchExample));

        mockMvc.perform(get("/batches")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(batchService, times(1)).getBatches(0, 10);
    }

    @Test
    @DisplayName("Should return empty list")
    void getRaces_emptyList_Success() throws Exception {
        when(batchService.getBatches(0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/batches")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(batchService, times(1)).getBatches(0, 10);
    }

    @Test
    @DisplayName("Should return list paginated with default parameters")
    void getRaces_defaultParameters_Success() throws Exception {
        mockMvc.perform(get("/batches"))
                .andExpect(status().isOk());

        verify(batchService, times(1)).getBatches(0, 10);
    }

    @Test
    @DisplayName("Should return list paginated with default parameters")
    void uploadRace_relevantData_Success() throws Exception {
        BatchRequestDto newBatchRequestDto = new BatchRequestDto(
                null,
                "exampleRace",
                2025,
                LocalDateTime.now(),
                null);

        BatchResponseDto newBatchResponseDto = new BatchResponseDto(
                UUID.randomUUID(),
                newBatchRequestDto.raceName(),
                newBatchRequestDto.year(),
                newBatchRequestDto.createdAt(),
                newBatchRequestDto.deletedAt()
        );

        when(batchService.uploadBatch(newBatchRequestDto)).thenReturn(newBatchResponseDto);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBatchRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batch_id").value(newBatchResponseDto.batch_id().toString()))
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/batches/"
                                + newBatchResponseDto.batch_id())));

        verify(batchService, times(1)).uploadBatch(newBatchRequestDto);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when year is more than expected maximum")
    void uploadRace_tooBigYear_ReturnsBadRequest() throws Exception {
        BatchRequestDto invalidRequest = new BatchRequestDto(
                null,
                "exampleRace",
                2027,
                LocalDateTime.now(),
                null);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when year is less than expected minimum")
    void uploadRace_tooSmallYear_ReturnsBadRequest() throws Exception {
        BatchRequestDto invalidRequest = new BatchRequestDto(
                null,
                "exampleRace",
                1949,
                LocalDateTime.now(),
                null);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when race name is too short")
    void uploadRace_tooShortRaceName_ReturnsBadRequest() throws Exception {
        BatchRequestDto invalidRequest = new BatchRequestDto(
                null,
                "rc",
                2024,
                LocalDateTime.now(),
                null);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when race name is too long")
    void uploadRace_tooLongRaceName_ReturnsBadRequest() throws Exception {
        BatchRequestDto invalidRequest = new BatchRequestDto(
                null,
                "invalid-race-name-here-invalid-race-name-here-invalid-race-name-here-invalid-race-name-here-invalid-race-name-here",
                2024,
                LocalDateTime.now(),
                null);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when batch id is not null during creation")
    void uploadRace_batchIdNotNull_ReturnsBadRequest() throws Exception {
        BatchRequestDto invalidRequest = new BatchRequestDto(
                UUID.randomUUID(),
                "exampleRace",
                2025,
                LocalDateTime.now(),
                null);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when race name is null")
    void uploadRace_nullRaceName_ReturnsBadRequest() throws Exception {
        BatchRequestDto invalidRequest = new BatchRequestDto(
                null,
                null,
                2025,
                LocalDateTime.now(),
                null);

        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is missing")
    void uploadRace_missingBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(batchService);
    }
}
