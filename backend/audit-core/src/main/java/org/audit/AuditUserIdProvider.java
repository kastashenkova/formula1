package org.audit;

import java.util.UUID;

public interface AuditUserIdProvider {
    UUID getCurrentUserId();
}
