package com.wifirent.bd.config;

import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.hotspot.Hotspot.HotspotStatus;
import com.wifirent.bd.domain.hotspot.repository.HotspotRepository;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.packageplan.PackageType;
import com.wifirent.bd.domain.packageplan.repository.PackagePlanRepository;
import com.wifirent.bd.domain.user.HostProfile;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.HostProfileRepository;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.domain.wallet.Wallet;
import com.wifirent.bd.domain.wallet.repository.WalletRepository;
import com.wifirent.bd.service.WalletService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDemoData(
            UserRepository userRepository,
            HostProfileRepository hostProfileRepo,
            HotspotRepository hotspotRepository,
            PackagePlanRepository packageRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            WalletService walletService) {

        return args -> {
            if (userRepository.count() == 0) {
                // Create host user
                User hostUser = new User("Demo Host", "host@demo.com",
                    passwordEncoder.encode("password123"), UserRole.HOST);
                hostUser = userRepository.save(hostUser);

                // Create host profile
                HostProfile hostProfile = new HostProfile();
                hostProfile.setUser(hostUser);
                hostProfile.setBusinessName("Demo Host Business");
                hostProfile.setApproved(true);
                hostProfile.setStatus(HostProfile.HostStatus.APPROVED);
                hostProfile = hostProfileRepo.save(hostProfile);

                // Create wallet for host
                walletService.ensureWallet(hostUser.getId());

                // Create hotspot
                Hotspot hotspot = new Hotspot();
                hotspot.setName("Campus Library Wi-Fi");
                hotspot.setDescription("Free Wi-Fi near the computer science building");
                hotspot.setLatitude(BigDecimal.valueOf(23.8103));
                hotspot.setLongitude(BigDecimal.valueOf(90.4125));
                hotspot.setSsid("CampusLibrary-WiFi");
                hotspot.setApproximateArea("Computer Science Building");
                hotspot.setAvailable(true);
                hotspot.setStatus(HotspotStatus.ACTIVE);
                hotspot.setHost(hostProfile);
                hotspot = hotspotRepository.save(hotspot);

                // Create package plans
                PackagePlan plan1 = new PackagePlan();
                plan1.setPriceMinorUnits(500L);
                plan1.setPackageType(PackageType.TIME_BASED);
                plan1.setDurationMinutes(60);
                plan1.setActive(true);
                plan1.setHotspot(hotspot);
                packageRepository.save(plan1);

                PackagePlan plan2 = new PackagePlan();
                plan2.setPriceMinorUnits(1000L);
                plan2.setPackageType(PackageType.DATA_BASED);
                plan2.setDataQuotaBytes(1073741824L);
                plan2.setActive(true);
                plan2.setHotspot(hotspot);
                packageRepository.save(plan2);

                PackagePlan plan3 = new PackagePlan();
                plan3.setPriceMinorUnits(2000L);
                plan3.setPackageType(PackageType.TIME_AND_DATA);
                plan3.setDurationMinutes(120);
                plan3.setDataQuotaBytes(2147483648L);
                plan3.setActive(true);
                plan3.setHotspot(hotspot);
                packageRepository.save(plan3);

                // Create customer user
                User customer = new User("Demo Customer", "customer@demo.com",
                    passwordEncoder.encode("password123"), UserRole.CUSTOMER);
                customer = userRepository.save(customer);

                // Create wallet for customer
                walletService.ensureWallet(customer.getId());

                System.out.println("Demo data seeded successfully!");
                System.out.println("Host: host@demo.com / password123");
                System.out.println("Customer: customer@demo.com / password123");
                System.out.println("Hotspot: Campus Library Wi-Fi");
                System.out.println("Packages: 500 credits/1hr, 1000 credits/1GB, 2000 credits/2hr+2GB");
            }
        };
    }
}
