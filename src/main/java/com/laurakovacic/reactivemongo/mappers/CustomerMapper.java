package com.laurakovacic.reactivemongo.mappers;

import com.laurakovacic.reactivemongo.domain.Customer;
import com.laurakovacic.reactivemongo.model.CustomerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    CustomerDTO customerToCustomerDto(Customer customer);

    Customer customerDtoToCustomer(CustomerDTO customerDTO);
}
