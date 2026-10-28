package com.wifirent.bd.dto;

import java.util.List;

public record WalletTransactionListDto(
    List<WalletTransactionDto> content,
    Integer page, Integer size, Long totalElements, Integer totalPages
) {}
