package org.stocker.nseData;

/**
 *  typical row format
 *  - INSTRUMENT,SYMBOL,EXPIRY_DT,STRIKE_PR,OPTION_TYP,OPEN,HIGH,LOW,CLOSE,SETTLE_PR,CONTRACTS,VAL_INLAKH,OPEN_INT,CHG_IN_OI,TIMESTAMP,
 */
public enum  NseDataRow {

    INSTRUMENT("INSTRUMENT"),
    SYMBOL("SYMBOL"),
    EXPIRY_DT("EXPIRY_DT"),
    STRIKE_PR("STRIKE_PR"),
    OPTION_TYP("OPTION_TYP"),
    OPEN("OPEN"),
    HIGH("HIGH"),
    LOW("LOW"),
    CLOSE("CLOSE"),
    SETTLE_PR("SETTLE_PR"),
    CONTRACTS("CONTRACTS"),
    VAL_INLAKH("VAL_INLAKH"),
    OPEN_INT("OPEN_INT"),
    CHG_IN_OI("CHG_IN_OI"),
    TIMESTAMP("TIMESTAMP");

    String rowData;


    NseDataRow(String rowData) {
        this.rowData = rowData;
    }
}
