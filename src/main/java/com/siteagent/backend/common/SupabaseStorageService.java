package com.siteagent.backend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

   
    public String uploadPdf(Long siteId, MultipartFile file) {
        try {
            
            String fileName = siteId + "_" + System.currentTimeMillis() + ".pdf";
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.APPLICATION_PDF);

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Supabase 업로드 실패: " + e.getMessage());
        }
    }
}