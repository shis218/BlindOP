package com.example.blindop;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.speech.tts.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.Button;
import java.util.*;

public class MainActivity extends AppCompatActivity {
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
   TextToSpeech dTTS=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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
       dTTS.speak("TTS carregado",dTTS.QUEUE_FLUSH,null,"TTS Carregado");
   }

});





    //Objetos desenhados
    private Button simpleButton;
    private LinearLayout linlay;
    private Bitmap background;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        simpleButton = (Button) findViewById(R.id.mainSimpleButton);
        simpleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                randomText();
            }
        });
        linlay = (LinearLayout) findViewById(R.id.simplecanvas);

        // Canvas
        background = Bitmap.createBitmap(250, 400, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(background);
       // canvas.drawBitmap(getMapBitmap(R.drawable.flat), 10f, 10f, null);


        //Array de textos adicionado
        arraydirecoes.add("Ande X passos para frente");
        arraydirecoes.add("Ande X passos para esquerda");
        arraydirecoes.add("Ande X passos para trás");
        arraydirecoes.add("Ande X passos para direita");
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
        dTTS.addSpeech(nextroute,"com.example.blindop",0);
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

    public void apertabotao(){
    //Faz a chamada CURL
        //https://square.github.io/retrofit/
    }

}

