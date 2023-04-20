package com.laurakovacic.reactivemongo.mappers;

import com.laurakovacic.reactivemongo.domain.Beer;
import com.laurakovacic.reactivemongo.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {

    BeerDTO beerToBeerDto(Beer beer);

    Beer beerDtoToBeer(BeerDTO beerDTO);
}
