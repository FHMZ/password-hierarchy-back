package com.hierarchy.password_hierarchy_back.utils;

import lombok.Getter;

@Getter
public enum StrengthLabel {
    WEEK("Fraca"),
    MEDIUM("Mediana"),
    GOOD("Boa"),
    STRONG("Forte");

    private final String label;

    StrengthLabel(String label) {
        this.label = label;
    }

    public static String getLabelByStrength(long strength) {
        if (strength <= 15) {
            return WEEK.getLabel();
        } else if (strength <= 60) {
            return MEDIUM.getLabel();
        } else if (strength <= 85) {
            return GOOD.getLabel();
        }
        return STRONG.getLabel();
    }
}
