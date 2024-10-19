package com.springbatch.tasklet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

// import lombok.extern.slf4j.Slf4j;


// @Slf4j
public class FilePreparationTasklet implements Tasklet{

    @Override
    @Nullable
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
        // log.info("got parameters: "+jobParameters.toString());
        String inputFile = jobParameters.getString("input.file");
        // log.info(inputFile);
        Path source = Paths.get(inputFile);
        Path targetDirectory = Paths.get("staging");
        Path targetFile = targetDirectory.resolve(source.getFileName());
        if (!Files.exists(targetDirectory))
        {
            Files.createDirectory(targetDirectory);
            System.out.println("Created staging directory");
        }
        Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);
        return RepeatStatus.FINISHED;
    }
    
}
