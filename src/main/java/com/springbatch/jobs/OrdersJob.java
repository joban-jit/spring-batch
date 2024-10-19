package com.springbatch.jobs;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.lang.NonNull;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
public class OrdersJob 
// implements Job 
{

    private final JobRepository jobRepository;

    public OrdersJob(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // @Override
    public void execute(@NonNull JobExecution jobExecution) {
        try {
            var jobParameters = jobExecution.getJobParameters();
            String inputFile = jobParameters.getString("input.file");
            // log.info("processing orders information from file "+inputFile);
            jobExecution.setStatus(BatchStatus.COMPLETED);
            jobExecution.setExitStatus(ExitStatus.COMPLETED);
        } catch (Exception exception) {
            jobExecution.addFailureException(exception);
            jobExecution.setStatus(BatchStatus.COMPLETED);
            jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription(exception.getMessage()));
        } finally {
            this.jobRepository.update(jobExecution);
        }

    }

    // @Override
    @NonNull
    public String getName() {
        return "OrdersJob";
    }

}
