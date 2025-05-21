package com.example.vkr2.entity;

public enum CarStatus {
    IN_REPAIR("На ремонте"),
    IN_USE("Используется"),
    IN_MAINTENANCE("На обслуживании");

    private final String displayName;

    CarStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}