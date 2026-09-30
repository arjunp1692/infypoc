package com.sentinelflow.persistence;

import com.sentinelflow.domain.AlertStatus;
import com.sentinelflow.domain.Severity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {

    Optional<AlertEntity> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select a from AlertEntity a
            where (:severity is null or a.severity = :severity)
              and (:status is null or a.status = :status)
              and (:source is null or a.source = :source)
              and (:from is null or a.lastSeen >= :from)
              and (:to is null or a.lastSeen <= :to)
            order by a.lastSeen desc
            """)
    List<AlertEntity> search(
            @Param("severity") Severity severity,
            @Param("status") AlertStatus status,
            @Param("source") String source,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("""
            select a from AlertEntity a
            where a.asset = :asset
              and a.eventType = :eventType
              and a.status not in :closed
              and a.lastSeen <= :eventTime
              and a.lastSeen >= :windowStart
            order by a.lastSeen desc
            """)
    List<AlertEntity> findSuppressionCandidates(
            @Param("asset") String asset,
            @Param("eventType") String eventType,
            @Param("closed") Collection<AlertStatus> closed,
            @Param("eventTime") Instant eventTime,
            @Param("windowStart") Instant windowStart);
}
