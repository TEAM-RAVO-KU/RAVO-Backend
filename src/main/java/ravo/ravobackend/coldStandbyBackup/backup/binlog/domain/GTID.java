package ravo.ravobackend.coldStandbyBackup.backup.binlog.domain;

import lombok.Getter;

@Getter
public class GTID {
    private final String uuid;   // 서버 UUID
    private final long start;    // 시작 트랜잭션 번호
    private final long end;      // 끝 트랜잭션 번호 (단일값이면 start==end)

    private GTID(String uuid, long start, long end) {
        this.uuid = uuid;
        this.start = start;
        this.end = end;
    }

    public boolean isEmpty() {
        return start > end;
    }

    /**
     * GTID 문자열 파싱 (예: "UUID:1-100" or "UUID:105")
     */
    public static GTID parse(String gtidStr) {
        String[] parts = gtidStr.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid GTID format: " + gtidStr);
        }
        String uuid = parts[0];
        String range = parts[1];

        if (range.contains("-")) {
            String[] nums = range.split("-");
            long start = Long.parseLong(nums[0]);
            long end = Long.parseLong(nums[1]);
            return new GTID(uuid, start, end);
        } else {
            long val = Long.parseLong(range);
            return new GTID(uuid, val, val);
        }
    }

    /**
     * lastSynced ~ current 범위 차이를 반환
     */
    public GTID subtract(GTID lastSynced) {
        if (!this.uuid.equals(lastSynced.uuid)) {
            throw new IllegalArgumentException("UUID mismatch: " + this.uuid + " vs " + lastSynced.uuid);
        }

        return new GTID(this.uuid, lastSynced.end+1, this.end);
    }

    @Override
    public String toString() {
        return (start == end)
                ? uuid + ":" + start
                : uuid + ":" + start + "-" + end;
    }
}
