package com.siteagent.backend.site;

public interface SiteListProjection {
    Long getId();
    String getName();
    String getAddress();
    Double getLat();
    Double getLng();
    String getManagerName();
    String getManagerPhone();
    
} 