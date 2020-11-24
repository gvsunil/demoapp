package in.gore.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by samarth.gupta on 12/10/17.
 */
public class TimeHRGenerator {

    public static void main(String[] args) {
        long currUTCTimeMin = System.currentTimeMillis() / (1000 * 60);
        long currentTimeHRRoundOff = currUTCTimeMin / 60 ;
        // go back 6 months
        long startTimeMin = currentTimeHRRoundOff - 24 * 60 * 180;

        //System.out.println("startTime: " + startTimeMin + " " + startTimeMin*60);
       //System.out.println("end time: " +currentTimeHRRoundOff + " " + currentTimeHRRoundOff*60);
        long timeinmilli = System.currentTimeMillis();
        long seconds =  timeinmilli / 1000 ;
        long minutesRoundToHR = ((timeinmilli / (1000 * 60)) / 60 ) * 60;
        long sixMonthBack = minutesRoundToHR - 24 * 60 * 180;
        long sixMonthBackSeconds = sixMonthBack * 60 ;
        long backtoseconds = minutesRoundToHR * 60 ;
        System.out.println(backtoseconds + " " +  sixMonthBackSeconds);
        Date dt = new Date(minutesRoundToHR * 60 * 1000);
        Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cl.setTime(dt);
        int hrofday = cl.get(Calendar.HOUR_OF_DAY);
        int dayofweek = cl.get(Calendar.DAY_OF_WEEK) -1 ;
        int hroftheweek = dayofweek*24 + hrofday;
        int dayofmonth = cl.get(Calendar.DAY_OF_MONTH) -1;
        int hrofmonth = dayofmonth*24 + hrofday;
        System.out.println(hrofday);
        System.out.println(dayofweek);
        System.out.println(hroftheweek);
        System.out.println(dayofmonth);
        System.out.println(hrofmonth);


    }
}
