package zsj.mockmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池。
 *
 * <p>简历摘要要调本地 Ollama，一次十几秒。放在上传请求里同步做，用户会等到超时，
 * 所以丢到后台线程跑。
 *
 * <p>这里特意只开 <b>1 个线程</b>：本地 Ollama 只有一张显卡，并发两个请求不会更快，
 * 只会互相抢显存、一起变慢，还容易 OOM。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String RESUME_EXECUTOR = "resumeTaskExecutor";

    @Bean(RESUME_EXECUTOR)
    public Executor resumeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("resume-summary-");
        // 队列满了就让提交任务的线程自己跑，形成背压，而不是丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
