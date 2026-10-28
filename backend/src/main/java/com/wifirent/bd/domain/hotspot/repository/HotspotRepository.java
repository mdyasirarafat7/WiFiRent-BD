package com.wifirent.bd.domain.hotspot.repository;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotspotRepository extends JpaRepository<Hotspot, Long> {
    List<Hotspot> findByHostId(Long hostId);

    @Query("SELECT h FROM Hotspot h WHERE h.host.user.id = :hostId")
    List<Hotspot> findByHostUserId(@Param("hostId") Long hostId);

    @Query("SELECT h FROM Hotspot h WHERE h.host.user.id = :hostId")
    Page<Hotspot> findByHostUserId(@Param("hostId") Long hostId, Pageable pageable);

    Page<Hotspot> findByStatusAndAvailableOrderByCreatedAtDesc(HotspotStatus status, boolean available, Pageable pageable);

    @Query(value = """
        SELECT * FROM hotspots h
        WHERE h.status = :status
          AND h.is_available = true
          AND (:latitude IS NULL OR 6371000 * acos(
            cos(radians(:latitude)) * cos(radians(h.latitude)) *
            cos(radians(h.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(h.latitude))
          )) <= :radiusMeters
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Hotspot> findAvailableNearby(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusMeters") Double radiusMeters,
        @Param("status") String status);

    boolean existsBySsid(String ssid);
}
