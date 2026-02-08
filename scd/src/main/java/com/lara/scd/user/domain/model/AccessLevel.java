package com.lara.scd.user.domain.model;

public enum AccessLevel {
    DOCTOR("DOCTOR"),MANAGER("MANAGER");
    public final String level;
    AccessLevel(String level){
        this.level = level;
    }
}