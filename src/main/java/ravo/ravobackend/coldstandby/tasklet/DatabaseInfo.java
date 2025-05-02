package ravo.ravobackend.coldstandby.tasklet;

import lombok.Getter;

@Getter
public class DatabaseInfo {
    private String host;
    private String port;
    private String databaseName;
    private String username;
    private String password;

    private DatabaseInfo(String host, String port, String databaseName, String username, String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    public static DatabaseInfo from(String url,
                                    String username,
                                    String password) {

        String hostPortDb = url.substring("jdbc:mysql://".length()).split("\\?")[0];
        String host = hostPortDb.split(":")[0];
        String port = hostPortDb.split(":")[1].split("/")[0];
        String dbName = hostPortDb.substring(hostPortDb.lastIndexOf('/') + 1);

        return new DatabaseInfo(host, port, dbName, username, password);
    }
}
