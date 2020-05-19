package com.example.velmurugan.detectnoiseandroidexample.Model;

public class ItemNoise {

    private String db_count;
    private String duration;
    private String start_time;
    private String end_time;
    private String latitude;
    private String longitude;

    public ItemNoise(String db_count, String duration, String start_time, String end_time, String latitude, String longitude) {
        this.db_count = db_count;
        this.duration = duration;
        this.start_time = start_time;
        this.end_time = end_time;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getDbCOunt()

    {
        return db_count.substring(0,7) + " dB";
    }

    public String getDuration()

    {
        return duration + " sec (Duration)";
    }

    public String getStartTime()

    {
        return start_time;
    }

    public String getEndTime()

    {
        return end_time;
    }

    public String getLat()

    {
        return latitude;
    }

    public String getLong()

    {
        return longitude;
    }

    public String getStatus(){
        if (Double.valueOf(db_count) < 40){
            return "Good";
        }
        if (Double.valueOf(db_count) > 90){
            return "Noisy";
        }
        return "Normal";
    }

}