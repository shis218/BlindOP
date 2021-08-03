package com.example.blindop;


import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {

    private final Retrofit retrofit;

    public RetrofitConfig() {
        this.retrofit = new Retrofit.Builder()
               // .baseUrl("https://viacep.com.br/ws/")
                .baseUrl("https://javablindop.herokuapp.com/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
    public CEPService getCEPService() {
        return this.retrofit.create(CEPService.class);
    }
    public PersonService getPersonService() {
        return this.retrofit.create(PersonService.class);
    }
    public RotaService getRotaService() {
        return this.retrofit.create(RotaService.class);
    }

}