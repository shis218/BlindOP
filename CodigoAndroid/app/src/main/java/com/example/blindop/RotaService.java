package com.example.blindop;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RotaService {
    @GET("/rota/")
    Call<List<Rota>> buscaRota();
}