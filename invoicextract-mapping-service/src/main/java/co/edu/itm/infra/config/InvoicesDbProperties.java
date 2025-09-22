package co.edu.itm.infra.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoicesDbProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
