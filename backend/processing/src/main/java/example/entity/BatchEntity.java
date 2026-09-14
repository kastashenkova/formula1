package example.entity;

import java.util.UUID;

public record BatchEntity(
        UUID batch_id,
        String raceName,
        Integer year
) {
}
