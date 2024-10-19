package com.springbatch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment for primary key
    private Long id;

    @Column(name = "account_key", nullable = false)
    private String accountKey;

    @Column(name = "initiated_datetime", nullable = false)
    private LocalDateTime initiatedDatetime;

    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "avg_price", nullable = false)
    private BigDecimal avgPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "last_updated_datetime", nullable = false)
    private LocalDateTime lastUpdatedDatetime;


}