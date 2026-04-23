 package com.siteagent.backend.admin.response;

 public record LoginResponse(
    String token,
    String role,
    Long id,
    String companyName 
) {}