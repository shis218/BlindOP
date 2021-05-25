package com.example.blindop;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PersonService {
    @GET("/blindopJava/login/")
    Call<Person> buscaPerson();
}