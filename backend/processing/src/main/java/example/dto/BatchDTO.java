package example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;


public record BatchDTO(

        UUID batch_id,

        @NotNull(message = "{validation.raceName.not-null}")
        @Size(min = 3, max = 100, message = "{validation.raceName.size}")
        @JsonProperty("race_name")
        String raceName,

        @NotNull
        @Min(value = 1950, message = "{validation.batchYear.min}")
        @Max(value = 2100, message = "{validation.batchYear.max}")
        Integer year,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("deleted_at")
        LocalDateTime deletedAt
) {
}
