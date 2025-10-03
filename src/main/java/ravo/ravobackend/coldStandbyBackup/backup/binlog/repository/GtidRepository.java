package ravo.ravobackend.coldStandbyBackup.backup.binlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GtidHistory;

import java.util.Optional;

@Repository
public interface GtidRepository extends JpaRepository<GtidHistory, Long> {

    @Query("SELECT g FROM GtidHistory g WHERE g.dbName = :dbName ORDER BY g.createdAt DESC LIMIT 1")
    Optional<GtidHistory> findLatestByDbName(String dbName);
}