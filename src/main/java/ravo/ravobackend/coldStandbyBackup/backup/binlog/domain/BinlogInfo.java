package ravo.ravobackend.coldStandbyBackup.backup.binlog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogInfo {
    private String logName;       // binlog 파일명 (예: mysql-bin.000001)
    private Long fileSize;        // 파일 크기
    private Boolean encrypted;    // 암호화 여부
}
