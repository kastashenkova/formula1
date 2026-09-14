package example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchResponseDto(
        UUID batch_id,
        @JsonProperty("race_name")
        String raceName,
        Integer year,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("deleted_at")
        LocalDateTime deletedAt
) {
}
