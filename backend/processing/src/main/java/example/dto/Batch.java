package example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;


public record Batch(

        UUID batch_id,

        @NotNull
        @Size(min = 3, max = 100)
        @JsonProperty("race_name")
        String raceName,

        @NotNull
        @Min(value = 1950)
        @Max(value = 2100)
        Integer year,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("deleted_at")
        LocalDateTime deletedAt
) {
}
