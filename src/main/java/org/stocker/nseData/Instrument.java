package org.stocker.nseData;

/**
 * Sample Values of Instruments
 */
public enum  Instrument {
    FUTIDX("FUTIDX"),
    FUTSTK("FUTSTK"),
    OTHERS("OTHERS");

    String value;

    Instrument(String value) {
        this.value = value;
    }
}
