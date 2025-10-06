package kr.huni.batctx.job.order.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PendingOrderJobController.class)
class PendingOrderJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean(name = "pendingOrderJob")
    private Job pendingOrderJob;

    @Test
    void triggersJobAndReturnsExecutionMetadata() throws Exception {
        JobExecution execution = new JobExecution(123L);
        execution.setExitStatus(ExitStatus.COMPLETED);

        when(jobLauncher.run(eq(pendingOrderJob), any(JobParameters.class))).thenReturn(execution);

        mockMvc.perform(post("/api/jobs/pending-orders/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(123))
                .andExpect(jsonPath("$.exitCode").value("COMPLETED"));
    }
}
