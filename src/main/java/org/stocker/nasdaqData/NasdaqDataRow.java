package org.stocker.nasdaqData;

/**
 *  typical row format
 */
public enum  NasdaqDataRow {

    SYMBOL("SYMBOL"),
    NAME("NAME"),
    OPEN("OPEN"),
    HIGH("HIGH"),
    LOW("LOW"),
    CLOSE("CLOSE"),
    ADJ_CLOSE("ADJ_CLOSE"),
    VOLUME("VOLUME"),
    TIMESTAMP("TIMESTAMP");

    String rowData;

    NasdaqDataRow(String rowData) {
        this.rowData = rowData;
    }
}
