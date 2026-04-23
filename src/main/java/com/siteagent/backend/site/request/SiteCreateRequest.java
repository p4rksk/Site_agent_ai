 package com.siteagent.backend.site.request;

 public record SiteCreateRequest(
    String name,
    String managerName,
    String managerPhone,
    String address,
    Double lat,
    Double lng
 ) {
}