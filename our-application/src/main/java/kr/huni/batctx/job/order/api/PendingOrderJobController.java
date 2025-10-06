package kr.huni.batctx.job.order.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/pending-orders")
public class PendingOrderJobController {

    private static final Logger log = LoggerFactory.getLogger(PendingOrderJobController.class);

    private final JobLauncher jobLauncher;
    private final Job pendingOrderJob;

    public PendingOrderJobController(JobLauncher jobLauncher, Job pendingOrderJob) {
        this.jobLauncher = jobLauncher;
        this.pendingOrderJob = pendingOrderJob;
    }

    @PostMapping("/run")
    public ResponseEntity<JobLaunchResponse> runPendingOrderJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("triggerTimestamp", System.currentTimeMillis())
                .toJobParameters();
        try {
            JobExecution jobExecution = jobLauncher.run(pendingOrderJob, jobParameters);
            ExitStatus exitStatus = jobExecution.getExitStatus();
            return ResponseEntity.ok(new JobLaunchResponse(jobExecution.getId(), exitStatus.getExitCode()));
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobRestartException | JobParametersInvalidException ex) {
            log.warn("Unable to trigger pendingOrderJob", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new JobLaunchResponse(null, "FAILED"));
        }
    }

    public record JobLaunchResponse(Long jobId, String exitCode) {
    }
}
