package com.goteacher.utils.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Pesanan implements Serializable {
    public String idPesanan;
    public String idCourse;
    public String idUser;
    public String titleCourse;
    public String hargaCourse;
    public String imgBuktiBayar;

    public Pesanan(String idCourse, String idUser, String titleCourse, String hargaCourse, String imgBuktiBayar) {
        this.idCourse = idCourse;
        this.idUser = idUser;
        this.titleCourse = titleCourse;
        this.hargaCourse = hargaCourse;
        this.imgBuktiBayar = imgBuktiBayar;
    }

    public String getIdPesanan() {
        return idPesanan;
    }

    public void setIdPesanan(String idPesanan) {
        this.idPesanan = idPesanan;
    }

    public String getIdCourse() {
        return idCourse;
    }

    public void setIdCourse(String idCourse) {
        this.idCourse = idCourse;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getTitleCourse() {
        return titleCourse;
    }

    public void setTitleCourse(String titleCourse) {
        this.titleCourse = titleCourse;
    }

    public String getHargaCourse() {
        return hargaCourse;
    }

    public void setHargaCourse(String hargaCourse) {
        this.hargaCourse = hargaCourse;
    }

    public String getImgBuktiBayar() {
        return imgBuktiBayar;
    }

    public void setImgBuktiBayar(String imgBuktiBayar) {
        this.imgBuktiBayar = imgBuktiBayar;
    }
}
