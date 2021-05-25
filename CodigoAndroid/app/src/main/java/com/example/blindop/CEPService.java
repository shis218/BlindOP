package com.example.blindop;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CEPService {

    @GET("03068000/json/")
    //Call<CEP> buscarCEP(@Path("cep") String cep);
    Call<CEP> buscarCEP();
}