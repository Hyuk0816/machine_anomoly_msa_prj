package dev.study.portal.repository.anomalyHistory;

import dev.study.portal.entity.anomalyHistory.AnomalyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyHistoryRepository extends JpaRepository<AnomalyHistory, Long> {
    List<AnomalyHistory> findByDetectedAtBetween(LocalDateTime startAt, LocalDateTime endAt);
}
