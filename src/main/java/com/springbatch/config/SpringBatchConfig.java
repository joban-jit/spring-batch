package com.springbatch.config;

import javax.sql.DataSource;

import com.springbatch.listener.OrdersDataSkipListener;
import com.springbatch.mapper.OrdersRecordFieldSetMapper;
import com.springbatch.processor.OrdersProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.springbatch.records.OrdersRecord;
import com.springbatch.tasklet.FilePreparationTasklet;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Configuration
public class SpringBatchConfig {

    // @Bean
    // public Job job(JobRepository jobRepository){
    //     return new OrdersJob(jobRepository);
    // }

    @Bean
    public Job job(JobRepository jobRepository, Step step1, Step step2, Step step3) {
        return new JobBuilder("OrdersJob", jobRepository)
                .start(step1)
                .next(step2)
                .next(step3)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("filePreparation", jobRepository)
                .tasklet(new FilePreparationTasklet(), transactionManager)
                .build();
    }


    @Bean
    @StepScope
    public FlatFileItemReader<OrdersRecord> ordersDataFlatFileReader(
            @Value("#{jobParameters['input.file']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<OrdersRecord>()
                .name("ordersDataFlatFileReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .names("id", "accountKey", "initiatedDatetime", "orderStatus",
                        "symbol", "avgPrice", "quantity", "lastUpdatedDatetime")
                .fieldSetMapper(new OrdersRecordFieldSetMapper())
                .linesToSkip(1)
                .build();
    }


    @Bean
    public JdbcBatchItemWriter<OrdersRecord> ordersDataTableItemWriter(DataSource dataSource) {

        String sql = "insert into orders values(:id,:accountKey,:initiatedDatetime,:orderStatus,:symbol,:avgPrice,:quantity,:LastUpdatedDatetime)";
        return new JdbcBatchItemWriterBuilder<OrdersRecord>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .build();

    }



    @Bean
    public Step step2(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<OrdersRecord> ordersDataFlatFileReader,
                      ItemWriter<OrdersRecord> ordersDataTableItemWriter,
                      OrdersDataSkipListener skipListener) {
        return new StepBuilder("fileIngestion", jobRepository)
                .<OrdersRecord, OrdersRecord>chunk(100, transactionManager)
                .reader(ordersDataFlatFileReader)
                .writer(ordersDataTableItemWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .listener(skipListener)
                .build();

        //The syntax <BillingData,BillingData>chunk(...) is used to tell Spring Batch that the input and output of the step are of type BillingData, meaning that the reader will return items of type BillingData and that the writer will write items of type BillingData as well.
        //In other words, this step does not change the type of items during its execution. It is possible to change the item type if data should be transformed during the processing.
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<OrdersRecord> ordersDataTableReader(
            DataSource dataSource,
            @Value("#{jobParameters['data.leastAvgPrice']}")BigDecimal leastAvgPrice,
            @Value("#{jobParameters['data.accountKey']}")String accountKey
            ){
        String sql = """
                select * from orders 
                where avg_price>=? and account_key=?
                """;
        return new JdbcCursorItemReaderBuilder<OrdersRecord>()
                .name("ordersDataTableReader")
                .dataSource(dataSource)
                .sql(sql)
                .preparedStatementSetter((ps)->{
                    ps.setBigDecimal(1, leastAvgPrice);
                    ps.setString(2, accountKey);
                })
                .rowMapper(new DataClassRowMapper<>(OrdersRecord.class))
                .build();
    }

    @Bean
    public ItemProcessor<OrdersRecord, OrdersRecord> ordersRecordItemProcessor(){
        return new OrdersProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<OrdersRecord> ordersRecordFlatFileItemWriter(
            @Value("#{jobParameters['output.file']}") String outputFile
    ){
        return new FlatFileItemWriterBuilder<OrdersRecord>()
                .resource(new FileSystemResource(outputFile))
                .name("ordersFileWriter")
                .delimited()
                .names("id", "accountKey", "initiatedDatetime", "orderStatus",
                        "symbol", "avgPrice", "quantity", "lastUpdatedDatetime")
                .build();
    }

    @Bean
    public Step step3(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemReader<OrdersRecord> ordersDataTableReader,
                      ItemProcessor<OrdersRecord, OrdersRecord> ordersRecordItemProcessor,
                      ItemWriter<OrdersRecord> ordersRecordFlatFileItemWriter){

         return new StepBuilder("reportGeneration", jobRepository)
                 .<OrdersRecord, OrdersRecord>chunk(100, transactionManager)
                .reader(ordersDataTableReader)
                 .processor(ordersRecordItemProcessor)
                 .writer(ordersRecordFlatFileItemWriter)
                 .build();
    }

    @Bean
    @StepScope
    public OrdersDataSkipListener skipListener(
            @Value("#{jobParameters['skip.file']}") String skippedFile
    ){
        return new OrdersDataSkipListener(skippedFile);
    }

//    java -jar target/billing-job-0.0.1-SNAPSHOT.jar input.file=input/billing-2023-01.csv output.file=staging/billing-report-2023-01.csv data.year=2023 data.month=1
}
