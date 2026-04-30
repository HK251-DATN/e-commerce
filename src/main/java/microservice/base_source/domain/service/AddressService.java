package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Address;
import microservice.base_source.domain.entity.Cart;
import microservice.base_source.domain.entity.Coordinate;
import microservice.base_source.domain.use_case.AddressUseCase;
import microservice.base_source.persistence.repository.AddressRepository;
import microservice.base_source.persistence.repository.CartRepository;
import microservice.base_source.presentation.response.order.ShipmentFeeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressService implements AddressUseCase {

    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final GeocodingService geocodingService;
    private final DistanceService distanceService;

    // Warehouse coordinates
    private static final double WAREHOUSE_LAT = 10.8759793;
    private static final double WAREHOUSE_LNG = 106.8086064;
    
    @Override
    @Transactional
    public Address create (Address address) {
        List<Address> userAddrs = readBuyerAddresses(address.getBuyerId());
        boolean isFirstAddress = userAddrs.isEmpty();

        // If this is the first address, automatically set it as default
        if (isFirstAddress) {
            address.setIsDefault(true);
        }

        if (address.getIsDefault()) {
            // Set all other user's address isDefault to false
            userAddrs.forEach(addr -> addr.setIsDefault(false));
            addressRepository.saveAll(userAddrs);
        }

        // Use lat/lng from request directly - no geocoding
        // Validation: both lat and lng must be provided
        if (address.getLat() == null || address.getLng() == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }

        Address savedAddress = addressRepository.save(address);

        // Update cart if this is the first address or if it's set as default
        if (isFirstAddress || address.getIsDefault()) {
            updateCartAddress(address.getBuyerId(), savedAddress.getAddressId());
        }

        return savedAddress;
    }
    
    @Override
    public Address read (Long addressId) {
        return addressRepository.findById(addressId).orElseThrow(
                () -> new RuntimeException("Not found address with id: " + addressId)
        );
    }
    
    @Override
    public List<Address> readBuyerAddresses (String buyerId) {
        return addressRepository.findByBuyerId(buyerId);
    }
    
    @Override
    @Transactional
    public Address update (Long addressId, Address address) {
        Address curAddr = read(addressId);

        if (address.getCommune() != null && !curAddr.getCommune().equals(address.getCommune())) {
            curAddr.setCommune(address.getCommune());
        }

        if (address.getDistrict() != null && !curAddr.getDistrict().equals(address.getDistrict())) {
            curAddr.setDistrict(address.getDistrict());
        }

        if (address.getProvince() != null && !curAddr.getProvince().equals(address.getProvince())) {
            curAddr.setProvince(address.getProvince());
        }

        if (address.getReceiverName() != null && !curAddr.getReceiverName().equals(address.getReceiverName())) {
            curAddr.setReceiverName(address.getReceiverName());
        }

        if (address.getReceiverPNum() != null && !curAddr.getReceiverPNum().equals(address.getReceiverPNum())) {
            curAddr.setReceiverPNum(address.getReceiverPNum());
        }

        if (address.getDetail() != null && !curAddr.getDetail().equals(address.getDetail())) {
            curAddr.setDetail(address.getDetail());
        }

        // Update lat/lng if provided from request
        if (address.getLat() != null) {
            curAddr.setLat(address.getLat());
        }

        if (address.getLng() != null) {
            curAddr.setLng(address.getLng());
        }

        // Track if address is being set to default
        boolean setToDefault = false;

        // Update is_default only if:
        // is_default is not null
        // AND the new is_default is true
        // AND the old is_default is false
        if (address.getIsDefault() != null && address.getIsDefault() && !curAddr.getIsDefault()) {
            // Set all other user's address isDefault to false
            List<Address> userAddrs = readBuyerAddresses(curAddr.getBuyerId());

            for (Address addr : userAddrs) {
                if (addr.getAddressId().equals(addressId))
                    continue;

                addr.setIsDefault(false);
            }

            addressRepository.saveAllAndFlush(userAddrs);

            // Set curAddr isDefault to true
            curAddr.setIsDefault(true);
            setToDefault = true;
        }

        Address savedAddress = addressRepository.save(curAddr);

        // Update cart if address was set to default
        if (setToDefault) {
            updateCartAddress(curAddr.getBuyerId(), addressId);
        }

        return savedAddress;
    }
    
    @Override
    public void delete (Long addressId) {
        Address addr = read(addressId);

        addressRepository.delete(addr);
    }

    @Override
    public ShipmentFeeResponse calculateDefaultShipmentFee(String buyerId) {
        // Get the default address for the buyer
        Address defaultAddress = addressRepository.findByBuyerIdAndIsDefaultTrue(buyerId)
                .orElseThrow(() -> new RuntimeException("No default address found for buyer: " + buyerId));

        // Validate that the address has coordinates
        if (defaultAddress.getLat() == null || defaultAddress.getLng() == null) {
            throw new RuntimeException("Address coordinates not found. Please update the address.");
        }

        // Calculate distance from warehouse to delivery address
        double distanceKm = distanceService.calculateDistanceKm(
                WAREHOUSE_LAT,
                WAREHOUSE_LNG,
                defaultAddress.getLat(),
                defaultAddress.getLng()
        );

        // Calculate shipment fee based on distance
        Long shipmentFee = distanceService.calculateShippingFee(distanceKm);

        // Build full address string
        String fullAddress = String.format("%s, %s, %s, %s",
                defaultAddress.getDetail(),
                defaultAddress.getCommune(),
                defaultAddress.getDistrict(),
                defaultAddress.getProvince()
        );

        // Create and return response
        return new ShipmentFeeResponse(
                defaultAddress.getAddressId(),
                defaultAddress.getReceiverName(),
                fullAddress,
                Math.round(distanceKm * 100.0) / 100.0, // Round to 2 decimal places
                shipmentFee
        );
    }
    
    @Override
    public ShipmentFeeResponse calculateShipmentFee (Long addressId) {
        // Get the default address for the buyer
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("No address : " + addressId));
        
        // Validate that the address has coordinates
        if (address.getLat() == null || address.getLng() == null) {
            throw new RuntimeException("Address coordinates not found. Please update the address.");
        }
        
        // Calculate distance from warehouse to delivery address
        double distanceKm = distanceService.calculateDistanceKm(
                WAREHOUSE_LAT,
                WAREHOUSE_LNG,
                address.getLat(),
                address.getLng()
        );
        
        // Calculate shipment fee based on distance
        Long shipmentFee = distanceService.calculateShippingFee(distanceKm);
        
        // Build full address string
        String fullAddress = String.format("%s, %s, %s, %s",
                address.getDetail(),
                address.getCommune(),
                address.getDistrict(),
                address.getProvince()
        );
        
        // Create and return response
        return new ShipmentFeeResponse(
                address.getAddressId(),
                address.getReceiverName(),
                fullAddress,
                Math.round(distanceKm * 100.0) / 100.0, // Round to 2 decimal places
                shipmentFee
        );
    }

    /**
     * Helper method to update cart's address and shipping fee
     * @param buyerId The buyer ID
     * @param addressId The new address ID
     */
    private void updateCartAddress(String buyerId, Long addressId) {
        Optional<Cart> cartOptional = cartRepository.findByBuyerId(buyerId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();

            // Calculate shipping fee for the new address
            ShipmentFeeResponse shipmentFee = calculateShipmentFee(addressId);

            // Update cart with new address and shipping fee
            cart.setAddressId(addressId);
            cart.setShippingFee(shipmentFee.getShipmentFee());

            cartRepository.save(cart);
        }
        // If cart doesn't exist yet, it will be set when cart is created
    }
}
