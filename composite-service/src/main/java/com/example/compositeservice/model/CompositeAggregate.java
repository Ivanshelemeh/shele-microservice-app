package com.example.compositeservice.model;

import java.util.List;

public record CompositeAggregate(
        String userId,
        String orderName,
        List<RecommendationSummary> summary
 ) {
}
