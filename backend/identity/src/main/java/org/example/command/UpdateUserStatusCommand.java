package org.example.command;

import org.example.enums.UserStatus;

public record UpdateUserStatusCommand(
        UserStatus userStatus
) {
}
