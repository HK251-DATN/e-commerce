package microservice.base_source.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.base_source.domain.entity.*;
import microservice.base_source.domain.entity.Coupon.DiscountType;
import microservice.base_source.persistence.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final BuyerRepository buyerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductGeneralRepository productGeneralRepository;
    private final BatchDetailRepository batchDetailRepository;
    private final ProductDetailRepository productDetailRepository;
    private final SaleEventRepository saleEventRepository;
    private final SaleProductRepository saleProductRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FeedBackRepository feedBackRepository;
    private final CouponRepository couponRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Only seed if database is empty
            if (buyerRepository.count() > 0) {
                log.info("Database already contains data. Skipping seeding.");
                return;
            }

            log.info("Starting database seeding...");

            // ==================== Seed Buyers (from identity-service) ====================
            
            
            Buyer buyer = createBuyer("2", "Fbuyer", "Lbuyer", "buyer@gmail.com");
            Buyer buyer1 = createBuyer("3", "Minh", "Trần", "minh.tran@gmail.com");
            Buyer buyer2 = createBuyer("4", "Hương", "Lê", "huong.le@gmail.com");
            Buyer buyer3 = createBuyer("5", "Tuấn", "Phạm", "tuan.pham@gmail.com");
            Buyer buyer4 = createBuyer("6", "Linh", "Võ", "linh.vo@gmail.com");

            log.info("Seeded {} buyers", buyerRepository.count());

            // ==================== Seed Addresses ====================
            createAddress(buyer.getBuyerId(), "Fbuyer Lbuyer", "0901234567",
                    "TP.HCM", "Quận 7", "Phường Tân Phú", "123 Nguyễn Văn Linh", true);
            createAddress(buyer1.getBuyerId(), "Trần Minh", "0901234567",
                "TP.HCM", "Quận 7", "Phường Tân Phú", "123 Nguyễn Văn Linh", true);
            createAddress(buyer2.getBuyerId(), "Lê Hương", "0902345678",
                "TP.HCM", "Thủ Đức", "Phường Linh Trung", "456 Võ Văn Ngân", true);
            createAddress(buyer3.getBuyerId(), "Phạm Tuấn", "0903456789",
                "TP.HCM", "Gò Vấp", "Phường 12", "789 Quang Trung", true);
            createAddress(buyer4.getBuyerId(), "Võ Linh", "0904567890",
                "TP.HCM", "Quận 1", "Phường Bến Nghé", "321 Nguyễn Thị Minh Khai", true);
            createAddress(buyer1.getBuyerId(), "Trần Minh", "0901234567",
                "TP.HCM", "Quận 5", "Phường 1", "654 Trần Hưng Đạo", false);

            log.info("Seeded {} addresses", addressRepository.count());

            // ==================== Seed Categories (synced from back-office) ====================
            Category thitHaiSan = createCategory(1L,"Thịt & Hải Sản", 1, "Thịt tươi, gia cầm và hải sản", "https://pub-b68fb0743b644089825dfe7eadaf6d7e.r2.dev/meat.png", "N", null);
            Category giaCam = createCategory(2L, "Gia Cầm", 1, "Các loại thịt gia cầm tươi", null, "Y", thitHaiSan.getCategoryId());
            Category thitDo = createCategory(3L, "Thịt Đỏ", 2, "Các loại thịt đỏ tươi", null, "Y", thitHaiSan.getCategoryId());
            Category haiSan = createCategory(4L, "Hải Sản", 3, "Hải sản tươi sống", null, "Y", thitHaiSan.getCategoryId());
            Category rauCuQua = createCategory(5L, "Rau Củ Quả", 2, "Rau xanh, củ quả và trái cây tươi", "https://pub-b68fb0743b644089825dfe7eadaf6d7e.r2.dev/vegetable.png", "N", null);
            Category rauAnLa = createCategory(6L, "Rau Ăn Lá", 1, "Các loại rau xanh ăn lá", null, "Y", rauCuQua.getCategoryId());
            Category cuQua = createCategory(7L, "Củ Quả", 2, "Các loại củ và quả tươi", null, "Y", rauCuQua.getCategoryId());
            Category traiCay = createCategory(8L, "Trái Cây", 3, "Trái cây tươi trong nước và nhập khẩu", null, "Y", rauCuQua.getCategoryId());
            Category rauGiaVi = createCategory(9L, "Rau Gia Vị", 4, "Các loại rau và củ gia vị", null, "Y", rauCuQua.getCategoryId());
            Category suaTrung = createCategory(10L, "Sữa & Trứng", 3, "Sữa tươi, sản phẩm từ sữa và trứng", "https://pub-b68fb0743b644089825dfe7eadaf6d7e.r2.dev/dairy-products.png", "N", null);
            Category suaTuoi = createCategory(11L, "Sữa Tươi", 1, "Các loại sữa tươi nguyên chất", null, "Y", suaTrung.getCategoryId());
            Category spTuSua = createCategory(12L, "Sản Phẩm Từ Sữa", 2, "Bơ, phô mai, kem và sữa chua", null, "Y", suaTrung.getCategoryId());
            Category trung = createCategory(13L, "Trứng", 3, "Các loại trứng tươi", null, "Y", suaTrung.getCategoryId());
            Category doUongTuSua = createCategory(14L, "Đồ Uống Từ Sữa", 4, "Sữa đậu nành, sữa hạt và các loại sữa uống", null, "Y", suaTrung.getCategoryId());

            log.info("Seeded {} categories", categoryRepository.count());

            // ==================== Seed Product Generals (synced from back-office) ====================
            // Match IDs from back-office service
            
            // Thịt Gà (subSubcategoryId: 1)
            createProductGeneral(1L, "Gà Ta Nguyên Con", "Gà ta thả vườn tươi ngon, thịt chắc thơm ngon", new String[]{"gà ta", "gà nguyên con", "thịt gà"}, ProductGeneral.Unit.KILOGRAM, 1L, 2L);
            createProductGeneral(2L, "Ức Gà Phi Lê", "Ức gà phi lê không xương, thịt trắng mềm", new String[]{"ức gà", "phi lê", "thịt gà"}, ProductGeneral.Unit.GRAM, 500L, 2L);
            createProductGeneral(3L, "Đùi Gà Tươi", "Đùi gà tươi có xương, thịt ngọt đậm đà", new String[]{"đùi gà", "thịt gà", "gà tươi"}, ProductGeneral.Unit.GRAM, 500L, 2L);
            
            // Thịt Vịt (subSubcategoryId: 2)
            createProductGeneral(4L, "Vịt Trời Nguyên Con", "Vịt trời tươi nguyên con, thịt chắc thơm", new String[]{"vịt trời", "vịt nguyên con", "thịt vịt"}, ProductGeneral.Unit.KILOGRAM, 1L, 2L);
            createProductGeneral(5L, "Ức Vịt Phi Lê", "Ức vịt phi lê không da, ít mỡ", new String[]{"ức vịt", "phi lê", "thịt vịt"}, ProductGeneral.Unit.GRAM, 500L, 2L);
            createProductGeneral(6L, "Đùi Vịt Bó Xôi", "Đùi vịt bó xôi tươi ngon, thịt đậm đà", new String[]{"đùi vịt", "vịt bó xôi", "thịt vịt"}, ProductGeneral.Unit.GRAM, 500L, 2L);
            
            // Thịt Ngan (subSubcategoryId: 3)
            createProductGeneral(7L, "Ngan Nguyên Con", "Ngan tươi nguyên con, thịt thơm béo", new String[]{"ngan", "ngan nguyên con", "thịt ngan"}, ProductGeneral.Unit.KILOGRAM, 1L, 2L);
            createProductGeneral(8L, "Ức Ngan Phi Lê", "Ức ngan phi lê cao cấp, thịt đỏ thơm", new String[]{"ức ngan", "phi lê", "thịt ngan"}, ProductGeneral.Unit.GRAM, 500L, 2L);
            createProductGeneral(9L, "Đùi Ngan Tươi", "Đùi ngan tươi ngon, thịt chắc", new String[]{"đùi ngan", "thịt ngan", "ngan tươi"}, ProductGeneral.Unit.GRAM, 500L, 2L);
            
            // Thịt Chim Cút (subSubcategoryId: 4)
            createProductGeneral(10L, "Chim Cút Nguyên Con", "Chim cút tươi nguyên con, thịt thơm ngọt", new String[]{"chim cút", "cút nguyên con", "thịt cút"}, ProductGeneral.Unit.GRAM, 200L, 2L);
            createProductGeneral(11L, "Chim Cút Rút Xương", "Chim cút rút xương sẵn, tiện chế biến", new String[]{"chim cút", "rút xương", "thịt cút"}, ProductGeneral.Unit.GRAM, 200L, 2L);
            createProductGeneral(12L, "Chim Cút Loại 1", "Chim cút loại 1 to đều, tươi ngon", new String[]{"chim cút", "cút loại 1", "thịt cút"}, ProductGeneral.Unit.GRAM, 300L, 2L);
            
            // Lòng Gia Cầm (subSubcategoryId: 5)
            createProductGeneral(13L, "Gan Gà Tươi", "Gan gà tươi ngon, giàu dinh dưỡng", new String[]{"gan gà", "lòng gà", "nội t장"}, ProductGeneral.Unit.GRAM, 300L, 2L);
            createProductGeneral(14L, "Mề Gà Tươi", "Mề gà tươi sạch, giòn ngọt", new String[]{"mề gà", "lòng gà", "dạ dày gà"}, ProductGeneral.Unit.GRAM, 300L, 2L);
            createProductGeneral(15L, "Tim Gà Tươi", "Tim gà tươi ngon, dai giòn", new String[]{"tim gà", "lòng gà", "nội tạng"}, ProductGeneral.Unit.GRAM, 200L, 2L);
            
            // Thịt Bò (subSubcategoryId: 6)
            createProductGeneral(16L, "Thịt Bò Úc Phi Lê", "Thịt bò Úc nhập khẩu phi lê mềm", new String[]{"thịt bò", "bò Úc", "phi lê"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(17L, "Thịt Bò Nạm", "Thịt bò nạm tươi, thích hợp nấu phở", new String[]{"thịt bò", "bò nạm", "bò tươi"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(18L, "Thịt Bò Vai", "Thịt bò vai tươi ngon, thơm mềm", new String[]{"thịt bò", "bò vai", "bò tươi"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            
            // Thịt Heo (subSubcategoryId: 7)
            createProductGeneral(19L, "Thịt Heo Ba Chỉ", "Thịt heo ba chỉ tươi, vừa nạc vừa mỡ", new String[]{"thịt heo", "ba chỉ", "thịt lợn"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(20L, "Thịt Heo Nạc Vai", "Thịt heo nạc vai tươi, ít mỡ", new String[]{"thịt heo", "nạc vai", "thịt lợn"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(21L, "Thịt Heo Nạc Dăm", "Thịt heo nạc dăm tươi ngon", new String[]{"thịt heo", "nạc dăm", "thịt lợn"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            
            // Thịt Dê (subSubcategoryId: 8)
            createProductGeneral(22L, "Thịt Dê Nạc", "Thịt dê nạc tươi, thơm ngon bổ dưỡng", new String[]{"thịt dê", "dê nạc", "dê tươi"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(23L, "Thịt Dê Có Xương", "Thịt dê có xương nấu cháo, hầm", new String[]{"thịt dê", "dê xương", "dê tươi"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(24L, "Sườn Dê Tươi", "Sườn dê tươi ngon, nướng hoặc hầm", new String[]{"sườn dê", "thịt dê", "dê tươi"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            
            // Thịt Cừu (subSubcategoryId: 9)
            createProductGeneral(25L, "Thịt Cừu Úc Phi Lê", "Thịt cừu Úc nhập khẩu phi lê cao cấp", new String[]{"thịt cừu", "cừu Úc", "phi lê"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(26L, "Thịt Cừu New Zealand", "Thịt cừu New Zealand tươi ngon", new String[]{"thịt cừu", "cừu NZ", "cừu nhập khẩu"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(27L, "Sườn Cừu Cao Cấp", "Sườn cừu cao cấp nướng BBQ", new String[]{"sườn cừu", "thịt cừu", "cừu nướng"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            
            // Xúc Xích Tươi (subSubcategoryId: 10)
            createProductGeneral(28L, "Xúc Xích Heo Tươi", "Xúc xích heo tươi chưa nướng", new String[]{"xúc xích", "xúc xích heo", "lạp xưởng"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(29L, "Xúc Xích Bò Tươi", "Xúc xích bò tươi nguyên chất", new String[]{"xúc xích", "xúc xích bò", "lạp xưởng"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            createProductGeneral(30L, "Xúc Xích Gà Tươi", "Xúc xích gà tươi ít béo, healthy", new String[]{"xúc xích", "xúc xích gà", "lạp xưởng"}, ProductGeneral.Unit.GRAM, 500L, 3L);
            
            // Tôm Tươi (subSubcategoryId: 11)
            createProductGeneral(31L, "Tôm Sú Tươi Sống", "Tôm sú tươi sống size lớn", new String[]{"tôm sú", "tôm tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            createProductGeneral(32L, "Tôm Thẻ Tươi", "Tôm thẻ tươi ngọt thịt chắc", new String[]{"tôm thẻ", "tôm tươi", "hải sản"}, ProductGeneral.Unit.GRAM, 500L, 4L);
            createProductGeneral(33L, "Tôm Càng Xanh", "Tôm càng xanh tươi sống to", new String[]{"tôm càng", "tôm tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            
            // Cá Tươi (subSubcategoryId: 12)
            createProductGeneral(34L, "Cá Hồi Phi Lê", "Cá hồi phi lê tươi Nauy", new String[]{"cá hồi", "phi lê", "cá tươi"}, ProductGeneral.Unit.GRAM, 500L, 4L);
            createProductGeneral(35L, "Cá Basa Phi Lê", "Cá basa phi lê không xương", new String[]{"cá basa", "phi lê", "cá tươi"}, ProductGeneral.Unit.GRAM, 500L, 4L);
            createProductGeneral(36L, "Cá Diêu Hồng Tươi", "Cá diêu hồng tươi nguyên con", new String[]{"cá diêu hồng", "cá nguyên con", "cá tươi"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            
            // Mực Tươi (subSubcategoryId: 13)
            createProductGeneral(37L, "Mực Ống Tươi", "Mực ống tươi size lớn", new String[]{"mực ống", "mực tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            createProductGeneral(38L, "Mực Nang Tươi", "Mực nang tươi ngọt thịt dai", new String[]{"mực nang", "mực tươi", "hải sản"}, ProductGeneral.Unit.GRAM, 500L, 4L);
            createProductGeneral(39L, "Mực Lá Tươi", "Mực lá tươi sống nhỏ", new String[]{"mực lá", "mực tươi", "hải sản"}, ProductGeneral.Unit.GRAM, 500L, 4L);
            
            // Cua Ghẹ (subSubcategoryId: 14)
            createProductGeneral(40L, "Cua Biển Tươi Sống", "Cua biển tươi sống size to", new String[]{"cua biển", "cua tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            createProductGeneral(41L, "Ghẹ Xanh Tươi", "Ghẹ xanh tươi sống thịt ngọt", new String[]{"ghẹ", "cua tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            createProductGeneral(42L, "Cua Gạch Tươi", "Cua gạch tươi đầy gạch béo ngậy", new String[]{"cua gạch", "cua tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            
            // Nghêu Sò (subSubcategoryId: 15)
            createProductGeneral(43L, "Nghêu Tươi Sống", "Nghêu tươi sống vỏ to thịt ngọt", new String[]{"nghêu", "sò tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            createProductGeneral(44L, "Sò Huyết Tươi", "Sò huyết tươi sống ngon ngọt", new String[]{"sò huyết", "sò tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            createProductGeneral(45L, "Hàu Sữa Tươi", "Hàu sữa tươi sống béo ngậy", new String[]{"hàu", "sò tươi", "hải sản"}, ProductGeneral.Unit.KILOGRAM, 1L, 4L);
            
            // Rau Muống (subSubcategoryId: 16)
            createProductGeneral(46L, "Rau Muống Xanh", "Rau muống xanh tươi non mơn mởn", new String[]{"rau muống", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(47L, "Rau Muống Dại", "Rau muống dại tươi thơm ngon", new String[]{"rau muống", "rau dại", "rau xanh"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(48L, "Rau Muống Cọng To", "Rau muống cọng to tươi giòn", new String[]{"rau muống", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            
            // Cải Xanh (subSubcategoryId: 17)
            createProductGeneral(49L, "Cải Xanh Tươi", "Cải xanh tươi ngọt mát", new String[]{"cải xanh", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(50L, "Cải Ngọt Baby", "Cải ngọt baby non mềm", new String[]{"cải ngọt", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 300L, 6L);
            createProductGeneral(51L, "Cải Ngồng Tươi", "Cải ngồng tươi giòn ngọt", new String[]{"cải ngồng", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            
            // Xà Lách (subSubcategoryId: 18)
            createProductGeneral(52L, "Xà Lách Xoong", "Xà lách xoong tươi giòn", new String[]{"xà lách", "rau xanh", "rau salad"}, ProductGeneral.Unit.GRAM, 300L, 6L);
            createProductGeneral(53L, "Xà Lách Lô Lô", "Xà lách lô lô đỏ tím tươi", new String[]{"xà lách", "rau xanh", "rau salad"}, ProductGeneral.Unit.GRAM, 300L, 6L);
            createProductGeneral(54L, "Xà Lách Tím", "Xà lách tím tươi giàu anthocyanin", new String[]{"xà lách", "rau xanh", "rau salad"}, ProductGeneral.Unit.GRAM, 300L, 6L);
            
            // Rau Dền (subSubcategoryId: 19)
            createProductGeneral(55L, "Rau Dền Đỏ", "Rau dền đỏ tươi giàu sắt", new String[]{"rau dền", "rau đỏ", "rau xanh"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(56L, "Rau Dền Xanh", "Rau dền xanh tươi mát", new String[]{"rau dền", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(57L, "Rau Dền Cơm", "Rau dền cơm non mềm thơm", new String[]{"rau dền", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            
            // Cải Thìa (subSubcategoryId: 20)
            createProductGeneral(58L, "Cải Thìa Tươi", "Cải thìa tươi ngọt mát", new String[]{"cải thìa", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(59L, "Cải Bẹ Trắng", "Cải bẹ trắng tươi giòn ngọt", new String[]{"cải bẹ", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 500L, 6L);
            createProductGeneral(60L, "Cải Thìa Baby", "Cải thìa baby non mềm", new String[]{"cải thìa", "rau xanh", "rau ăn lá"}, ProductGeneral.Unit.GRAM, 300L, 6L);
            
            // Cà Rốt (subSubcategoryId: 21)
            createProductGeneral(61L, "Cà Rốt Đà Lạt", "Cà rốt Đà Lạt tươi giòn ngọt", new String[]{"cà rốt", "củ quả", "rau củ"}, ProductGeneral.Unit.GRAM, 500L, 7L);
            createProductGeneral(62L, "Cà Rốt Nhật", "Cà rốt Nhật to đều màu đẹp", new String[]{"cà rốt", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(63L, "Cà Rốt Baby", "Cà rốt baby nhỏ xinh ăn salad", new String[]{"cà rốt", "củ quả", "rau củ"}, ProductGeneral.Unit.GRAM, 300L, 7L);
            
            // Khoai Tây (subSubcategoryId: 22)
            createProductGeneral(64L, "Khoai Tây Đà Lạt", "Khoai tây Đà Lạt tươi ngon", new String[]{"khoai tây", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(65L, "Khoai Tây Ai Cập", "Khoai tây Ai Cập nhập khẩu", new String[]{"khoai tây", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(66L, "Khoai Tây Tím", "Khoai tây tím giàu chất chống oxy hóa", new String[]{"khoai tây", "củ quả", "rau củ"}, ProductGeneral.Unit.GRAM, 500L, 7L);
            
            // Củ Cải (subSubcategoryId: 23)
            createProductGeneral(67L, "Củ Cải Trắng", "Củ cải trắng tươi giòn ngọt", new String[]{"củ cải", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(68L, "Củ Cải Đỏ", "Củ cải đỏ tươi giàu vitamin", new String[]{"củ cải", "củ quả", "rau củ"}, ProductGeneral.Unit.GRAM, 500L, 7L);
            createProductGeneral(69L, "Củ Cải Muối", "Củ cải trắng dùng làm dưa muối", new String[]{"củ cải", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            
            // Bắp (subSubcategoryId: 24)
            createProductGeneral(70L, "Bắp Ngọt Tươi", "Bắp ngọt tươi hạt vàng căng mọng", new String[]{"bắp ngô", "ngô", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(71L, "Bắp Nếp Tươi", "Bắp nếp tươi dẻo thơm", new String[]{"bắp nếp", "ngô", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(72L, "Bắp Mỹ Tươi", "Bắp Mỹ tươi hạt to ngọt", new String[]{"bắp ngô", "ngô Mỹ", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            
            // Su Su (subSubcategoryId: 25)
            createProductGeneral(73L, "Su Su Xanh", "Su su xanh tươi giòn ngọt", new String[]{"su su", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(74L, "Su Su Trắng", "Su su trắng tươi mềm ngọt", new String[]{"su su", "củ quả", "rau củ"}, ProductGeneral.Unit.KILOGRAM, 1L, 7L);
            createProductGeneral(75L, "Su Su Non", "Su su non tươi giòn ăn salad", new String[]{"su su", "củ quả", "rau củ"}, ProductGeneral.Unit.GRAM, 500L, 7L);
            
            // Xoài (subSubcategoryId: 26)
            createProductGeneral(76L, "Xoài Cát Hòa Lộc", "Xoài cát Hòa Lộc ngọt thơm", new String[]{"xoài", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(77L, "Xoài Úc", "Xoài Úc nhập khẩu to ngọt", new String[]{"xoài", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(78L, "Xoài Tượng", "Xoài tượng xanh giòn chua ngọt", new String[]{"xoài", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            
            // Chuối (subSubcategoryId: 27)
            createProductGeneral(79L, "Chuối Già", "Chuối già ngọt thơm bổ dưỡng", new String[]{"chuối", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(80L, "Chuối Tiêu Hương", "Chuối tiêu hương thơm ngọt", new String[]{"chuối", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(81L, "Chuối Sứ", "Chuối sứ nhỏ ngọt đậm", new String[]{"chuối", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            
            // Dưa Hấu (subSubcategoryId: 28)
            createProductGeneral(82L, "Dưa Hấu Không Hạt", "Dưa hấu không hạt ngọt mát", new String[]{"dưa hấu", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(83L, "Dưa Hấu Ruột Đỏ", "Dưa hấu ruột đỏ ngọt tươi", new String[]{"dưa hấu", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(84L, "Dưa Hấu Vàng", "Dưa hấu vàng giòn ngọt thanh", new String[]{"dưa hấu", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            
            // Ổi (subSubcategoryId: 29)
            createProductGeneral(85L, "Ổi Nữ Hoàng", "Ổi nữ hoàng giòn ngọt thơm", new String[]{"ổi", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(86L, "Ổi Ruột Đỏ", "Ổi ruột đỏ ngọt giàu lycopene", new String[]{"ổi", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(87L, "Ổi Xanh", "Ổi xanh giòn chua nhẹ", new String[]{"ổi", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            
            // Thanh Long (subSubcategoryId: 30)
            createProductGeneral(88L, "Thanh Long Ruột Đỏ", "Thanh long ruột đỏ ngọt thơm", new String[]{"thanh long", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(89L, "Thanh Long Ruột Trắng", "Thanh long ruột trắng ngọt mát", new String[]{"thanh long", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            createProductGeneral(90L, "Thanh Long Vàng", "Thanh long vàng Ecuador cao cấp", new String[]{"thanh long", "trái cây", "hoa quả"}, ProductGeneral.Unit.KILOGRAM, 1L, 8L);
            
            // Hành Lá (subSubcategoryId: 31)
            createProductGeneral(91L, "Hành Lá Tươi", "Hành lá tươi thơm dùng nêm nếm", new String[]{"hành lá", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 200L, 9L);
            createProductGeneral(92L, "Hành Tây Tươi", "Hành tây tươi cay thơm", new String[]{"hành tây", "gia vị", "rau thơm"}, ProductGeneral.Unit.KILOGRAM, 1L, 9L);
            createProductGeneral(93L, "Hành Tím Tươi", "Hành tím tươi cay nồng", new String[]{"hành tím", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 500L, 9L);
            
            // Tỏi (subSubcategoryId: 32)
            createProductGeneral(94L, "Tỏi Lý Sơn", "Tỏi Lý Sơn đặc sản thơm cay", new String[]{"tỏi", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 500L, 9L);
            createProductGeneral(95L, "Tỏi Tươi Cà Mau", "Tỏi tươi Cà Mau múi to", new String[]{"tỏi", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 500L, 9L);
            createProductGeneral(96L, "Tỏi Tây", "Tỏi tây nhập khẩu múi lớn", new String[]{"tỏi", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 500L, 9L);
            
            // Gừng (subSubcategoryId: 33)
            createProductGeneral(97L, "Gừng Già", "Gừng già cay nồng dùng nấu ăn", new String[]{"gừng", "gia vị", "rau củ"}, ProductGeneral.Unit.GRAM, 500L, 9L);
            createProductGeneral(98L, "Gừng Non", "Gừng non ít cay giòn ngọt", new String[]{"gừng", "gia vị", "rau củ"}, ProductGeneral.Unit.GRAM, 300L, 9L);
            createProductGeneral(99L, "Gừng Khô", "Gừng khô cay thơm lâu", new String[]{"gừng", "gia vị", "rau củ"}, ProductGeneral.Unit.GRAM, 200L, 9L);
            
            // Ớt (subSubcategoryId: 34)
            createProductGeneral(100L, "Ớt Sừng Xanh", "Ớt sừng xanh tươi cay nhẹ", new String[]{"ớt", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 200L, 9L);
            createProductGeneral(101L, "Ớt Hiểm Đỏ", "Ớt hiểm đỏ tươi cay nồng", new String[]{"ớt", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 200L, 9L);
            createProductGeneral(102L, "Ớt Chuông", "Ớt chuông tươi ngọt màu sắc", new String[]{"ớt", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 500L, 9L);
            
            // Sả (subSubcategoryId: 35)
            createProductGeneral(103L, "Sả Tươi Nguyên Cây", "Sả tươi nguyên cây thơm nồng", new String[]{"sả", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 300L, 9L);
            createProductGeneral(104L, "Sả Tía Tươi", "Sả tía tươi thơm đặc biệt", new String[]{"sả", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 300L, 9L);
            createProductGeneral(105L, "Sả Tôm Tươi", "Sả tôm tươi thơm nhẹ", new String[]{"sả", "gia vị", "rau thơm"}, ProductGeneral.Unit.GRAM, 300L, 9L);

            log.info("Seeded {} product generals", productGeneralRepository.count());

            // ==================== Seed Carts ====================
            Cart cart = createCart(buyer.getBuyerId(), 100L);
            Cart cart1 = createCart(buyer1.getBuyerId(), 100L);
            Cart cart2 = createCart(buyer2.getBuyerId(), 100L);
            Cart cart3 = createCart(buyer3.getBuyerId(), 100L);
            Cart cart4 = createCart(buyer4.getBuyerId(), 100L);
            
            log.info("Seeded {} carts", cartRepository.count());
            
            // ==================== Seed Coupon ====================
            // 1. Coupon giảm 10% cho đơn hàng từ 100k, tối đa giảm 50k
            createCoupon(
                null, 
                "GIAM10% MAX 500K", 
                100L, 100L, 
                DiscountType.PERCENTAGE, 10L, 500000L, 100000L, 
                LocalDateTime.now().plusMonths(1)
            );

            createCoupon(
                null, 
                "GIAM10% MAX 50K", 
                100L, 100L, 
                DiscountType.PERCENTAGE, 10L, 50000L, 100000L, 
                LocalDateTime.now().plusMonths(1)
            );

            // 2. Coupon giảm thẳng 100k cho đơn hàng từ 1 triệu, không giới hạn mức giảm tối đa
            createCoupon(
                null, 
                "VIP100K", 
                50L, 50L, 
                DiscountType.FIXED_AMOUNT, 100000L, 100000L, 1000000L, 
                LocalDateTime.now().plusWeeks(2)
            );

            // 3. Coupon "Phá giá" - Giảm 50% cho đơn hàng nhỏ (từ 50k), tối đa giảm 20k
            createCoupon(
                null, 
                "BANMOI", 
                500L, 500L, 
                DiscountType.PERCENTAGE, 50L, 20000L, 50000L, 
                LocalDateTime.now().plusDays(7)
            );

            // 4. Coupon ngày lễ - Giảm 15% cho đơn từ 500k, tối đa 100k
            createCoupon(
                null, 
                "LE3004", 
                200L, 200L, 
                DiscountType.PERCENTAGE, 15L, 100000L, 500000L, 
                LocalDateTime.now().plusMonths(2)
            );

            // 5. Coupon Flash Sale - Giảm 200k cho đơn từ 2 triệu (số lượng cực ít)
            createCoupon(
                null, 
                "FLASHSALE", 
                10L, 10L, 
                DiscountType.FIXED_AMOUNT, 200000L, 200000L, 2000000L, 
                LocalDateTime.now().plusHours(5)
            );

            // Tạo BatchDetail cho sản phẩm đầu tiên
//            BatchDetail batch1 = createBatchDetail(
//                "BATCH-SKU-001",      // batchDetailId (Khớp với CartItem)
//                101L,                 // productGeneralId (ID sản phẩm tổng quát)
//                50,                   // quantity (Số lượng tồn kho trong lô)
//                new BigDecimal("150000"), // price (Giá bán: 150.000 VNĐ)
//                "Màu xanh, Size L, Chất liệu Cotton" // detailContent
//            );
//
//            // ==================== Seed Cart Items ====================
//            // 1. Sản phẩm A - Số lượng 2, được chọn để thanh toán
//            createCartItem(cart.getCartId(), batch1.getBatchDetailId(), 2L, true);
//            // 2. Sản phẩm B - Số lượng 1, được chọn để thanh toán
//            createCartItem(cart.getCartId(), batch1.getBatchDetailId(), 1L, true);
//            // 3. Sản phẩm C - Số lượng 5, chưa chọn thanh toán (để trong giỏ hàng chờ)
//            createCartItem(cart.getCartId(), batch1.getBatchDetailId(), 5L, false);
//            // 4. Sản phẩm D - Số lượng 10, được chọn để thanh toán
//            createCartItem(cart.getCartId(), batch1.getBatchDetailId(), 10L, true);
//            // 5. Sản phẩm E - Số lượng 1, chưa chọn thanh toán
//            createCartItem(cart.getCartId(), batch1.getBatchDetailId(), 1L, false);
//
//            createCartItem(cart2.getCartId(), batch8.getBatchDetailId(), 1L, true);
//            createCartItem(cart2.getCartId(), batch5.getBatchDetailId(), 2L, false);
//
//            createCartItem(cart3.getCartId(), batch11.getBatchDetailId(), 1L, true);
//            createCartItem(cart3.getCartId(), batch12.getBatchDetailId(), 2L, true);
//
//            createCartItem(cart4.getCartId(), batch4.getBatchDetailId(), 3L, true);
//            createCartItem(cart4.getCartId(), batch7.getBatchDetailId(), 2L, true);
//            createCartItem(cart4.getCartId(), batch14.getBatchDetailId(), 10L, false);
//
//            log.info("Seeded {} cart items", cartItemRepository.count());
//
//            // ==================== Seed FeedBack (Product Reviews) ====================
//            // Note: productDetailId are auto-generated starting from 1L
//            createFeedBack(buyer1.getBuyerId(), 1L, new BigDecimal("5.00"), "Xoài rất ngon và ngọt! Chất lượng tuyệt vời, sẽ mua lại.");
//            createFeedBack(buyer2.getBuyerId(), 1L, new BigDecimal("4.00"), "Xoài ngon nhưng hơi nhỏ so với mô tả.");
//            createFeedBack(buyer1.getBuyerId(), 3L, new BigDecimal("5.00"), "Dâu tây tươi ngon, không bị dập, đóng gói cẩn thận.");
//            createFeedBack(buyer3.getBuyerId(), 8L, new BigDecimal("5.00"), "Thịt gà tươi và sạch, rất hài lòng.");
//            createFeedBack(buyer2.getBuyerId(), 11L, new BigDecimal("5.00"), "Cá hồi chất lượng cao, tươi ngon, đáng giá tiền.");
//            createFeedBack(buyer4.getBuyerId(), 4L, new BigDecimal("4.00"), "Rau bina tươi nhưng hơi ít so với mong đợi.");
//            createFeedBack(buyer3.getBuyerId(), 6L, new BigDecimal("5.00"), "Cà chua bi ngọt và tươi, con nhà mình rất thích.");
//            createFeedBack(buyer1.getBuyerId(), 13L, new BigDecimal("5.00"), "Sữa tươi Vinamilk luôn đảm bảo chất lượng.");
//            createFeedBack(buyer4.getBuyerId(), 2L, new BigDecimal("4.00"), "Cam ngon và mọng nước nhưng giao hơi lâu.");
//            createFeedBack(buyer2.getBuyerId(), 9L, new BigDecimal("5.00"), "Thịt bò Úc chất lượng xuất sắc, nướng rất ngon.");

            log.info("Seeded {} feedbacks", feedBackRepository.count());

            log.info("Database seeding completed successfully!");
            log.info("Summary:");
            log.info("  - {} buyers registered", buyerRepository.count());
            log.info("  - {} addresses saved", addressRepository.count());
            log.info("  - {} categories available", categoryRepository.count());
            log.info("  - {} products in catalog", productGeneralRepository.count());
            log.info("  - {} batch details in stock", batchDetailRepository.count());
            log.info("  - {} product details for sale", productDetailRepository.count());
            log.info("  - {} active sale events", saleEventRepository.count());
            log.info("  - {} products on sale", saleProductRepository.count());
            log.info("  - {} shopping carts created", cartRepository.count());
            log.info("  - {} items in carts", cartItemRepository.count());
            log.info("  - {} customer reviews", feedBackRepository.count());
        };
    }

    // ==================== Helper Methods ====================

    private Buyer createBuyer(String buyerId, String fName, String lName, String email) {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(buyerId);
        buyer.setFName(fName);
        buyer.setLName(lName);
        buyer.setEmail(email);
        return buyerRepository.save(buyer);
    }

    private Address createAddress(String buyerId, String receiverName, String receiverPNum,
                                  String province, String district, String commune, String detail, Boolean isDefault) {
        Address address = new Address();
        address.setBuyerId(buyerId);
        address.setReceiverName(receiverName);
        address.setReceiverPNum(receiverPNum);
        address.setProvince(province);
        address.setDistrict(district);
        address.setCommune(commune);
        address.setDetail(detail);
        address.setIsDefault(isDefault);
        return addressRepository.save(address);
    }

    private Category createCategory(Long categoryId, String categoryName, Integer displayOrder,
                                   String description, String iconUrl, String isSubCategory, Long belongToCategory) {
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName(categoryName);
        category.setDisplayOrder(displayOrder);
        category.setDescription(description);
        category.setIconUrl(iconUrl);
        category.setIsSubCategory(isSubCategory);
        category.setBelongToCategory(belongToCategory);
        return categoryRepository.save(category);
    }

    private ProductGeneral createProductGeneral(Long productGeneralId,
                                               String name, String description, String[] tags,
                                               ProductGeneral.Unit unit, Long unitQuantity, Long categoryId) {
        ProductGeneral productGeneral = new ProductGeneral();
        productGeneral.setProductGeneralId(productGeneralId);
        productGeneral.setCategoryId(categoryId);
        productGeneral.setName(name);
        productGeneral.setDescription(description);
        productGeneral.setTags(tags);
        productGeneral.setUnit(unit);
        productGeneral.setUnitQuantity(unitQuantity);
        productGeneral.setImg("https://pub-0365edd1781141cdb68675969c7cdb87.r2.dev/5fe95e6c-c064-49c4-8712-1c8f54472502.jpg");
        return productGeneralRepository.save(productGeneral);
    }

    private BatchDetail createBatchDetail(String batchDetailId, Long productGeneralId, int quantity,
                                         BigDecimal price, String detailContent) {
        BatchDetail batchDetail = new BatchDetail();
        batchDetail.setBatchDetailId(batchDetailId);
        batchDetail.setProductGeneralId(productGeneralId);
        batchDetail.setQuantity(quantity);
        batchDetail.setPrice(price);
        batchDetail.setAvgRate(BigDecimal.ZERO);
        batchDetail.setNumRate(0);
        batchDetail.setDetailContent(detailContent);
        return batchDetailRepository.save(batchDetail);
    }

    private ProductDetail createProductDetail(Long productGeneralId, String batchId,
                                             String detailContent, BigDecimal rating, String buyYn) {
        ProductDetail productDetail = new ProductDetail();
        productDetail.setProductGeneralId(productGeneralId);
        productDetail.setBatchId(batchId);
        productDetail.setDetail(detailContent);
        productDetail.setImg("https://pub-0365edd1781141cdb68675969c7cdb87.r2.dev/5fe95e6c-c064-49c4-8712-1c8f54472502.jpg");
        productDetail.setStatus("ACTIVE");
        productDetail.setRating(rating);
        productDetail.setBuyYn(buyYn);
        return productDetailRepository.save(productDetail);
    }

    private SaleEvent createSaleEvent(String name, String description, String img,
                                     Long displayPriority, String activeYn, String enabledYn,
                                     LocalTime beginTime, LocalTime endTime,
                                     LocalDateTime beginDate, LocalDateTime endDate,
                                     Object detail) {
        SaleEvent saleEvent = new SaleEvent();
        saleEvent.setName(name);
        saleEvent.setDescription(description);
        saleEvent.setImg(img);
        saleEvent.setDisplayPriority(displayPriority);
        saleEvent.setActiveYn(activeYn);
        saleEvent.setEnabledYn(enabledYn);
        saleEvent.setBeginTime(beginTime);
        saleEvent.setEndTime(endTime);
        saleEvent.setBeginDate(beginDate);
        saleEvent.setEndDate(endDate);
        saleEvent.setDetail(detail);
        return saleEventRepository.save(saleEvent);
    }

    private SaleProduct createSaleProduct(Long saleEventId, String batchId, Integer disVal,
                                         Long maxQty, Long curQty, Long maxBuy) {
        SaleProduct saleProduct = new SaleProduct();
        saleProduct.setSaleEventId(saleEventId);
        saleProduct.setBatchId(batchId);
        saleProduct.setDisVal(disVal);
        saleProduct.setMaxQty(maxQty);
        saleProduct.setCurQty(curQty);
        saleProduct.setMaxBuy(maxBuy);
        return saleProductRepository.save(saleProduct);
    }

    private Coupon createCoupon(Long couponId, String couponCode, Long totalQuantity, Long currentQuantity, DiscountType discountType, Long discountValue, Long maxDiscountAmount, Long minOrderValue, LocalDateTime expiredAt) {
        Coupon coupon = new Coupon();
        coupon.setCouponId(couponId);
        coupon.setCouponCode(couponCode);
        coupon.setTotalQuantity(totalQuantity);
        coupon.setCurrentQuantity(currentQuantity);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setMaxDiscountAmount(maxDiscountAmount);
        coupon.setMinOrderValue(minOrderValue);
        coupon.setExpiredAt(expiredAt);
        return couponRepository.save(coupon);
    }

    private Cart createCart(String buyerId, Long totalPrice) {
        Cart cart = new Cart();
        cart.setBuyerId(buyerId);
        cart.setTotalPrice(totalPrice);
        return cartRepository.save(cart);
    }

    private CartItem createCartItem(Long cartId, String batchDetailId, Long quantity, Boolean isSelected) {
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setBatchDetailId(batchDetailId);
        cartItem.setQuantity(quantity);
        cartItem.setIsSelected(isSelected);
        return cartItemRepository.save(cartItem);
    }

    private FeedBack createFeedBack(String buyerId, Long productDetailId, BigDecimal rating, String content) {
        FeedBack feedBack = new FeedBack();
        feedBack.setBuyerId(buyerId);
        feedBack.setProductDetailId(productDetailId);
        feedBack.setRating(rating);
        feedBack.setContent(content);
        feedBack.setReplyId(null);
        feedBack.setImg(null);
        feedBack.setDetail(null);
        return feedBackRepository.save(feedBack);
    }
}
