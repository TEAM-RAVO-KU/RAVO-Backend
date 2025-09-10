package ravo.ravobackend.global.domain;

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
