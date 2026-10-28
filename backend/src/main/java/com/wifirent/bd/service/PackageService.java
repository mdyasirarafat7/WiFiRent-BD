package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.PackageType;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.domain.user.repository.HostProfileRepository;
import com.wifirent.bd.domain.user.repository.UserRepository;

import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class PackageService {

    private final PackagePlanRepository packageRepository;
    private final HotspotRepository hotspotRepository;
    private final HostProfileRepository hostProfileRepository;
    private final UserRepository userRepository;

    public PackageService(PackagePlanRepository packageRepository,
                          HotspotRepository hotspotRepository,
                          HostProfileRepository hostProfileRepository,
                          UserRepository userRepository) {
        this.packageRepository = packageRepository;
        this.hotspotRepository = hotspotRepository;
        this.hostProfileRepository = hostProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PackagePlanDto create(Long hotspotId, Long userId,
                                            CreatePackageRequest request) {
        PackageType type;
        try {
            type = PackageType.valueOf(request.packageType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid package type");
        }

        if (type == PackageType.TIME_BASED && (request.durationMinutes() == null || request.durationMinutes() <= 0)) {
            throw new BadRequestException("Time-based packages require durationMinutes > 0");
        }
        if (type == PackageType.DATA_BASED && (request.dataQuotaBytes() == null || request.dataQuotaBytes() <= 0)) {
            throw new BadRequestException("Data-based packages require dataQuotaBytes > 0");
        }
        if (type == PackageType.TIME_AND_DATA &&
            (request.durationMinutes() == null || request.durationMinutes() <= 0) &&
            (request.dataQuotaBytes() == null || request.dataQuotaBytes() <= 0)) {
            throw new BadRequestException("Combined packages require duration or data quota");
        }
        if (request.priceMinorUnits() == null || request.priceMinorUnits() < 0) {
            throw new BadRequestException("Price must be >= 0");
        }

        Hotspot hotspot = hotspotRepository.findById(hotspotId)
            .orElseThrow(() -> new ResourceNotFoundException("Hotspot not found"));
        if (!hotspot.getHost().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not your hotspot");
        }

        PackagePlan plan = new PackagePlan();
        plan.setPriceMinorUnits(request.priceMinorUnits());
        plan.setPackageType(type);
        plan.setDurationMinutes(request.durationMinutes());
        plan.setDataQuotaBytes(request.dataQuotaBytes());
        plan.setValidityMinutes(request.validityMinutes());
        plan.setSpeedLimitKbps(request.speedLimitKbps());
        plan.setActive(true);
        plan.setHotspot(hotspot);

        return toDto(packageRepository.save(plan));
    }

    @Transactional
    public PackagePlanDto update(Long packageId, Long userId,
                                            UpdatePackageRequest request) {
        PackagePlan plan = packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        if (!plan.getHotspot().getHost().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not your package");
        }

        if (request.priceMinorUnits() != null) {
            plan.setPriceMinorUnits(request.priceMinorUnits());
        }
        if (request.packageType() != null) {
            try {
                plan.setPackageType(PackageType.valueOf(request.packageType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid package type");
            }
        }
        if (request.durationMinutes() != null) plan.setDurationMinutes(request.durationMinutes());
        if (request.dataQuotaBytes() != null) plan.setDataQuotaBytes(request.dataQuotaBytes());
        if (request.validityMinutes() != null) plan.setValidityMinutes(request.validityMinutes());
        if (request.speedLimitKbps() != null) plan.setSpeedLimitKbps(request.speedLimitKbps());
        if (request.active() != null) plan.setActive(request.active());

        return toDto(packageRepository.save(plan));
    }

    @Transactional
    public void deactivate(Long packageId) {
        PackagePlan plan = packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        plan.setActive(false);
        packageRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public PackagePlanDto getById(Long id) {
        PackagePlan plan = packageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        return toDto(plan);
    }

    private PackagePlanDto toDto(PackagePlan p) {
        return new PackagePlanDto(
            p.getId(), p.getPriceMinorUnits(), p.getPackageType().name(),
            p.getDurationMinutes(), p.getDataQuotaBytes(), p.getValidityMinutes(),
            p.getSpeedLimitKbps(), p.getActive()
        );
    }
}



