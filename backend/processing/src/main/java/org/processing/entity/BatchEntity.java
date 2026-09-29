package org.processing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "batches")
public class BatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID batchId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column()
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkItemEntity> workItems = new HashSet<>();

    protected BatchEntity() {}

    public void removeItem(WorkItemEntity item) {
        workItems.remove(item);
        item.setBatch(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        var that = (BatchEntity) o;
        return Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchId);
    }
}
