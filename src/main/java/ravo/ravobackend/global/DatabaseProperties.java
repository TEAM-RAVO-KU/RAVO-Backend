package ravo.ravobackend.global;

import lombok.Data;

@Data
public class DatabaseProperties {

    private String host;

    private String port;

    private String database;

    private String username;

    private String password;

    private String driverClassName;
}
