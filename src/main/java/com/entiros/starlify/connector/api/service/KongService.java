package com.entiros.starlify.connector.api.service;

import com.entiros.starlify.connector.api.dto.kong.Consumer;
import com.entiros.starlify.connector.api.dto.kong.KongResponse;
import com.entiros.starlify.connector.api.dto.kong.KongServiceDto;
import com.entiros.starlify.connector.api.dto.kong.Route;

public interface KongService {
    public KongResponse<KongServiceDto> getServices();
    public KongResponse<Route> getServiceRoutes(String serviceId);
    public KongResponse<Consumer> getConsumers();
}
