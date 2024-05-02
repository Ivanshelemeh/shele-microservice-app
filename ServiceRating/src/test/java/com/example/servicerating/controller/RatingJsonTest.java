package com.example.servicerating.controller;

import com.example.servicerating.data.Rating;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonbTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class RatingJsonTest {

    @Autowired
    private JacksonTester<Rating> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        var rating = new Rating("34","Fully story",23.55);
        var jsoncontent = jacksonTester.write(rating);
        assertThat(jsoncontent).extractingJsonPathStringValue("@.id")
                .isEqualTo(rating.getId());
        assertThat(jsoncontent).extractingJsonPathStringValue("@.description")
                .isEqualTo(rating.getDescription());
        assertThat(jsoncontent).extractingJsonPathNumberValue("@.rate")
                .isEqualTo(rating.getRate());
    }
}
