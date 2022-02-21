package com.entiros.starlify.connector.api.dto.kong;

import lombok.Data;

import java.util.List;

@Data
public class Route extends KongBase {
    private KongServiceDto kongServiceDto;
    private Object headers;
    private List<String> hosts;
    private Boolean request_buffering;
    private List<String> paths;
    private List<String> methods;
    private String sources;
    private String destinations;
    private boolean preserve_host;
    private boolean strip_path;
    private int https_redirect_status_code;
    private int regex_priority;
    private List<String> snis;
    private String name;
    private boolean response_buffering;
    private List<String> protocols;
    private String path_handling;
}
