package microservice.base_source.domain.use_case;

import microservice.base_source.domain.entity.Address;

import java.util.List;

public interface AddressUseCase {
    Address create(Address address);
    
    Address read(String addressId);
    
    List<Address> readBuyerAddresses(String buyerId);
    
    Address update(String addressId, Address address);
    
    void delete(String addressId);
}
