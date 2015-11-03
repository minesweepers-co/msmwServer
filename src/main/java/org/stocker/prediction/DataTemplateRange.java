package org.stocker.prediction;

/**
 *
 */
public enum DataTemplateRange {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY");

    String value;

    DataTemplateRange(String value) {
        this.value = value;
    }
}
