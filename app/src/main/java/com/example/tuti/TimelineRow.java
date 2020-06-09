package com.example.tuti;

import android.graphics.Bitmap;

import java.util.Date;


public class TimelineRow {

    private int type;
    private int id;
    private long timeBegin = -1;
    private long timeEnd = -1;
    private String title = null;
    private String description = null;
    private Bitmap image = null;
    private int bellowLineColor = 0;
    private int bellowLineSize = 6;
    private int imageSize = 50;
    private int backgroundColor = 0;
    private int backgroundSize = 50;
    private int dateColor = 0;
    private int titleColor = 0;
    private int descriptionColor = 0;

    public TimelineRow(int id, int type) {
        this.id = id;
        this.type = type;
    }

    public TimelineRow(int id, int type, long timeBegin) {
        this.id = id;
        this.type = type;
        this.timeBegin = timeBegin;
    }

    public TimelineRow(int id, int type, long timeBegin, long timeEnd) {
        this.id = id;
        this.type = type;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
    }

    public TimelineRow(int id, int type, long timeBegin, long timeEnd, String title, String description) {
        this.id = id;
        this.type = type;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.title = title;
        this.description = description;
    }

    public TimelineRow(int id, int type, long timeBegin, long timeEnd, String title, String description, Bitmap image, int bellowLineColor, int bellowLineSize, int imageSize) {
        this.id = id;
        this.type = type;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.title = title;
        this.description = description;
        this.image = image;
        this.bellowLineColor = bellowLineColor;
        this.bellowLineSize = bellowLineSize;
        this.imageSize = imageSize;
    }

    public TimelineRow(int id, int type,long timeBegin, long timeEnd, String title, String description, Bitmap image, int bellowLineColor, int bellowLineSize, int imageSize, int backgroundColor, int backgroundSize) {
        this.id = id;
        this.type = type;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.title = title;
        this.description = description;
        this.image = image;
        this.bellowLineColor = bellowLineColor;
        this.bellowLineSize = bellowLineSize;
        this.imageSize = imageSize;
        this.backgroundColor = backgroundColor;
        this.backgroundSize = backgroundSize;
    }

    public TimelineRow(int id, int type, long timeBegin, long timeEnd, String title, String description, Bitmap image, int bellowLineColor, int bellowLineSize, int imageSize, int backgroundColor, int backgroundSize, int dateColor, int titleColor, int descriptionColor) {
        this.id = id;
        this.type = type;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.title = title;
        this.description = description;
        this.image = image;
        this.bellowLineColor = bellowLineColor;
        this.bellowLineSize = bellowLineSize;
        this.imageSize = imageSize;
        this.backgroundColor = backgroundColor;
        this.backgroundSize = backgroundSize;
        this.dateColor = dateColor;
        this.titleColor = titleColor;
        this.descriptionColor = descriptionColor;
    }

    public int getBackgroundSize() {
        return backgroundSize;
    }

    public void setBackgroundSize(int backgroundSize) {
        this.backgroundSize = backgroundSize;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageSize() {
        return imageSize;
    }

    public void setImageSize(int imageSize) {
        this.imageSize = imageSize;
    }

    public int getBellowLineSize() {
        return bellowLineSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public void setBellowLineSize(int bellowLineSize) {
        this.bellowLineSize = bellowLineSize;
    }

    public int getBellowLineColor() {
        return bellowLineColor;
    }

    public void setBellowLineColor(int bellowLineColor) {
        this.bellowLineColor = bellowLineColor;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDateColor() {
        return dateColor;
    }

    public void setDateColor(int dateColor) {
        this.dateColor = dateColor;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public int getDescriptionColor() {
        return descriptionColor;
    }

    public void setDescriptionColor(int descriptionColor) {
        this.descriptionColor = descriptionColor;
    }
}
