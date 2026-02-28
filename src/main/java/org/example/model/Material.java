package org.example.model;

import java.sql.Timestamp;

public class Material {
    private String materialID;
    private String materialTitle;
    private String materialContent;
    private String userID;
    private String uploaderName;
    private String materialSubject;
    private String materialState;
    private Timestamp uploadTime;

    // Default constructor
    public Material() {
    }

    // Full constructor
    public Material(String materialID, String materialTitle, String materialContent, String userID, String uploaderName, String materialSubject, String materialState, Timestamp uploadTime) {
        this.materialID = materialID;
        this.materialTitle = materialTitle;
        this.materialContent = materialContent;
        this.userID = userID;
        this.uploaderName = uploaderName;
        this.materialSubject = materialSubject;
        this.materialState = materialState;
        this.uploadTime = uploadTime;
    }

    // Getters and Setters
    public String getMaterialID() {
        return materialID;
    }

    public void setMaterialID(String materialID) {
        this.materialID = materialID;
    }

    public String getMaterialTitle() {
        return materialTitle;
    }

    public void setMaterialTitle(String materialTitle) {
        this.materialTitle = materialTitle;
    }

    public String getMaterialContent() {
        return materialContent;
    }

    public void setMaterialContent(String materialContent) {
        this.materialContent = materialContent;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getMaterialSubject() {
        return materialSubject;
    }

    public void setMaterialSubject(String materialSubject) {
        this.materialSubject = materialSubject;
    }

    public String getMaterialState() {
        return materialState;
    }

    public void setMaterialState(String materialState) {
        this.materialState = materialState;
    }

    public Timestamp getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }
}