package kr.huni.batctx.job.order;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kr.huni.batctx.job.order.domain.PendingOrder;
import kr.huni.batctx.job.order.domain.ProcessedOrder;

@Configuration
public class PendingOrderJobConfig {

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job pendingOrderJob(JobRepository jobRepository, Step pendingOrderStep) {
        return new JobBuilder("pendingOrderJob", jobRepository)
                .start(pendingOrderStep)
                .build();
    }

    @Bean
    public Step pendingOrderStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemReader<PendingOrder> pendingOrderItemReader,
                                 ItemProcessor<PendingOrder, ProcessedOrder> thirdPartyOrderProcessor,
                                 ItemWriter<ProcessedOrder> processedOrderItemWriter) {
        return new StepBuilder("pendingOrderStep", jobRepository)
                .<PendingOrder, ProcessedOrder>chunk(CHUNK_SIZE, transactionManager)
                .reader(pendingOrderItemReader)
                .processor(thirdPartyOrderProcessor)
                .writer(processedOrderItemWriter)
                .build();
    }
}
