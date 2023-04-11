package com.quaterfoldvendorapp.data;

/**
 * Created by Ind3 on 15-12-17.
 */

public class Agentinfo {

    public String id;

    public String vendor_code;

    public String username;

    public String company_name;
    public String email;

    public String mobile;

    public String contact;

    public String address_line_1;

    public String address_line_2;

    public String city;

    public String state;

    public String pincode;

    public String working_cities;

    public String imei_no;


    public Agentinfo() {
    }

    public Agentinfo(String id, String username, String email, String mobile,
                     String address_line_1, String address_line_2, String city, String state,
                     String imei) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.address_line_1 = address_line_1;
        this.address_line_2 = address_line_2;
        this.city = city;
        this.state = state;
        this.imei_no = imei;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendor_code() {
        return vendor_code;
    }

    public void setVendor_code(String vendor_code) {
        this.vendor_code = vendor_code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress_line_1() {
        return address_line_1;
    }

    public void setAddress_line_1(String address_line_1) {
        this.address_line_1 = address_line_1;
    }

    public String getAddress_line_2() {
        return address_line_2;
    }

    public void setAddress_line_2(String address_line_2) {
        this.address_line_2 = address_line_2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getWorking_cities() {
        return working_cities;
    }

    public void setWorking_cities(String working_cities) {
        this.working_cities = working_cities;
    }

    public String getImei_no() {
        return imei_no;
    }

    public void setImei_no(String imei_no) {
        this.imei_no = imei_no;
    }
}
