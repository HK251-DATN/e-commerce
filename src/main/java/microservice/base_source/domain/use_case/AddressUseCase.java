package microservice.base_source.domain.use_case;

import microservice.base_source.domain.entity.Address;
import microservice.base_source.presentation.response.order.ShipmentFeeResponse;

import java.util.List;

public interface AddressUseCase {
    Address create(Address address);
    
    Address read(Long addressId);
    
    List<Address> readBuyerAddresses(String buyerId);
    
    Address update(Long addressId, Address address);
    
    void delete(Long addressId);
    
    ShipmentFeeResponse calculateShipmentFee(String buyerId);
}
