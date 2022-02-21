package com.entiros.starlify.connector.api.dto.kong;

import lombok.Data;

import java.util.List;

@Data
public class KongResponse<T> {
    private List<T> data;
    private String next;
}
