package ravo.ravobackend.coldstandby;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "backup")
@Getter
@Setter
public class BackupProps {
    private Path dumpDir;
    private String mysqldumpExe;
}
