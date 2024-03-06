package com.example.servicerating.dtotests;

import com.example.servicerating.data.Rating;
import com.example.servicerating.dto.RatingDTO;
import com.example.servicerating.utils.Converter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class RatingDtoTest {


    private Converter converter = new Converter();

    @Test
    public void test_convert_To_Dto(){
        Rating rating = new Rating();
        rating.setRate(12.66);
        rating.setDescription("Fall out boys");

        RatingDTO ratingDTO = converter.convertFromEntity(rating);
        assertThat(ratingDTO).isNotNull();
        assertThat(ratingDTO.getRate()).isEqualTo(rating.getRate());

    }
}
