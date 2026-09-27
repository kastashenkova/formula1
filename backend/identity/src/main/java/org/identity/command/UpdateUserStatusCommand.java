package org.identity.command;

import org.identity.enums.UserStatus;

public record UpdateUserStatusCommand(
        UserStatus userStatus
) {
}
