package ravo.ravobackend.coldStandbyRecovery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ColdStandbyRecoveryService {

    private final JobLauncher jobLauncher;

    private final Job coldStandbyRecoveryJob;

    public void recover(String fileName) throws Exception {
        long ts = System.currentTimeMillis();
        jobLauncher.run(coldStandbyRecoveryJob,
                new JobParametersBuilder()
                        .addString("dumpFile", fileName)
                        .addLong("ts", ts)
                        .toJobParameters()
        );
    }
}
