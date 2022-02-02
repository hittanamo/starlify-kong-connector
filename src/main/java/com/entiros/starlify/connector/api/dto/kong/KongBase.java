package com.entiros.starlify.connector.api.dto.kong;

import lombok.Data;

@Data
public class KongBase {
    private long created_at;
    private long updated_at;
    private String id;
    private Object tags;
}
