package ravo.ravobackend.coldStandbyBackup.backup.binlog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.BinlogInfo;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Service
public class BinlogQueryService {

    private final DataSource standbyDataSource;

    public BinlogQueryService(@Qualifier("standbyDataSource") DataSource standbyDataSource) {
        this.standbyDataSource = standbyDataSource;
    }

    /**
     * 현재 사용 중인 binlog 파일 목록 조회
     */
    private List<BinlogInfo> getBinlogFiles() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(standbyDataSource);

        List<BinlogInfo> binlogFiles = jdbcTemplate.query(
                "SHOW BINARY LOGS",
                (rs, rowNum) -> BinlogInfo.builder()
                        .logName(rs.getString("Log_name"))
                        .fileSize(rs.getLong("File_size"))
                        .encrypted(rs.getString("Encrypted").equals("Yes"))
                        .build()
        );

        log.info("Found {} binlog files", binlogFiles.size());
        return binlogFiles;
    }

    /**
     * 특정 binlog 파일부터 시작하는 첫 번째 파일 찾기
     */
    public String getFirstBinlogFile() {
        List<BinlogInfo> binlogFiles = getBinlogFiles();

        if (binlogFiles.isEmpty()) {
            throw new IllegalStateException("No binlog files found");
        }

        return binlogFiles.get(0).getLogName();
    }

}
