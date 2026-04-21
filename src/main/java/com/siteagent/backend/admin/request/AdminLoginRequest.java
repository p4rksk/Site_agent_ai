package com.siteagent.backend.admin.request;


public record AdminLoginRequest(
   String loginId,
   String password
) {}
