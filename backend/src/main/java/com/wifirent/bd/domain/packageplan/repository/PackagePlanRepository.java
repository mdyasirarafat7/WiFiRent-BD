package com.wifirent.bd.domain.packageplan.repository;

import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.hotspot.Hotspot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackagePlanRepository extends JpaRepository<PackagePlan, Long> {
    List<PackagePlan> findByHotspotId(Long hotspotId);

    @Query("SELECT p FROM PackagePlan p WHERE p.hotspot.id = :hotspotId AND p.active = true")
    Page<PackagePlan> findActiveByHotspotId(@Param("hotspotId") Long hotspotId, Pageable pageable);
}



