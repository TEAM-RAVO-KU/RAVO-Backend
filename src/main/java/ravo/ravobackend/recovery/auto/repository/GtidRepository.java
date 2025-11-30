package ravo.ravobackend.recovery.auto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ravo.ravobackend.recovery.auto.domain.GtidHistory;

import java.util.Optional;

@Repository
public interface GtidRepository extends JpaRepository<GtidHistory, Long> {

    Optional<GtidHistory> findTop1ByDbNameOrderByCreatedAtDesc(String dbName);

}