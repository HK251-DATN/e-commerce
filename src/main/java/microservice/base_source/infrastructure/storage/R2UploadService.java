package microservice.base_source.infrastructure.storage;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2UploadService {

    private final S3Client s3Client;

    @Value("${app.sale-event-banner-bucket}")
    private String bannerBucket;

    @Value("${app.sale-event-banner-public-url}")
    private String bannerPublicUrl;

    public String uploadSaleEventBanner(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bannerBucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            log.info("Uploaded sale event banner: {}", fileName);
            return bannerPublicUrl + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Banner upload failed: " + e.getMessage(), e);
        }
    }
}
