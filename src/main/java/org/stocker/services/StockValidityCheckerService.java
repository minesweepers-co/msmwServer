package org.stocker.services;

import org.stocker.nseData.NseDataObj;
import org.stocker.nseData.NseDataRow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class StockValidityCheckerService {
    protected final static DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    public static boolean isDataValid(NseDataObj dataObj){
        Date expiryDate;
        Date currentDate;
        try {
            expiryDate = dateFormat.parse(dataObj.rowData.get(NseDataRow.EXPIRY_DT).toUpperCase(Locale.US));
            currentDate = dateFormat.parse(dataObj.rowData.get(NseDataRow.TIMESTAMP).toUpperCase(Locale.US));
        } catch (ParseException e) {
            // coudnt parse date , invalid data
            return false;
        }

        return ( satisfiesCurrentMonthCheck(currentDate, expiryDate)
                || satisfiesNextMonthCheck(currentDate, expiryDate) )
                && ( !dataObj.rowData.get(NseDataRow.LOW).equals("0")
                     && !dataObj.rowData.get(NseDataRow.HIGH).equals("0")
                     && !dataObj.rowData.get(NseDataRow.CLOSE).equals("0"));

    }

    // TODO : move to joda time as java date interface is deprecated
    protected static boolean satisfiesCurrentMonthCheck(Date currentDate, Date expiryDate){
        return (expiryDate.after(currentDate) &&
                (expiryDate.getMonth() == currentDate.getMonth()) &&
                (expiryDate.getYear() == currentDate.getYear()));
    }

    protected static boolean satisfiesNextMonthCheck(Date currentDate, Date expiryDate){
        Calendar currentCalender = Calendar.getInstance();
        currentCalender.setTime(currentDate);
        Calendar expiryCalender = Calendar.getInstance();
        expiryCalender.setTime(expiryDate);
        int diffYear = expiryCalender.get(Calendar.YEAR) - currentCalender.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + expiryCalender.get(Calendar.MONTH) - currentCalender.get(Calendar.MONTH);
        return diffMonth == 1;
    }
}
