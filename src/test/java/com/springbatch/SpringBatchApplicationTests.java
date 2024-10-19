package com.springbatch;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;


@SpringBatchTest
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SpringBatchApplicationTests {

	// @Autowired
	// private Job job;

	// @Autowired
	// private JobLauncher jobLauncher;

 	@Autowired
    private JdbcTemplate jdbcTemplate;


	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;
	
	@BeforeEach
	void setUp(){
		this.jobRepositoryTestUtils.removeJobExecutions();
		JdbcTestUtils.deleteFromTableWhere(this.jdbcTemplate, "ORDERS", "id>33");
		// JdbcTestUtils.deleteFromTables(this.jdbcTemplate, "BILLING_DATA");
	}

	@AfterEach
	void tearDown(){

	}

	// @Test
	// void testJobExecution(CapturedOutput output) throws Exception {
	// 	// Given
	// 	// var jobParameters = new JobParametersBuilder()
	// 	// .addString("input.file", 	"/some/input/file")
	// 	// .addString("file.format", "csv", false)
	// 	// .toJobParameters();
	// 	var jobParameters = this.jobLauncherTestUtils.getUniqueJobParametersBuilder()
	// 	.addString("input.file", 	"/some/input/file")
	// 	.addString("file.format", "csv", false)
	// 	.toJobParameters();
	// 	// When
	// 	var jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
	// 	// Then
	// 	assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
	// 	assertTrue(output.getOut().contains("processing orders information from file /some/input/file"));

	// }
	@Test
	void testJobExecution() throws Exception{
		JobParameters jobParameters = new JobParametersBuilder()
		.addString("input.file", "input/orders_part1.csv")
				.addString("output.file", "staging/orders_processed_2.csv")
				.addJobParameter("data.leastAvgPrice", BigDecimal.valueOf(200), BigDecimal.class)
				.addString("data.accountKey","account_4")
		.toJobParameters();
		Path ordersReport = Paths.get("staging", "orders_processed_2.csv");

		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);
//		Path billingReport = Paths.get("staging", "billing-report-2023-01.csv");
		assertTrue(Files.exists(Paths.get("staging", "orders_part1.csv")));
		assertTrue(Files.exists(ordersReport));
		assertEquals(39, Files.lines(ordersReport).count());
//		assertTrue(Files.exists(billingReport));
//		assertEquals(781, Files.lines(billingReport).count());
		assertEquals(ExitStatus.COMPLETED,jobExecution.getExitStatus());
		assertEquals(1000, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "ORDERS", "id>33"));
		// Assertions.assertEquals(1000, JdbcTestUtils.countRowsInTable(jdbcTemplate, "BILLING_DATA"));
	}


}
