package com.entiros.starlify.connector.api.dto.kong;

import lombok.Data;

@Data
public class KongServiceDto extends KongBase {
    private long read_timeout;
    private String host;
    private String path;
    private String protocol;
    private long retries;
    private long write_timeout;
    private boolean enabled;
    private int port;
    private String tls_verify;
    private Object client_certificate;
    private String name;
    private String tls_verify_depth;
    private long connect_timeout;
    private Object ca_certificates;
}
