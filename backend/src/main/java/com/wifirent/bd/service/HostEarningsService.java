package com.wifirent.bd.service;

import com.wifirent.bd.domain.ledger.HostLedgerEntry;
import com.wifirent.bd.domain.ledger.HostLedgerEntry.EntryType;
import com.wifirent.bd.domain.ledger.repository.HostLedgerEntryRepository;
import com.wifirent.bd.domain.payment.Payment;
import com.wifirent.bd.domain.payment.PaymentStatus;
import com.wifirent.bd.domain.payment.repository.PaymentRepository;
import com.wifirent.bd.domain.rental.RentalOrder;
import com.wifirent.bd.domain.rental.repository.RentalOrderRepository;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.user.repository.UserRepository;

import com.wifirent.bd.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class HostEarningsService {

    private final HostLedgerEntryRepository ledgerRepository;
    private final PaymentRepository paymentRepository;
    private final RentalOrderRepository orderRepository;
    private final UserRepository userRepository;

    public HostEarningsService(HostLedgerEntryRepository ledgerRepository,
                               PaymentRepository paymentRepository,
                               RentalOrderRepository orderRepository,
                               UserRepository userRepository) {
        this.ledgerRepository = ledgerRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public HostEarningsDto getEarnings(Long hostId) {
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        Long gross = ledgerRepository.sumByHostAndType(hostId, EntryType.EARNING);
        Long commission = ledgerRepository.sumByHostAndType(hostId, EntryType.COMMISSION);
        Long earnings = ledgerRepository.sumByHostAndType(hostId, EntryType.EARNING)
            - (ledgerRepository.sumByHostAndType(hostId, EntryType.COMMISSION) != null
            ? ledgerRepository.sumByHostAndType(hostId, EntryType.COMMISSION) : 0);

        return new HostEarningsDto(
            hostId,
            gross != null ? gross : 0,
            commission != null ? commission : 0,
            earnings != null ? earnings : 0,
            "BDT"
        );
    }

    @Transactional(readOnly = true)
    public Object getLedger(Long hostId, Pageable pageable) {
        return ledgerRepository.findByHostId(hostId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Object getPayouts(Long hostId, Pageable pageable) {
        return ledgerRepository.findByHostId(hostId, pageable).map(this::toDto);
    }

    @Transactional
    public HostLedgerEntry recordEarning(Long hostId, Long amountMinorUnits,
                                         String referenceId, String referenceType, String description) {
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        HostLedgerEntry entry = new HostLedgerEntry();
        entry.setHost(host);
        entry.setEntryType(EntryType.EARNING);
        entry.setAmountMinorUnits(amountMinorUnits);
        entry.setCurrency("BDT");
        entry.setReferenceId(referenceId);
        entry.setReferenceType(referenceType);
        entry.setDescription(description);
        return ledgerRepository.save(entry);
    }

    @Transactional
    public HostLedgerEntry recordCommission(Long hostId, Long amountMinorUnits,
                                            String referenceId, String description) {
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new ResourceNotFoundException("Host not found"));

        HostLedgerEntry entry = new HostLedgerEntry();
        entry.setHost(host);
        entry.setEntryType(EntryType.COMMISSION);
        entry.setAmountMinorUnits(-amountMinorUnits);
        entry.setCurrency("BDT");
        entry.setReferenceId(referenceId);
        entry.setReferenceType("PAYMENT");
        entry.setDescription(description);
        return ledgerRepository.save(entry);
    }

    private HostLedgerEntryDto toDto(HostLedgerEntry e) {
        return new HostLedgerEntryDto(
            e.getId(), e.getEntryType().name(), e.getAmountMinorUnits(),
            e.getCurrency(), e.getReferenceId(), e.getReferenceType(),
            e.getDescription(), e.getCreatedAt()
        );
    }
}


