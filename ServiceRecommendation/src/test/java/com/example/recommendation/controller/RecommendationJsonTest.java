package com.example.recommendation.controller;

import com.example.recommendation.data.Recommendation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class RecommendationJsonTest {

    @Autowired
    private JacksonTester<Recommendation> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        var rating = new Recommendation("34", "Fully story", 23.55);
        var jsonContent = jacksonTester.write(rating);
        assertThat(jsonContent).extractingJsonPathStringValue("@.id")
                .isEqualTo(rating.getId());
        assertThat(jsonContent).extractingJsonPathStringValue("@.description")
                .isEqualTo(rating.getDescription());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.rate")
                .isEqualTo(rating.getRate());
    }
}
