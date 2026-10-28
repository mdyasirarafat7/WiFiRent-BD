package com.wifirent.bd.domain.hotspot.repository;

import com.wifirent.bd.domain.hotspot.GatewayDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GatewayDeviceRepository extends JpaRepository<GatewayDevice, Long> {
    Optional<GatewayDevice> findByDeviceId(String deviceId);
    List<GatewayDevice> findByHotspotId(Long hotspotId);

    @Query("SELECT g FROM GatewayDevice g WHERE g.hotspot.host.user.id = :hostId")
    List<GatewayDevice> findByHotspotHostUserId(@Param("hostId") Long hostId);
}
