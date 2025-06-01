package ravo.ravobackend.global.util;

public class JdbcUrlParser {

    public static class ParsedResult {
        private final String host;
        private final String port;
        private final String databaseName;

        public ParsedResult(String host, String port, String databaseName) {
            this.host = host;
            this.port = port;
            this.databaseName = databaseName;
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public String getDatabaseName() {
            return databaseName;
        }
    }

    /**
     * 주어진 JDBC URL에서 host, port, databaseName을 꺼내 반환한다.
     *
     * @param jdbcUrl "jdbc:<subprotocol>://host[:port]/database[?<...>]" 형태의 문자열
     * @return host, port, databaseName을 담은 ParsedResult 객체
     * @throws IllegalArgumentException URL 형식이 예상과 다를 경우
     */
    public static ParsedResult parse(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("올바른 JDBC URL이 아닙니다: " + jdbcUrl);
        }

        // 1) "jdbc:" 이후부터 "://"까지 건너뛰기
        int idxProtocol = jdbcUrl.indexOf("://");
        if (idxProtocol < 0) {
            throw new IllegalArgumentException("JDBC URL에 '://' 구분자가 없습니다: " + jdbcUrl);
        }
        // "jdbc:subprotocol" 부분은 무시 → "://" 바로 뒤부터 파싱 시작
        String afterSlashSlash = jdbcUrl.substring(idxProtocol + 3); // host[:port]/dbName?...

        // 2) 파라미터(?) 또는 세미콜론(;) 구분자 이전 부분만 가져오기
        //    예: "localhost:3306/mydb?useSSL=false" → "localhost:3306/mydb"
        //    SQLServer처럼 세미콜론을 사용하는 경우도 대비: "server:1433;databaseName=..."
        int idxParam = indexOfFirst(afterSlashSlash, '?', ';');
        String core = idxParam >= 0
                ? afterSlashSlash.substring(0, idxParam)
                : afterSlashSlash;

        // 3) "/" 기준으로 "호스트[:포트]" 와 "데이터베이스 이름" 분리
        String hostPortPart;
        String dbPart;
        int idxSlash = core.indexOf('/');
        if (idxSlash < 0) {
            // "/"가 없다면 호스트 정보만 있고, 데이터베이스 이름은 비어있다고 간주
            hostPortPart = core;
            dbPart = "";
        } else {
            hostPortPart = core.substring(0, idxSlash);
            dbPart = core.substring(idxSlash + 1);
        }

        // 4) "호스트[:포트]"를 ":"로 분리
        String host;
        String port;
        int idxColon = hostPortPart.indexOf(':');
        if (idxColon < 0) {
            host = hostPortPart;
            port = null; // URL에 포트가 명시되지 않았으면 null
        } else {
            host = hostPortPart.substring(0, idxColon);
            port = hostPortPart.substring(idxColon + 1);
        }

        // 5) 최종 데이터베이스 이름
        String databaseName = dbPart == null ? "" : dbPart;

        return new ParsedResult(host, port, databaseName);
    }

    /**
     * 주어진 문자열에서 첫 번째로 등장하는 문자 idx를 찾되, 둘 다 없는 경우 -1 반환.
     */
    private static int indexOfFirst(String source, char a, char b) {
        int idxA = source.indexOf(a);
        int idxB = source.indexOf(b);
        if (idxA < 0) return idxB;
        if (idxB < 0) return idxA;
        return Math.min(idxA, idxB);
    }
}

