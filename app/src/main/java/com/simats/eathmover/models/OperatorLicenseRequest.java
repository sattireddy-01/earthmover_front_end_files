package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for submitting operator license details and machine images
 */
public class OperatorLicenseRequest {
    @SerializedName("operator_id")
    private String operatorId;

    @SerializedName("license_no")
    private String licenseNo;

    @SerializedName("rc_number")
    private String rcNumber;

    @SerializedName("machine_model")
    private String machineModel;

    @SerializedName("machine_year")
    private String machineYear;

    @SerializedName("machine_image_1")
    private String machineImage1; // Base64 encoded image

    @SerializedName("machine_image_2")
    private String machineImage2; // Base64 encoded image

    @SerializedName("machine_image_3")
    private String machineImage3; // Base64 encoded image

    @SerializedName("equipment_type")
    private String equipmentType;

    // Getters and setters
    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getRcNumber() {
        return rcNumber;
    }

    public void setRcNumber(String rcNumber) {
        this.rcNumber = rcNumber;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(String machineModel) {
        this.machineModel = machineModel;
    }

    public String getMachineYear() {
        return machineYear;
    }

    public void setMachineYear(String machineYear) {
        this.machineYear = machineYear;
    }

    public String getMachineImage1() {
        return machineImage1;
    }

    public void setMachineImage1(String machineImage1) {
        this.machineImage1 = machineImage1;
    }

    public String getMachineImage2() {
        return machineImage2;
    }

    public void setMachineImage2(String machineImage2) {
        this.machineImage2 = machineImage2;
    }

    public String getMachineImage3() {
        return machineImage3;
    }

    public void setMachineImage3(String machineImage3) {
        this.machineImage3 = machineImage3;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }
}









