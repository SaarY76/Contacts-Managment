package com.example.androidtaskes;

import com.google.gson.annotations.SerializedName;

public class GenderResponse
{
    @SerializedName("name")
    private String name;

    @SerializedName("gender")
    private String gender;

    @SerializedName("probability")
    private double probability;

    @SerializedName("count")
    private int count;

    public GenderResponse() {}

    public GenderResponse(String name, String gender, double probability, int count)
    {
        this.name = name;
        this.gender = gender;
        this.probability = probability;
        this.count = count;
    }

    // Getters (and optionally setters) for each field
    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public double getProbability() {
        return probability;
    }

    public int getCount() {
        return count;
    }
}

