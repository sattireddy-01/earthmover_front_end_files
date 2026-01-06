package com.simats.eathmover.models;

import com.google.gson.annotations.SerializedName;

public class Machine {
    @SerializedName("machine_id")
    private int machineId;

    @SerializedName("category_id")
    private Integer categoryId;

    @SerializedName("model_name")
    private String modelName;

    @SerializedName("machine_model")
    private String machineModel;

    @SerializedName("price_per_hour")
    private double pricePerHour;

    @SerializedName("specs")
    private String specs;

    @SerializedName("model_year")
    private Integer modelYear;

    @SerializedName("image")
    private String image;

    @SerializedName("machine_image_1")
    private String machineImage1;

    @SerializedName("availability")
    private String availability;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("equipment_type")
    private String equipmentType;

    @SerializedName("operator_id")
    private Integer operatorId;

    // Legacy fields for backward compatibility
    @SerializedName("model")
    private String model;

    @SerializedName("type")
    private String type;

    @SerializedName("last_updated")
    private String lastUpdated;

    // Getters and setters
    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getModelName() {
        // Prefer machine_model, then model_name, then model
        if (machineModel != null && !machineModel.isEmpty()) {
            return machineModel;
        }
        return modelName != null ? modelName : model;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(String machineModel) {
        this.machineModel = machineModel;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getSpecs() {
        return specs != null ? specs : type;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public String getImage() {
        // Prefer machine_image_1 over image
        return machineImage1 != null && !machineImage1.isEmpty() ? machineImage1 : image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMachineImage1() {
        return machineImage1;
    }

    public void setMachineImage1(String machineImage1) {
        this.machineImage1 = machineImage1;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    // Legacy getters for backward compatibility
    public String getModel() {
        return model != null ? model : modelName;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type != null ? type : specs;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

