package com.example.lizhi.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id") // 映射到数据库中的 supplier_id 字段
    private Integer supplierId; // 属性名保持不变，但映射的列名已修改

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "contact")
    private String contact;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "varieties")
    private String varieties;

    @Column(name = "cooperation_start_date")
    private String cooperationStartDate;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    // Getters and Setters

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVarieties() {
        return varieties;
    }

    public void setVarieties(String varieties) {
        this.varieties = varieties;
    }

    public String getCooperationStartDate() {
        return cooperationStartDate;
    }

    public void setCooperationStartDate(String cooperationStartDate) {
        this.cooperationStartDate = cooperationStartDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}