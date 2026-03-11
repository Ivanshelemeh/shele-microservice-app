package com.example.compositeservice.service.impl;

import com.example.compositeservice.model.CompositeAggregate;
import com.example.compositeservice.model.RecommendationSummary;
import com.example.compositeservice.model.integration.Customer;
import com.example.compositeservice.model.integration.OrderRecommendation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CompositeServiceImpl implements CompositeService {

    private final String bikeOrderUrl;
    private final String bikeUserUrl;
    @Value("${app.bike-order-service.host}")
    private String orderServiceHost;
    @Value("${app.bike-order-service.port}")
    private String orderServicePort;

    private static final String REQ_URL_ORDER = "/rest/api/v1/order-recommendation/";
    private static final String REQ_URL_CUSTOMER = "/rest/api/v1/customers/";
    private final CompositeService compositeService;
    private final WebClient webClient;

    @Autowired
    public CompositeServiceImpl(CompositeService compositeService, WebClient.Builder webClient) {
        this.compositeService = compositeService;
        this.webClient = webClient.build();
        bikeOrderUrl = "http://" + orderServiceHost + ":" + orderServicePort + REQ_URL_ORDER;
        bikeUserUrl = "http://" + orderServiceHost + ":" + orderServicePort + REQ_URL_CUSTOMER;

    }


    @Override
    public CompositeAggregate getAggregetaModel(@NonNull String requestId) {
        String requestCustomerURL = bikeUserUrl + requestId;
        String requestOrderRecommendationURL = bikeOrderUrl + requestId;

        final var customer = webClient.get()
                .uri(requestCustomerURL)
                .retrieve()
                .bodyToMono(Customer.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer respond fail")))
                .map(c -> new Customer(
                        c.customerID(),
                        c.customerName()
                ))
                .blockOptional()
                .orElseThrow(() -> new NotFoundException("Customer cannot process"));

        final var orderRecommendResponse = webClient.get()
                .uri(requestOrderRecommendationURL)
                .retrieve()
                .bodyToMono(OrderRecommendation.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("OrderRecommendation respond fail")))
                .map(orderRecommendation -> new OrderRecommendation(
                        orderRecommendation.recommendationId(),
                        orderRecommendation.orderName(),
                        orderRecommendation.content(),
                        orderRecommendation.orderRate()

                ))
                .blockOptional()
                .orElseThrow();

        return new CompositeAggregate(
                customer.customerID().toString(),
                orderRecommendResponse.orderName(),
                listFromModel(orderRecommendResponse)


        );
    }

    private List<RecommendationSummary> listFromModel(OrderRecommendation orderRecommendation) {
        if (ObjectUtils.isEmpty(orderRecommendation)) {
            return Collections.emptyList();
        }
        RecommendationSummary summary = new RecommendationSummary(
                createIdFromString(orderRecommendation.recommendationId()),
                orderRecommendation.content(),
                orderRecommendation.orderRate()
        );
        return List.of(summary);
    }

    private Integer createIdFromString(@org.springframework.lang.NonNull String input) {
        return Integer.parseInt(input);

    }
}
