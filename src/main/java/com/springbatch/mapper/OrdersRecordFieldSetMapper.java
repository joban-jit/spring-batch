package com.springbatch.mapper;

import com.springbatch.records.OrdersRecord;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrdersRecordFieldSetMapper implements FieldSetMapper<OrdersRecord> {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public OrdersRecord mapFieldSet(FieldSet fieldSet) throws BindException {
        return new OrdersRecord(
                fieldSet.readLong("id"),
                fieldSet.readString("accountKey"),
                LocalDateTime.parse(fieldSet.readString("initiatedDatetime"), DATE_TIME_FORMATTER),
                fieldSet.readString("orderStatus"),
                fieldSet.readString("symbol"),
                fieldSet.readBigDecimal("avgPrice"),
                fieldSet.readInt("quantity"),
                LocalDateTime.parse(fieldSet.readString("lastUpdatedDatetime"), DATE_TIME_FORMATTER)
        );
    }
}
