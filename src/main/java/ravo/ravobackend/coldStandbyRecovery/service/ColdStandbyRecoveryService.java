package ravo.ravobackend.coldStandbyRecovery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ravo.ravobackend.coldStandbyRecovery.controller.request.DumpFileDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ColdStandbyRecoveryService {

    private final JobLauncher jobLauncher;

    private final Job coldStandbyRecoveryJob;

    @Value("${backup.output-dir}")
    private String dumpFolderPath;

    public void recover(String fileName) throws Exception {
        long ts = System.currentTimeMillis();
        jobLauncher.run(coldStandbyRecoveryJob,
                new JobParametersBuilder()
                        .addString("dumpFile", fileName)
                        .addLong("ts", ts)
                        .toJobParameters()
        );
    }

    public List<DumpFileDto> listDumpFiles() throws IOException {
        Path dumpDir = Paths.get(dumpFolderPath);
        if (!Files.exists(dumpDir) || !Files.isDirectory(dumpDir)) {
            throw new IOException("Dump directory not found: " + dumpFolderPath);
        }

        try {
            return Files.list(dumpDir)
                    .filter(Files::isRegularFile)
                    .map(path -> new DumpFileDto(path.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IOException("Failed to read dump folder: " + e.getMessage(), e);
        }
    }

}
