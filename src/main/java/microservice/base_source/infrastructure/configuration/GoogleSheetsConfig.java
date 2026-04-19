package microservice.base_source.infrastructure.configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {
    
    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;
    
    @Bean
    public Sheets sheetsService() throws Exception {
        InputStream credStream = getClass()
                .getClassLoader()
                .getResourceAsStream(credentialsPath);
        
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credStream)
                .createScoped(List.of(SheetsScopes.SPREADSHEETS_READONLY));
        
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("Capstone Payment").build();
    }
}
