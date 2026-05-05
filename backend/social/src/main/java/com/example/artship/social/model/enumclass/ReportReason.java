package com.example.artship.social.model.enumclass;

public enum ReportReason {
    SPAM("Спам"),
    HARASSMENT("Домогательства/Оскорбления"),
    COPYRIGHT("Нарушение авторских прав"),
    VIOLENCE("Насилие/Жестокость"),
    ADULT("Взрослый контент"),
    HATE_SPEECH("Разжигание ненависти"),
    OTHER("Другое");
    
    private final String displayName;
    
    ReportReason(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}