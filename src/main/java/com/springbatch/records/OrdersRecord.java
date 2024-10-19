package com.springbatch.records;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdersRecord(
    long id,
    String accountKey,
    LocalDateTime initiatedDatetime,
    String orderStatus,
    String symbol,
    BigDecimal avgPrice,
    int quantity,
    LocalDateTime lastUpdatedDatetime
) {

}