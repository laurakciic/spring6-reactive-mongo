package com.laurakovacic.reactivemongo.web.fn;

import com.laurakovacic.reactivemongo.model.BeerDTO;
import com.laurakovacic.reactivemongo.services.BeerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BeerHandler {

    private final BeerService beerService;

    public Mono<ServerResponse> listBeers(ServerRequest request) {
        return ServerResponse.ok()
                .body(beerService.listBeers(), BeerDTO.class);
                // BeerDTO.class so the framework knows how to parse that into JSON with Jackson
    }
}
