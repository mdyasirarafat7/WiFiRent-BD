package com.wifirent.bd.service;

import com.wifirent.bd.domain.ledger.HostLedgerEntry;
import com.wifirent.bd.domain.ledger.HostLedgerEntry.EntryType;
import com.wifirent.bd.domain.ledger.repository.HostLedgerEntryRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.UserRole;
import com.wifirent.bd.domain.user.repository.UserRepository;
import com.wifirent.bd.dto.HostEarningsDto;
import com.wifirent.bd.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HostEarningsServiceTest {

    @Mock
    private HostLedgerEntryRepository ledgerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HostEarningsService earningsService;

    @Test
    void getEarnings_shouldCalculateCorrectly() {
        User host = new User("Host", "host@example.com", "pass", UserRole.HOST);
        host.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        when(ledgerRepository.sumByHostAndType(1L, EntryType.EARNING)).thenReturn(1000L);
        when(ledgerRepository.sumByHostAndType(1L, EntryType.COMMISSION)).thenReturn(100L);

        HostEarningsDto dto = earningsService.getEarnings(1L);

        assertEquals(1L, dto.hostId());
        assertEquals(1000L, dto.grossRevenueMinorUnits());
        assertEquals(100L, dto.platformCommissionMinorUnits());
        assertEquals(900L, dto.hostEarningsMinorUnits());
    }

    @Test
    void recordEarning_shouldCreateEntry() {
        User host = new User("Host", "host@example.com", "pass", UserRole.HOST);
        host.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        when(ledgerRepository.save(any(HostLedgerEntry.class))).thenAnswer(inv -> {
            HostLedgerEntry e = inv.getArgument(0);
            e.setId(50L);
            return e;
        });

        HostLedgerEntry entry = earningsService.recordEarning(1L, 500L, "order-1", "PAYMENT", "Rental earning");

        assertEquals(50L, entry.getId());
        assertEquals(EntryType.EARNING, entry.getEntryType());
        assertEquals(500L, entry.getAmountMinorUnits());
    }

    @Test
    void recordCommission_shouldCreateNegativeEntry() {
        User host = new User("Host", "host@example.com", "pass", UserRole.HOST);
        host.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        when(ledgerRepository.save(any(HostLedgerEntry.class))).thenAnswer(inv -> {
            HostLedgerEntry e = inv.getArgument(0);
            e.setId(51L);
            return e;
        });

        HostLedgerEntry entry = earningsService.recordCommission(1L, 100L, "order-1", "Platform commission");

        assertEquals(EntryType.COMMISSION, entry.getEntryType());
        assertEquals(-100L, entry.getAmountMinorUnits());
    }

    @Test
    void getEarnings_shouldThrowWhenHostNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> earningsService.getEarnings(999L));
    }

    @Test
    void getLedger_shouldReturnPage() {
        when(ledgerRepository.findByHostId(1L, PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(List.of()));

        var page = earningsService.getLedger(1L, PageRequest.of(0, 20));
        assertNotNull(page);
    }
}
