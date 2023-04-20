package com.laurakovacic.reactivemongo.services;

import com.laurakovacic.reactivemongo.domain.Beer;
import com.laurakovacic.reactivemongo.mappers.BeerMapper;
import com.laurakovacic.reactivemongo.mappers.BeerMapperImpl;
import com.laurakovacic.reactivemongo.model.BeerDTO;
import com.laurakovacic.reactivemongo.repositories.BeerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@SpringBootTest
class BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    BeerRepository beerRepository;

    BeerDTO beerDTO;

    @BeforeEach
    void setUp() {
        beerDTO = beerMapper.beerToBeerDto(getTestBeer());
    }

    @Test
    @DisplayName("Test Save Beer Using Subscriber")
    void saveBeer() {

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
            atomicBoolean.set(true);
            atomicDto.set(savedDto);
        });

        await().untilTrue(atomicBoolean);

        BeerDTO persistedDto = atomicDto.get();
        assertThat(persistedDto).isNotNull();
        assertThat(persistedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Save Beer Using Block")
    void saveBeerUseBlock() {
        BeerDTO savedDto = beerService.saveBeer(Mono.just(getTestBeerDto())).block();
        assertThat(savedDto).isNotNull();
        assertThat(savedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Update Beer Using Block")
    void updateUseBlock() {
        final String newName = "New Beer Name";
        BeerDTO savedBeerDto = getSavedDto();
        savedBeerDto.setBeerName(newName);

        BeerDTO updatedDto = beerService.saveBeer(Mono.just(savedBeerDto)).block();

        // verify it exists in DB
        BeerDTO fetchedDto = beerService.getById(updatedDto.getId()).block();
        assertThat(fetchedDto.getBeerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test Update Using Reactive Streams")
    void updateBeerUseStreams() {
        final String newName = "New Beer Name";

        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        beerService.saveBeer(Mono.just(getTestBeerDto()))
                .map(savedBeerDto -> {
                    savedBeerDto.setBeerName(newName);
                    return savedBeerDto;
                })
                .flatMap(beerService::saveBeer)
                .flatMap(savedUpdatedDto -> beerService.getById(savedUpdatedDto.getId())) // get from db
                .subscribe(dtoFromDb -> atomicDto.set(dtoFromDb));

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getBeerName()).isEqualTo(newName);
    }

    @Test
    void deleteBeer() {
        BeerDTO beerToDelete = getSavedDto();

        beerService.deleteBeerById(beerToDelete.getId()).block();

        Mono<BeerDTO> expectedEmptyBeerMono = beerService.getById(beerToDelete.getId());

        BeerDTO emptyBeer = expectedEmptyBeerMono.block();

        assertThat(emptyBeer).isNull();
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

    public static BeerDTO getTestBeerDto() {
        return new BeerMapperImpl().beerToBeerDto(getTestBeer());
    }

    public BeerDTO getSavedDto() {
        return beerService.saveBeer(Mono.just(getTestBeerDto())).block();
    }
}