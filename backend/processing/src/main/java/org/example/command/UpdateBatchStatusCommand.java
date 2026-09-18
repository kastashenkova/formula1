package org.example.command;

import org.example.enums.BatchStatus;

public record UpdateBatchStatusCommand(
        BatchStatus batchStatus
) {
}
