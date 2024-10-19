package com.springbatch.repository;



import org.springframework.data.repository.CrudRepository;

import com.springbatch.entity.Orders;

public interface OrdersRepository extends CrudRepository<Orders, Long> {
    
     
}
