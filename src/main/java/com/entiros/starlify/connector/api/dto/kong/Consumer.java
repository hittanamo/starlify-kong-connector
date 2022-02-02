package com.entiros.starlify.connector.api.dto.kong;

import lombok.Data;

@Data
public class Consumer extends KongBase {
    private String username;
    private String custom_id;
}
