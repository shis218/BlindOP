package com.example.blindop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
import android.speech.tts.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import retrofit2.*;


public class MainActivity extends AppCompatActivity {

    File configs;
    //Array de textos
    ArrayList<String> arraydirecoes=new ArrayList();
    public String nextroute;
    //Pontos de interesse
    String[] interesses=new String[20];
    int[][] interesseCoordenadas=new int[20][3]; //Modo de uso: [Index do ponto de interesse] [0=x,1=y,2=z(andares)]
    String[] interessesCURL=new String[20];
    //Obstaculos
    String[] nomeObstaculos=new String[100];
    int[][] obstaculosCoordenadas=new int[100][3]; //Modo de uso: [Index do obstaculo] [Cordenada do centro 0=x,1=y,2=z(andares)]
    int[][] tamanhoObstaculo=new int[100][4];//Modo de uso: [Index do obstaculo] [expande 0:norte,1=leste,2=sul,3=oeste]
    //Sequencia de instruções
    int[][] instrucoes=new int[100][2]; //Modo de uso [Index da instrução] [0:opcode 1:Numero de controle]
  /*Lista de opcodes temporaria: onde possui X é substituido pelo numero de controle
  00: ande para frente X passos
  01: ande para direita X passos
  02: ande para trás X passos
  03: ande para esquerda X passos
  10: Cuidado com obstáculo a frente
  11: Cuidado com obstáculo a direita
  12: Cuidado com obstáculo atrás
  13: Cuidado com obstáculo a esquerda
  20: Chegou a ponto de interesse parcial: X //Buscar nome do ponto de interesse por esse index
  21: Chegou ao ponto de interesse final: X //Buscar nome do ponto de interesse por esse index
  22: Ponto de interesse com pouca movimentação de pessoas
  23: Ponto de interesse com mediana movimentação de pessoas
  24: Ponto de interesse com alta movimentação de pessoas,cuidado
  25: Alerta de possível desvio de rota
  30: Suba escada X andares
  31: Desça escada X andares
  32: Pegue o elevador, suba para o andar X
  32: Pegue o elevador, desça para o andar X
   */

    //Temporario: Mapa no formato vetor x y z para demonstração
    int[][][] mapa=new int[100][100][5]; //Dentro do z colocar o que tem lá: 0 para paredes,1 pra possui obstaculo conhecido,2 para andavel,3 botão do ponto de interesse

    //Text to speech
    TextToSpeech dTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView centralTxt = (TextView) findViewById(R.id.hwtxt);
        Button btnFala = (Button) findViewById(R.id.falar);

        Context context=getApplicationContext();
//Inicializa TTS
        dTTS=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS)
                {
                    //Seta a lingua pra do celular
                    int result = dTTS.setLanguage(Locale.getDefault());
                    if(result == TextToSpeech.LANG_NOT_SUPPORTED || result ==TextToSpeech.LANG_MISSING_DATA)
                        {
                            //Caso não possua a linguagem então coloca como ingles
                            Log.e("TTS", "Idioma não suportado");
                            dTTS.setLanguage(Locale.ENGLISH);
                        }
                }
                else {
                    //Falha no uso do recurso do TTS
                    Log.e("TTS","Inicialização falhou...");
                }


            }

        });
        btnFala.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //dTTS.speak("TTS funcionando",dTTS.QUEUE_FLUSH,null,null);
                speak("TTS Funcionando perfeitamente");
            }
        });


        //Tenta ver se o arquivo existe
        //Se nao existe, cria
        String txtchave=lechave();
        if(!txtchave.isEmpty()){
            txtchave="chave lida "+ txtchave;
        }
        else {

            txtchave="Chave criada";

            salvaChave("22313");
        }

        //Array de textos adicionado
        arraydirecoes.add("Ande X passos para frente");
        arraydirecoes.add("Ande X passos para esquerda");
        arraydirecoes.add("Ande X passos para trás");
        arraydirecoes.add("Ande X passos para direita");
        centralTxt.setText(txtchave);


    }
    //Login
    private void salvaChave(String data) {

            try {
                //Deleta arquivo antes de criar
                File arq = new File(getApplicationContext().getFilesDir(), "cfg.txt");
                arq.delete();

                FileOutputStream fou = openFileOutput("cfg.txt", MODE_APPEND);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fou);
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

    }


    public String lechave() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("cfg.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }

        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

       public void BuscaCep(){
        CEP cep=new CEP();

    Call<CEP> call = new RetrofitConfig().getCEPService().buscarCEP(cep.getText().toString());
                call.enqueue(new Callback<CEP>() {
                    @Override
                    public void onResponse(Call<CEP> call, Response<CEP> response) {
                        CEP cep = response.body();
        //                resposta.setText(cep.toString());
                    }

                    @Override
                    public void onFailure(Call<CEP> call, Throwable t) {

                    }
                });
        }


    public String nextText(int type,int number){
        String[] fala=arraydirecoes.get(type).split(" ");
        StringBuilder resultado=new StringBuilder();
        for (int i=0; i<fala.length;i++ ) {
            if(!fala[i].equals("X")) {
                resultado.append(fala[i]+" ");
            }
            else{
                //Caso opcode não seja de localização, substitui X por numero
                if(type<20||type>=30) {
                    resultado.append(number + " ");
                }
                else{
                    //Caso opcode esteja entre 20 e 29,procura number como index de localização
                    resultado.append(interesses[number]+ " ");
                }
            }

        }

        return resultado.toString();
    }


    public void randomText(){
        Random geranumero=new Random();
        int a= (int) (geranumero.nextInt(4));
        int b= (int) geranumero.nextInt(10);
        nextroute=nextText(a,b);
      //  dTTS.addSpeech(nextroute,"com.example.blindop",0);
    }


    //Operações de inicialização: Preenche pontos de interesse
    public void adicionaInteresse(int index,String nome,int coordenadax,int coordenaday,int coordenadaz){
        interesses[index]=nome;
        interesseCoordenadas[index][0]=coordenadax;
        interesseCoordenadas[index][1]=coordenaday;
        interesseCoordenadas[index][2]=coordenadaz;
    }

    public void adicionaObstaculo(int index,String nome,int coordenadax,int coordenaday,int coordenadaz,boolean ehparede){
        nomeObstaculos[index]=nome;
        obstaculosCoordenadas[index][0]=coordenadax;
        obstaculosCoordenadas[index][1]=coordenaday;
        obstaculosCoordenadas[index][2]=coordenadaz;
        if(ehparede){
            mapa[coordenadax][coordenaday][coordenadaz]=2;
        }
        else{
            mapa[coordenadax][coordenaday][coordenadaz]=1;
        }
    }

    public void speak(final String text){ // make text 'final'

        // ... do not declare tts here

        dTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = dTTS.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    } else {
                        dTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null,null);
                    }
                } else {
                    Log.e("TTS", "Failed");
                }
            }
        });
    }

}