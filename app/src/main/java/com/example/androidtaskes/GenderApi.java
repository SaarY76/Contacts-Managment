package com.example.androidtaskes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GenderApi
{
    @GET("/")
    Call<GenderResponse> getGender(@Query("name") String name);
}
