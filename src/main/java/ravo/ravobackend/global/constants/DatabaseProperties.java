package ravo.ravobackend.global.constants;

import lombok.Data;

import java.io.Serializable;

@Data
public class DatabaseProperties implements Serializable {

    private String host;

    private String port;

    private String database;

    private String username;

    private String password;

    private String driverClassName;
}
