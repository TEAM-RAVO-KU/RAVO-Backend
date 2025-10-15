package ravo.ravobackend.legacy.hotStandbyRecovery;

import java.util.List;

/**
 * Sql 큐 관리(버퍼링) 책임을 추상화환 인터페이스
 */
public interface SqlQueueService {

    void enqueue(String sql);
}
