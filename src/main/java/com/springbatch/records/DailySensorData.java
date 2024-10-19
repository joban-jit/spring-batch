package com.springbatch.records;

import java.util.List;

public record DailySensorData(
  String date, 
  List<Double> measurements
){}
