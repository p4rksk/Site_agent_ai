package com.siteagent.backend.site.response;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SiteDetailResponse {
    private Long id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String managerName;
    private String managerPhone;
    private List<PdfInfo> pdfs;  // PDF 목록도 같이

   

    @Getter
    @AllArgsConstructor
    public static class PdfInfo {
        private Long id;
        private String fileName;
        private String filePath;
    }
}