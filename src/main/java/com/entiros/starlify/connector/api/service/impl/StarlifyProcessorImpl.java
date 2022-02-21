package com.entiros.starlify.connector.api.service.impl;

import com.entiros.starlify.connector.api.dto.*;
import com.entiros.starlify.connector.api.dto.kong.Consumer;
import com.entiros.starlify.connector.api.dto.kong.KongResponse;
import com.entiros.starlify.connector.api.dto.kong.KongServiceDto;
import com.entiros.starlify.connector.api.dto.starlify.*;
import com.entiros.starlify.connector.api.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarlifyProcessorImpl implements StarlifyProcessor {

    private final StarlifyService starlifyService;
    private final KongService kongService;

    private final Map<String, Map<String, NetworkSystem>> systemCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, NetworkSystem>> consumerSystemCache = new ConcurrentHashMap<>();
    private final Map<String, RequestItem> statusMap = new ConcurrentHashMap<>();

    private void processRequestIntrnl(Request request) {
        ((RequestItem)request).setStatus(RequestItem.Status.IN_PROCESS);
        KongResponse<KongServiceDto> services = kongService.getServices();

        List<NetworkSystem> systems = starlifyService.getSystems(request);
        this.populateSystems(request, systems);
        this.handleConsumers(request);
        String flowId = getFlowId(request);
        Map<String, NetworkSystem> existingSystems = systemCache.get(request.getNetworkId());
        List<KongServiceDto> data = services.getData();
        data.forEach(s -> {
            try {
                log.info("Started service:"+s.getName()+" id:"+s.getId());
                NetworkSystem networkSystem = existingSystems != null ? existingSystems.get(s.getName()) : null;
                String systemId;
                if(networkSystem == null) {
                    SystemDto systemDto = this.createSystemDto(request, s.getName());
                    SystemRespDto systemRespDto = starlifyService.addSystem(request, systemDto);
                    systemId = systemRespDto.getId();
                    NetworkSystem newSystem = new NetworkSystem();
                    newSystem.setId(systemId);
                    newSystem.setName(s.getName());
                    updateCache(request.getNetworkId(), newSystem);
                } else {
                    systemId = networkSystem.getId();
                }

                starlifyService.addServices(request, consumerSystemCache.get(request.getNetworkId()).values(), flowId, systemId, s.getId());

                ((RequestItem)request).setStatus(RequestItem.Status.DONE);
                log.info("Completed service:"+s.getName());
            } catch (Throwable t) {
                log.error("Error while processing servic:"+s.getName(), t);
                ((RequestItem)request).setStatus(RequestItem.Status.ERROR);
            }
        });
        // clearing cache
        consumerSystemCache.remove(request.getNetworkId());
        systemCache.remove(request.getNetworkId());
    }

    private void handleConsumers(Request request) {
        KongResponse<Consumer> consumers = kongService.getConsumers();
        Map<String, NetworkSystem> existingSystems = systemCache.get(request.getNetworkId());
        List<Consumer> data = consumers.getData();
        data.forEach(c ->  {
                log.info("Started consumer:"+c.getUsername()+" id:"+c.getId());
                NetworkSystem networkSystem = existingSystems != null ? existingSystems.get(c.getUsername()) : null;
                if(networkSystem == null) {
                    SystemDto systemDto = this.createSystemDto(request, c.getUsername());
                    SystemRespDto systemRespDto = starlifyService.addSystem(request, systemDto);
                    networkSystem = new NetworkSystem();
                    networkSystem.setId(systemRespDto.getId());
                    networkSystem.setName(c.getUsername());
                }
            Map<String, NetworkSystem> cachedConsumers = consumerSystemCache.get(request.getNetworkId());
            if(cachedConsumers == null) {
                cachedConsumers = new ConcurrentHashMap<>();
                consumerSystemCache.put(request.getNetworkId(), cachedConsumers);
            }
            cachedConsumers.put(networkSystem.getId(), networkSystem);
            log.info("Done consumer:"+c.getUsername());

        });
    }

    private String getFlowId(Request request) {
        if(consumerSystemCache.isEmpty()) {
            return null;
        }
        Response<FlowRespDto> flows = starlifyService.getFlows(request);
        if(flows.getContent() != null && !flows.getContent().isEmpty()) {
            for(FlowRespDto d : flows.getContent()) {
                if(d.getName().equalsIgnoreCase("Dynamically Generated Flow")) {
                    return d.getId();
                }
            }
        }
        FlowRespDto flowRespDto = starlifyService.addFlow(request);
        return flowRespDto.getId();
    }

    @Override
    public RequestItem processRequest(Request request) {
        RequestItem workItem = new RequestItem();
        workItem.setStatus(RequestItem.Status.NOT_STARTED);
        workItem.setStarlifyKey(request.getStarlifyKey());
        workItem.setApiKey(request.getApiKey());
        workItem.setNetworkId(request.getNetworkId());
        statusMap.put(request.getNetworkId(), workItem);
        CompletableFuture.runAsync(() -> {
            try{
                processRequestIntrnl(workItem);
            } catch (Throwable t) {
                log.error("Error while processing:", t);
                workItem.setStatus(RequestItem.Status.ERROR);
            }

        });
        return workItem;
    }


    @Override
    public RequestItem status(Request request) {
        return statusMap.get(request.getNetworkId());
    }

    private SystemDto createSystemDto(Request request, String name) {
        SystemDto s = new SystemDto();
        String id = UUID.randomUUID().toString();
        s.setId(id);
        s.setName(name);
        Network n = new Network();
        n.setId(request.getNetworkId());
        s.setNetwork(n);
        return s;
    }

    private synchronized void populateSystems(Request request, List<NetworkSystem> networkSystems) {
        if(networkSystems != null && !networkSystems.isEmpty()) {
            Map<String, NetworkSystem> existingSystems = systemCache.get(request.getNetworkId());
            if(existingSystems == null) {
                existingSystems = new ConcurrentHashMap<>();
                systemCache.put(request.getNetworkId(), existingSystems);
            }
            for (NetworkSystem ns : networkSystems) {
                existingSystems.put(ns.getName(), ns);
            }
        }
    }

    private synchronized void updateCache(String networkId, NetworkSystem networkSystem) {
        if(networkSystem != null) {
            Map<String, NetworkSystem> existingSystems = systemCache.get(networkId);
            if(existingSystems == null) {
                existingSystems = new ConcurrentHashMap<>();
                systemCache.put(networkId, existingSystems);
            }
            existingSystems.put(networkSystem.getName(), networkSystem);
        }
    }
}
