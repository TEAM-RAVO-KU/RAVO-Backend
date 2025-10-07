package ravo.ravobackend.coldStandbyBackup.backup.binlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GtidHistory;

import java.util.Optional;

@Repository
public interface GtidRepository extends JpaRepository<GtidHistory, Long> {

    Optional<GtidHistory> findTop1ByDbNameOrderByCreatedAtDesc(String dbName);

}