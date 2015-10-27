package org.stocker.services;

/**
 *
 */
public enum  ReportType {
    BHAV_REPORT("bhav.csv.zip"),
    OTHERS("others");

    String reportType;

    ReportType(String reportType){
        this.reportType = reportType;
    }
}
