package microservice.base_source.infrastructure.messaging.productgeneral;

import microservice.base_source.domain.entity.ProductGeneral;

public record ProductGeneralCreatedEvent(
        Long prodGenId,
        String prodName,
        String imgUrl,
        String description,
        Long subSubcategoryId,  // Main field from back-office (ecommerce doesn't use this)
        Long categoryId         // Derived subcategory ID - this is what ecommerce uses
) {
    public ProductGeneral toEntity() {
        ProductGeneral productGeneral = new ProductGeneral();
        productGeneral.setProductGeneralId(prodGenId);
        productGeneral.setName(prodName);
        productGeneral.setDescription(description);
        productGeneral.setImg(imgUrl);
        productGeneral.setCategoryId(categoryId);  // Uses the derived subcategory ID
        return productGeneral;
    }
}