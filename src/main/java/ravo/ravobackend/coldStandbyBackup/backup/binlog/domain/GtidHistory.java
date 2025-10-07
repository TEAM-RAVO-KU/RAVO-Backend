package ravo.ravobackend.coldStandbyBackup.backup.binlog.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gtid_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GtidHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String dbName;

    @Column(nullable = false, length = 1000)
    private String gtidSet;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
