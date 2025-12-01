package com.ctxh.volunteer.module.auth.enums;

import lombok.Getter;

@Getter
public enum AttributeLoginType {
    GOOGLE("google","sub"),
    FACEBOOK("facebook", "id"),
    GITHUB("github", "login")
    ;

    private final String loginType;
    private final String sub;

    AttributeLoginType(String loginType, String sub) {
        this.loginType = loginType;
        this.sub = sub;
    }
}