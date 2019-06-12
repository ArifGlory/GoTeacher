package com.goteacher.utils.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wahyu.raya on 09/10/18.
 */
@SuppressWarnings("serial")
public class Model implements Serializable {

    public enum key {
        id,
        title,
        desc,
        category,
        creator,
        rates,
        imgURL,
        created,
        active_address,
        address,
        coordinate,
        published,
    }

    private String id;
    private String title = "";
    private String desc  = "";
    private List<String> category = new ArrayList<>();
    private String creator  = "";
    private String imgURL  = "";
    private String address  = "";
    private String coordinate = "";
    private long rates = 0;
    private long created = 0;
    private boolean published = true;
    private boolean activeAddress = false;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public long getRates() {
        return rates;
    }

    public void setRates(long rates) {
        this.rates = rates;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public boolean isActiveAddress() {
        return activeAddress;
    }

    public void setActiveAddress(boolean activeAddress) {
        this.activeAddress = activeAddress;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

}
