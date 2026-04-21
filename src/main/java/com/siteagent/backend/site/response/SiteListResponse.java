package com.siteagent.backend.site.response;

import com.siteagent.backend.site.SiteListProjection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SiteListResponse {
    private Long id;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String managerName;
    private String managerPhone;

    public static SiteListResponse from(SiteListProjection siteList) {
        return new SiteListResponse(
            siteList.getId(),
            siteList.getName(),
            siteList.getAddress(),
            siteList.getLat(),
            siteList.getLng(),
            siteList.getManagerName(),
            siteList.getManagerPhone()
        );
    }
}