package com.laurakovacic.reactivemongo.web.fn;

import com.laurakovacic.reactivemongo.domain.Beer;
import com.laurakovacic.reactivemongo.model.BeerDTO;
import com.laurakovacic.reactivemongo.services.BeerServiceImplTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.laurakovacic.reactivemongo.web.fn.BeerRouterConfig.BEER_PATH;
import static com.laurakovacic.reactivemongo.web.fn.BeerRouterConfig.BEER_PATH_ID;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class BeerEndpointTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void patchIdNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(BeerServiceImplTest.getTestBeer()), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchIdFound() {
        BeerDTO beerDTO = getSavedTestBeer();

        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(BEER_PATH_ID, beerDTO.getId())
                .body(Mono.just(beerDTO), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(999)
    void deleteBeer() {
        BeerDTO beerDTO = getSavedTestBeer();

        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(BEER_PATH_ID, beerDTO.getId())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @Order(4)
    void updateBeerBadRequest() {
        BeerDTO testBeer = getSavedTestBeer();
        testBeer.setBeerStyle("");

        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, testBeer)
                .body(Mono.just(testBeer), BeerDTO.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateBeerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(BeerServiceImplTest.getTestBeer()), BeerDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(3)
    void updateBeer() {

        BeerDTO beerDTO = getSavedTestBeer();
        beerDTO.setBeerName("New");

        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(BEER_PATH_ID, beerDTO.getId())
                .body(Mono.just(beerDTO), BeerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createBeerBadData() {
        Beer testBeer = BeerServiceImplTest.getTestBeer();
        testBeer.setBeerName("");

        webTestClient
                .mutateWith(mockOAuth2Login())
                .post()
                .uri(BEER_PATH)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createBeer() {
        BeerDTO testDto = getSavedTestBeer();

        webTestClient
                .mutateWith(mockOAuth2Login())
                .post()
                .uri(BEER_PATH)
                .body(Mono.just(testDto), BeerDTO.class)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location");
    }

    @Test
    void getByIdNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(BEER_PATH_ID, 999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(1)
    void getById() {
        BeerDTO beerDTO = getSavedTestBeer();

        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(BEER_PATH_ID, beerDTO.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody(BeerDTO.class);
    }

    @Test
    @Order(2)
    void testListBeersByStyle() {
        final String BEER_STYLE = "TEST";
        BeerDTO testDto = getSavedTestBeer();
        testDto.setBeerStyle(BEER_STYLE);

        //create test data
        webTestClient
                .mutateWith(mockOAuth2Login())
                .post()
                .uri(BeerRouterConfig.BEER_PATH)
                .body(Mono.just(testDto), BeerDTO.class)
                .header("Content-Type", "application/json")
                .exchange();

        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(UriComponentsBuilder
                        .fromPath(BeerRouterConfig.BEER_PATH)
                        .queryParam("beerStyle", BEER_STYLE).build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    @Test
    @Order(2)
    void listBeers() {
        webTestClient
                .mutateWith(mockOAuth2Login()).get()
                .uri(BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-type", "application/json")
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }

    public BeerDTO getSavedTestBeer(){
        FluxExchangeResult<BeerDTO> beerDTOFluxExchangeResult =
                webTestClient
                    .mutateWith(mockOAuth2Login())
                    .post()
                    .uri(BEER_PATH)
                    .body(Mono.just(BeerServiceImplTest.getTestBeer()), BeerDTO.class)
                    .header("Content-Type", "application/json")
                    .exchange()
                    .returnResult(BeerDTO.class);

        List<String> location = beerDTOFluxExchangeResult.getResponseHeaders().get("Location");

        return webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(BEER_PATH)
                .exchange().returnResult(BeerDTO.class).getResponseBody().blockFirst();
    }
}
