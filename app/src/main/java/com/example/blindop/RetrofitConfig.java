package com.example.blindop;


import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {

    private final Retrofit retrofit;

    public RetrofitConfig() {
        this.retrofit = new Retrofit.Builder()
               // .baseUrl("https://viacep.com.br/ws/")
                .baseUrl("http://ip-50-62-81-50.ip.secureserver.net:8080/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
    public CEPService getCEPService() {
        return this.retrofit.create(CEPService.class);
    }
    public PersonService getPersonService() {
        return this.retrofit.create(PersonService.class);
    }
}