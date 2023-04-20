package com.laurakovacic.reactivemongo.services;

import com.laurakovacic.reactivemongo.domain.Beer;
import com.laurakovacic.reactivemongo.mappers.BeerMapper;
import com.laurakovacic.reactivemongo.model.BeerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;


@SpringBootTest
class BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    BeerDTO beerDTO;

    @BeforeEach
    void setUp() {
        beerDTO = beerMapper.beerToBeerDto(getTestBeer());
    }

    @Test
    void saveBeer() throws InterruptedException {
        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));

        savedMono.subscribe(savedDto -> System.out.println(savedDto.getId()));

        Thread.sleep(1000);
    }

    public static Beer getTestBeer() {
        return Beer.builder()
                .beerName("Vukovarsko")
                .beerStyle("IPA")
                .price(BigDecimal.TEN)
                .quantityOnHand(500)
                .upc("32132")
                .build();
    }
}