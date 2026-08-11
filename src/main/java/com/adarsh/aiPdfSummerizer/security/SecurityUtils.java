package com.adarsh.aiPdfSummerizer.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public static String getCurrentUserEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
