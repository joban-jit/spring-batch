package com.springbatch.processor;

import com.springbatch.records.OrdersRecord;
import org.springframework.batch.item.ItemProcessor;

public class OrdersProcessor implements ItemProcessor<OrdersRecord, OrdersRecord> {
    @Override
    public OrdersRecord process(OrdersRecord item) throws Exception {
        if(!item.orderStatus().equals("completed"))
            return null;
        return item;
    }
}
