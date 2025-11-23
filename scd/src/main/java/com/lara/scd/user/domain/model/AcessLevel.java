package com.lara.scd.user.domain.model;

public enum AcessLevel {
    DOCTOR("DOCTOR"),MANAGER("MANAGER");
    public final String level;
    AcessLevel(String level){
        this.level = level;
    }
}