package ravo.ravobackend.coldstandby.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColdStandbyService {

    private final JobLauncher jobLauncher;
    private final Job coldStandbyRecoveryJob;

    //리커버리 배치 잡을 실행하고, 실행에 사용된 파라미터(timestamp)를 리턴
    public long launchRecovery() throws Exception {
        long ts = System.currentTimeMillis();
        jobLauncher.run(
                coldStandbyRecoveryJob,
                new JobParametersBuilder()
                        .addLong("ts", ts)
                        .toJobParameters()
        );
        log.info("ColdStandbyRecoveryJob launched with ts={}", ts);
        return ts;
    }
}
