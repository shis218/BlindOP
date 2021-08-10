package com.example.HerokuBlind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.*;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

//Devolvendo pagina em HTML
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.core.net.SyslogOutputStream;

/**
 * @author      Henrique Nakaema Simões <linknakaema@gmail.com>
 * @version     1.0                
 * @since       1.0         
 */

/**
 * Classe principal RestController, responsavel pelos endpoints, utiliza a anotação @RestController antes da classe e
 * para fazer os endpoints, é utilizado a anotação @GetMapping("/nomedoendpoint") antes de um metodo para fazer com que seu retorno seja exibido como resposta a um get em tal pagina. Uma das possibilidades é devolver um String e montar uma pagina HTML a partir disso. Outra possibilidade bastante usada nesse projeto é a devolução de um JSON.
 */
@RestController
public class restcontroller {
	/**
	 * variaveis globais para salvar os grafos
	 */
		private String globGraph="";
		private String globNonDirGraph="";
		private String globDij="";

		
		private final AtomicLong counter = new AtomicLong();
		
		
		/**
		 * Primeiro HTML de entrada, pode ser considerado como o indexhtml, tem função de direcionar para as outras paginas
		 * @return
		 */
		@GetMapping("/")
		public String greeting() {
			StringBuilder sb=new StringBuilder();
			//int[][][] intmap=getMapa(nomemapa);
			
			
			sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
					+ "\r\n"
					+ "Bem vindo ao BlindOP, o seu operador de rotas em ambientes indoor"
					+ "Links uteis:<br>"
					+ "<a href=\"/mapa\">mapa</a><br>"
					+ "<a href=\"/criaPontosDeInteresse\">Cria ponto de interesse</a><br>"
					+ "<a href=\"ConectaPontoDeInteresse\">Conecta ponto de interesse</a><br>"
					+ "<a href=\"dij\">Apenas o dijkstra</a><br>"
					+ "<a href=\"dijgrafo\">Grafo em Dijsktra[Não implementado]</a><br>"
					
					+ "<Form action=\"rota\" method=\"get\">Rota ini: "
					+"<input type=\"text\" id=\"inicio\" name=\"inicio\"> -> "
					+"<input type=\"text\" id=\"fim\" name=\"fim\"><button> BuscaRota</button></form>ss"
					+ "<a href=\"EntopeMapa\">Restaura mapa</a><br>"
					+ "</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
			return sb.toString();
		}
		
		/**
		 * devolve um JSON processado dos dados de login que estão no fuseki baseado no nome e key passados
		 * @param nome
		 * @param key
		 * @return classe Person
		 */
		@GetMapping("/login")
		public Person person(@RequestParam(value = "name", defaultValue = "World") String nome,@RequestParam(value = "key", defaultValue = "42") String key) {
			Person p= new Person();
			p.setId(counter.incrementAndGet()+"");
			p.setKey(key);
			p.setNome(nome);
			return p;
		}
		
		/**
		 * {@return Utiliza o metodo getChaves para devolver a lista completa de logins}
		 */
		@GetMapping("/searchLogins")
		public Object[] searchLogins() {
			Object[] pt= getChaves();

		    return pt;
			
			
			//return p;
		}
		
		
		/**
		 * Tenta inserir o login no fuseki, inicialmente buscando todas as chaves, então utilizando um random, comparar se a chave já existe, se existir, gera uma nova chave, se nao existir, insere a nova chave no fuseki
		 * @return a lista de chaves atualizada pelo getchaves()
		 */
		@GetMapping("/newLogin")
		public Object[] newLogin() {
			int rA=(int) ((Math.random() * (1000)));
			Object[] pt= getChaves();
			for(int i=0;i<pt.length;i++) {
				if((Integer.parseInt( ((Person) pt[i]).getKey()) )==rA) {
					//Volta no inicio do for com um novo numero
					System.out.println("Tenta nova chave" + (Integer.parseInt( ((Person) pt[i]).getKey()) ) +" " +rA);
					rA=(int) ((Math.random() * (1000)));
					i=0;
				};
			}
			//Se saiu do for, insere novo num
			System.out.println("Tentando inserir: "+rA);
			InsereNum(rA);
			System.out.println("Inserido: "+rA);
			pt= getChaves();
		    return pt;
			
			
			//return p;
		}
		
		/**
		 * Utiliza o metodo Dijkstra para verificar o menor caminho do Inicio e do Fim, então faz alguns tratamentos de string para buscar informações de cada nó e cada aresta de conexão entre os dois vertices definidos pelo caminho resultante do Dijkstra.
		 * Foi utilizado uma estrategia de verificar se o retorno de GenericSearch era null, então pesquisando pelo nome de sujeito no Fuskei ao contrario
		 * Caso não tenha uma remoção no meio do programa, é considerado que ao menos um desses dois ira ter retorno, já que eles foram utilizados no Dijkstra pra formular sua resposta.
		 * Notas adicionais: 
		 * rq2[0][0] se refere ao peso(distancia em metros) do unico elemento que deve ter sido retornado na pesquisa
		 * numero de passos foi calculado como 1/3 dos metros
		 * rq3[0][0] se refere a qual direção do unico elemento que deve ter sido retornado na pesquisa
		 * Foi feito uma solução para que caso seja sempre feito da forma correta as inserções, quando se busca o generic search inverso, ele também inverta a direção que é adicionado na rota
		 * exemplo: Considerando que a adição entrada-corredor é feita na direção Sul, então um caminho corredor->entrada é em direção norte 
		 * @param mapa [Nome do mapa, não utilizado]
		 * @param vertIniName [Qual o inicio]
		 * @param vertFimName [Qual o fim]
		 * @return Json com o array de cada nó de rota que deve ser usado
		 * @throws InterruptedException
		 */
		
		@GetMapping("/rota")
		public Rota[] rota(@RequestParam(value = "lugar", defaultValue = "mapaEach") String mapa,@RequestParam(value = "inicio", defaultValue = "Entrada") String vertIniName,@RequestParam(value = "fim", defaultValue = "Saida") String vertFimName) throws InterruptedException {
			
			String txt=Dijkm(vertIniName,vertFimName);
			System.out.println(txt+"\n");
		//	Rota[] rtm=getrota(mapa);
			txt=txt.replace("[", "")	;
			txt=txt.replace("]", "")	;
			String[] pontos= txt.split(",");
			//Tamanho é -1 pois na ultima posição já chegou no destino e não precisa andar mais
			Rota[] rts=new Rota[pontos.length-1];
			
			for(int in=0;in<pontos.length-1;in++) {
				boolean inverte=false;
				Rota r=new Rota();
				

				String nome1=pontos[in].trim();
				String nome2=pontos[in+1].trim();
				r.setNumeroSequencia(in);
				r.setNome(nome1+" até "+nome2);
				String[][] rq2=this.genericSearch1("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nome1+""+nome2+">", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#weight>", "?A");
				if(rq2==null) {
					rq2=this.genericSearch1("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nome2+""+nome1+">", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#weight>", "?A");
				}
				rq2[0][0]=CleanXSD(rq2[0][0]);
				r.setMetros(Float.parseFloat(rq2[0][0]));
				r.setPassos((int)Float.parseFloat(rq2[0][0])/3);
				String[][] rq3=this.genericSearch1("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nome1+""+nome2+">", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct>", "?A");
				if(rq3==null) {
					inverte=true;
					rq3=this.genericSearch1("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nome2+""+nome1+">", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct>", "?A");
				}
				rq3[0][0]=CleanXSD(rq3[0][0]);
				if(inverte==false) {
				r.setDirecao(CleanXSD(rq3[0][0]));
				}
				else {
					//Caso seja buscado o inverte, então muda a direção proposta
					if(rq3[0][0].toLowerCase().contains("sul")) {
						r.setDirecao("norte");
					}
					if(rq3[0][0].toLowerCase().contains("norte")) {
						r.setDirecao("sul");
					}
					if(rq3[0][0].toLowerCase().contains("leste")) {
						r.setDirecao("oeste");
					}
					if(rq3[0][0].toLowerCase().contains("oeste")) {
						r.setDirecao("sul");
					}
				}
				rts[in]=r;
				}
				
			
			return rts;
		}
		
		/**
		 * Preparações:
		 * >Chama o metodo getMapa para ter a matriz de paredes do mapa
		 * >Chama o metodo getPontosDeInteresse para ter a matriz de pontos de interesse contendo [index][0] nome [index][1] coordenadaX [index][1] coordenadaY
		 * >Chama o Dijkstra para entrada e saida apenas para produzir o grafo não direcionado sem pesos do mapa e salva na variavel md para uso futuro
		 * >faz um replace em md para manter a usabilidade do Mermaid. (Talvez isso não seja necessario, fazer testes no futuro)
		 * 
		 * Execução da escrita da pagina:
		 * >Utilizando um string builder, adiciona as tags basicas do HTML
		 * >Cria um canvas
		 * >Abre um JavaScript que trabalha com preencher esse canvas, o atributo lineWidth pode ser usado para alterar a grossura da linha e fillStyle a cor dos elementos escritos posteriormente.
		 * >Faz um for percorrendo a matriz do mapa,  utiliza a combinação no canvas moveTo+lineTo passando um par ordenado de inicio e fim da linha, então utiliza o metodo Stroke() pra desenhar essa linha, esta foi a solução usada para desenhar o mapa em um canvas HTML
		 * >Caso no get possua um rotaIni(pode ser feito no formulario no fim da pagina ou passado diretamente na url) executa o dijkstra e coloca uma linha pela rota
		 * Foi utilizado a informações de pontos de interesses obtidas na preparação para pegar a coordenada de cada ponto de interesse e usar o combo moveTo e LineTo para criar as linhas
		 * >Percorre a lista de pontos de interesse, colocando seus nomes no canvas. Obs: Por conta da solução encontrada para colocar nomes entre as ligações como "meio+numero", caso o ponto de interesse contenha a substring "meio", então ela não é exibida de forma escrita no mapa nem listada nos pontos de interesse do aplicativo. Mas ira aparecer no grafo
		 * >Faz um script de chamada do Mermaid e coloca entre as divs "mermaid" o texto que produz o grafo. Este texto é gerado pelo metodo dijkstra.
		 * >Faz um formulario para chamar rota para esta mesma pagina. 
		 * >Finaliza as tags html e retorna a pagina.
		 * @param nomemapa [Nao utilizado corretamente]
		 * @param rotar [Caso tenha rota, Qual o inicio]
		 * @param rotaro [Caso tenha rota, Qual o fim]
		 * @return String de HTML de um mapa dinamico, junto com o grafo deste mapa
		 * @throws InterruptedException
		 */
		@GetMapping("/mapa")
		public String mapear(@RequestParam(value = "lugar", defaultValue = "mapaEach") String nomemapa,@RequestParam(value = "rotaIni", defaultValue = "ch0") String rotar,@RequestParam(value = "rotaFim", defaultValue = "ch0") String rotaro) throws InterruptedException {
			StringBuilder sb=new StringBuilder();
			
			int[][][] intmap=getMapa(nomemapa);
			//int[][][] intmap=null;
			String[][] strPontosDeInteresse=this.getPontosDeInteresse();		
			Dijkm("Entrada","saida");
		    String md=this.globNonDirGraph;
		    md=md.replaceAll("(\r\n|\n)", "\n");
		        
		        
		        
			sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
					+ "\r\n");
			
					sb.append("<br><canvas id=\"myCanvas\" width=\"800\" height=\"800\"\r\n"
					+ "style=\"border:1px absolute; top:0; left:0; solid #c3c3c3;\">\r\n"
					+ "Your browser does not support the canvas element.\r\n"
					+ "</canvas>\r\n"
					+ "\r\n"
					+ "<script>\r\n"
					+ "var canvas = document.getElementById(\"myCanvas\");\r\n"
					+ "var ctx = canvas.getContext(\"2d\");\r\n"
					+ "ctx.lineWidth = 1.2;\r\n");
			
			for(int i=0;i<intmap[0].length;i++) {
			
			sb.append("ctx.moveTo("+intmap[0][i][0]+","+ intmap[0][i][1]+");");
			sb.append("ctx.lineTo("+intmap[0][i][2]+","+ intmap[0][i][3]+");");
			sb.append("ctx.stroke();\n");
			}
			Rota[] rts=null;
			if(!rotar.equals("ch0")) {
				String txt=Dijkm(rotar,rotaro);
				txt=txt.replace("[", "")	;
				txt=txt.replace("]", "")	;
				String[] pontos= txt.split(",");
				//Tamanho é -1 pois na ultima posição já chegou no destino e não precisa andar mais
				rts=new Rota[pontos.length-1];
			sb.append("ctx.lineWidth = 0.2;\r\n ctx.fillStyle = \"#FF0000\";");
				
				for(int in=0;in<pontos.length-1;in++) {
					
					Rota r=new Rota();				
					String nome1=pontos[in].trim();
					for(int j=0;j<strPontosDeInteresse.length;j++) {
						if(strPontosDeInteresse[j][0].equals(nome1)) {
							sb.append("ctx.moveTo("+strPontosDeInteresse[j][1]+","+ strPontosDeInteresse[j][2]+");");
						}
						
						
							
					}
					String nome2=pontos[in+1].trim();
					for(int j=0;j<strPontosDeInteresse.length;j++) {
						if(strPontosDeInteresse[j][0].equals(nome2)) {
							sb.append("ctx.lineTo("+strPontosDeInteresse[j][1]+","+ strPontosDeInteresse[j][2]+");");
							sb.append("ctx.stroke();\n");
							
						}
					}
				
				
				
				}
			}
				sb.append("ctx.font = \"12px Arial\";");
					
					for(int i=0;i<strPontosDeInteresse.length;i++) {
						if(!(strPontosDeInteresse[i][0]==null)) {
							if(!strPontosDeInteresse[i][0].contains("meio")) {
							//sb.append(strPontosDeInteresse[i][0]+"<br> \n");
							sb.append("ctx.fillText(\""+strPontosDeInteresse[i][0]+"\","+strPontosDeInteresse[i][1]+","+strPontosDeInteresse[i][2]+");\n");
							}
						}
					}
					sb.append("</script>\r\n");
					sb.append("\r\n"
					+"<div class=\"mermaid\"> ");
					sb.append(md);        
					sb.append(";</div>"
	        		+ "        <script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>\r\n"
					+ "        <script>\r\n"
					+ "            mermaid.initialize({ startOnLoad: true });\r\n"
					+ "        </script>\r\n"
	        
					+ "<form>"   //Fazendo um form que leva a propria pagina
					+ "Inicio:<input name='rotaIni' id='rotaIni' value='"+rotar+"'><br>"
					+ "Fim:<input name='rotaFim' id='rotaFim' value='"+rotaro+"'><br>"
					+ "<button>Rota</button><br>"
					+ "</form>"
					+ "</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
//					System.out.print(sb.toString());
			return sb.toString();
		}
		
		/**
		 * Formulario para criação de mapa
		 * Caso o valor não seja o default "Parede0", produz uma string de insert sparql com os valores de variavel nos parametros, então chama a função InsertGenerico que faz o update com essa string passada.
		 * @param nomemapa
		 * @param nomeParede
		 * @param posXini
		 * @param posXfim
		 * @param posYini
		 * @param posYfim
		 * @return
		 */
		@GetMapping("/criamapa")
		public String criamapa(@RequestParam(value = "lugar", defaultValue = "mapaEach") String nomemapa,@RequestParam(value = "nomeParede", defaultValue = "Parede0") String nomeParede,  @RequestParam(value = "posXini", defaultValue = "0") String posXini,  @RequestParam(value = "posXfim", defaultValue = "0") String posXfim,  @RequestParam(value = "posYini", defaultValue = "0") String posYini,  @RequestParam(value = "posYfim", defaultValue = "0") String posYfim) {
			StringBuilder sb=new StringBuilder();
			//int[][][] intmap=getMapa(nomemapa);
			
			
			sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
					+ "\r\n"
					+ "<form>"   //Fazendo um form que leva a propria pagina
					+ "Nome da Parede: <input name='nomeParede' id='nomeParede' value='"+nomeParede+"'><br>"
					+ "posXini:<input name='posXini' id='posXini' value='"+posXini+"'><br>"
					+ "posXfim:<input name='posXfim' id='posXfim' value='"+posXfim+"'><br>"
					+ "posYini:<input name='posYini' id='posYini' value='"+posYini+"'><br>"
					+ "posYfim:<input name='posYfim' id='posYfim' value='"+posYfim+"'><br>"
					+ "<button>ProximaLinha</button><br>"
					+ "</form>"
					+ "</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
			if(!nomeParede.equals("Parede0")){
			//Insere coordenadas
			String string="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
					+ "INSERT DATA{	\r\n"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeParede+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaXInicio> "+posXini+"; \r\n"
	                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaXFim> "+posXfim+";"
	                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaYinicio> "+posYini+";"
	                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaYFim> "+posYfim+"."	
	                + "};";
			this.InsertGenerico(string);
			//Insere referencia no mapa 
			String string2="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
					+ "INSERT DATA{	\r\n"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomemapa+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#uri> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeParede+">. \r\n"
	                + "};";
			
			this.InsertGenerico(string2);
			}
			
			return sb.toString();
		}
		
		/**
		 * Coloca o resultado de Dijkstra como HTML
		 * @param vertIniName
		 * @param vertFimName
		 * @return
		 * @throws InterruptedException
		 */
		@GetMapping("/dij")
		public String Dijk(@RequestParam(value = "inicio", defaultValue = "Entrada") String vertIniName,@RequestParam(value = "fim", defaultValue = "Saida") String vertFimName) throws InterruptedException {

		        String txt=Dijkm(vertIniName,vertFimName);
		        StringBuilder sb=new StringBuilder();
		        sb.append("<!DOCTYPE html>\r\n"
						+ "<html>\r\n"
						+ "<body>\r\n");
		        sb.append(txt);        
		        sb.append("</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
						
			return sb.toString();
		}
		
		/**
		 * metodo de Dijkstra, utiliza o criado pelo autor ArtLovan
		 * Preparações:
		 * >Chama o metodo getPontosDeInteresse para ter a matriz de pontos de interesse contendo [index][0] nome [index][1] coordenadaX [index][1] coordenadaY
		 * >Coloca a string "graph TD\n" no grafo que sera passado globalmente para gerar scripts pro mermaid
		 * >Cria uma hashtable que o par é de "nome do vertice" e "classe vertice" com o proposito de recuperar a classe de forma facil
		 * >Cria um array apenas com os nomes, dessa forma sendo possivel iterar sobre eles, no futuro uma boa refatoração de codigo seja apenas usar os dados obtididos no getpontos de interesse, pois estao redundantes
		 * 
		 * Algoritmo:
		 * Faz um FOR por todos pontos de interesse e cria uma instancia de classe vertice, preenchendo com nome, assim como colocando na hashtable e salvando o nome no vetor Nomes 
		 * Faz um FOR pelos nomes, buscando todas as conexões para o ponto de interesse que contem o nome atual 
		 * Percorre a lista de conexões, recupera o vertice que contem o nome dessa conexão, adiciona adjacencia com o vertice atual usando a informação de distancia
		 * Também é escrito nas variaveis para o mermaid na forma "vertice1 --- vertice2\n" e  "vertice1 --- |distancia| vertice2\n", possui um IF extra para impedir que seja duplicado a inserção de vertice1-vertice2 e vertice2-vertice1		 *
		 * >Roda o algoritmo de Dijkstra para esses vertices e arestas, com o parametro do vertice inicial vertIniName.
		 * >Coloca o menor caminho em uma String txt que é dado como resposta
		 * Adiciona informações da rota no String do Mermaid  mudando a cor das caixas
		 * Coloca essas strings de Mermaid em variaveis globais
		 * @param vertIniName Nome do ponto de interesse de Inicio
		 * @param vertFimName Nome do ponto de interesse de Fim
		 * @return String contendo um "array" de nomes dos pontos de interesse com o menor caminho entre inicio-fim
		 * @throws InterruptedException
		 */
		public String Dijkm(String vertIniName,String vertFimName) throws InterruptedException {

			//Source: https://gist.github.com/artlovan/a07f29e16ab725f8077157de7abdf125
			String[][] strPontosDeInteresse=this.getPontosDeInteresse();
			StringBuilder graph=new StringBuilder("graph TD\n");
			StringBuilder graph2=new StringBuilder("graph TD\n");
			Hashtable<String, Vertex> g=new Hashtable<String, Vertex>();
			String[] nomes=new String[strPontosDeInteresse.length];
			//Cria todos vertices
			for(int i=0;i<strPontosDeInteresse.length;i++) {
				if(strPontosDeInteresse[i][0]==null) {
					break;
				}
				Vertex v= new Vertex(strPontosDeInteresse[i][0]);
				nomes[i]=strPontosDeInteresse[i][0];
				System.out.println("\n\n"+strPontosDeInteresse[i][0]);
				//Adiciona V no indice que tem o nome do ponto de interesse[i]
				g.put(strPontosDeInteresse[i][0],v);
			}
			//Adiciona as conexões entre vertices
			 for(int i=0;i<nomes.length;i++) {
				 if(nomes[i]==null) {
					 break;
				 }
				 String[][] conectaCom=this.getConexoesPontosDeInteresse(g.get(nomes[i]).getName());
				 if(conectaCom==null) {
					 //Caso nao tenha, entao continua pro proximo
					 continue;
				 }
				 else {
					 for(int j=0;j<conectaCom.length;j++) {
						 try {
						 Vertex m=g.get(conectaCom[j][0]);
						 //graph.append(m.getName()+" --> |"+Float.parseFloat(conectaCom[j][1])+"| "+g.get(nomes[i]).getName()+"\n");
						 //Se nao tiver o inverso, adiciona
						 if(!graph2.toString().contains(g.get(nomes[i]).getName()+" --- "+m.getName())) {
						 graph2.append(m.getName()+" --- "+g.get(nomes[i]).getName()+"\n");
						 graph.append(m.getName()+" --- |"+Float.parseFloat(conectaCom[j][1])+"| "+g.get(nomes[i]).getName()+"\n");
						 }
						 System.out.print(m.getName());
						g.get(nomes[i]).addNeighbour(new Edge(Float.parseFloat(conectaCom[j][1]),g.get(nomes[i]),g.get(conectaCom[j][0])));
						 }
						 catch(Exception E) {
							 continue;
						 }
					 }
				 }
				 
				
				 
			 }
			 Dijkstra dijkstra = new Dijkstra();
			 dijkstra.computePath(g.get(vertIniName));
		
		        String txt=dijkstra.getShortestPathTo(g.get(vertFimName)).toString();
		        StringBuilder sb=new StringBuilder();

		        sb.append(txt);        
		        //Geração de codigo pro mermaid:
		        String result2= txt.replace("[", "");
		        result2= result2.replace("]", "");
		        String[] locs=result2.split(",");
		        for(int x=0;x<locs.length;x++) {
		        	if(x==0) {
		        		graph.append("style "+locs[x].trim()+" fill:#42692F \n");	
		        	}
		        	else if(x==locs.length-1) {
		        		graph.append("style "+locs[x].trim()+" fill:#65FF00 \n");	
		        	}
		        	else {
		        	graph.append("style "+locs[x].trim()+" fill:#BD96D0 \n");
		        	}
		        }
		        result2= result2.replace(",", " --> ");
		        this.globGraph=(graph.toString());
		        this.globNonDirGraph=(graph2.toString());
		        this.globDij=("graph TD\n"+result2+"\n \n \n \n");
						
			return sb.toString();
		}
		/**
		 * {@return Gera HTML contendo um Mermaid gerado pelo Dijkstra, embora não considera rota}
		 * @throws InterruptedException
		 */
		
		@GetMapping("/grafo")
		public String graf() throws InterruptedException {
			
	        Dijkm("Entrada","Saida");
	        String md=this.globNonDirGraph;
	        md=md.replaceAll("(\r\n|\n)", "\n");
	        StringBuilder sb=new StringBuilder();
	        sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
	        +"<div class=\"mermaid\"> ");
	        sb.append(md);        
	        sb.append(";</div>"
	        		+ "        <script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>\r\n"
					+ "        <script>\r\n"
					+ "            mermaid.initialize({ startOnLoad: true });\r\n"
					+ "        </script>\r\n"
	        		+ " </body>\r\n"
				+ "</html>\r\n"
				+ "\r\n"
				+ "");
					
	        System.out.print(sb.toString());
		return sb.toString();
	}
		
		
	
		/**
		 * {@return Gera HTML contendo um Mermaid gerado pelo Dijkstra,considerando rota, então é o grafo colorido}
		 * @throws InterruptedException
		 */
		@GetMapping("/grafoRota")
		public String graf2(@RequestParam(value = "inicio", defaultValue = "Entrada") String vertIniName,@RequestParam(value = "fim", defaultValue = "Saida") String vertFimName) throws InterruptedException {
			 Dijkm(vertIniName,vertFimName);
		        String md=this.globDij;
		        md=md.replaceAll("(\r\n|\n)", "\n");
		        StringBuilder sb=new StringBuilder();
		        sb.append("<!DOCTYPE html>\r\n"
						+ "<html>\r\n"
						+ "<body>\r\n"
		        +"<div class=\"mermaid\"> ");
		        sb.append(md);        
		        sb.append(";</div>"
		        		+ "        <script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>\r\n"
						+ "        <script>\r\n"
						+ "            mermaid.initialize({ startOnLoad: true });\r\n"
						+ "        </script>\r\n"
		        		+ " </body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
						
		        System.out.print(sb.toString());
			return sb.toString();
	}
		/**
		 * {@return Gera HTML contendo um Mermaid gerado pelo Dijkstra,considerando rota, então é o grafo colorido e também possui as informações de distancia}
		 * @throws InterruptedException
		 */
		
		@GetMapping("/grafoComPeso")
		public String graf3(@RequestParam(value = "inicio", defaultValue = "Entrada") String vertIniName,@RequestParam(value = "fim", defaultValue = "Saida") String vertFimName) throws InterruptedException {
			
	        Dijkm(vertIniName,vertFimName);
	        String md=this.globGraph;
	        md=md.replaceAll("(\r\n|\n)", "\n");
	        StringBuilder sb=new StringBuilder();
	        sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
	        +"<div class=\"mermaid\"> ");
	        sb.append(md);        
	        sb.append(";</div>"
	        		+ "        <script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>\r\n"
					+ "        <script>\r\n"
					+ "            mermaid.initialize({ startOnLoad: true });\r\n"
					+ "        </script>\r\n"
	        		+ " </body>\r\n"
				+ "</html>\r\n"
				+ "\r\n"
				+ "");
					
	        System.out.print(sb.toString());
		return sb.toString();
	}
		
		/**
		 * Execução void que deleta paredes anteriores e adiciona novas paredes como um spam de inserts iguais ao /criamapa
		 * Desta forma facilita a inserção em massa de dados ao fuseki
		 * Uma melhor forma de que um metodo no futuro pegue essas coordenadas de algum arquivo
		 */
		@GetMapping("/EntopeMapa")
		public void EntopeMapa() {
			String nomeParede="Parede";
			String nomemapa="mapaEach";
			//String posXfim="2";
			//String posYfim="2";
			//String posXini="2";
			//String posYini="2";
			int startParede=10;
			String[] posXini={"10","60","90","140","190","240","10","10","90","140","90","90","190"};
			String[] posXfim={"10","60","90","140","190","240","240","240","90","140","140","140","240"};
			String[] posYini={"10","10","10","10","10","10","10","450","280","280","280","250","250"};
			String[] posYfim={"450","450","250","250","250","450","10","450","450","450","280","250","250"};
			for(int i=0;i<posXini.length;i++) {
				String numParede=(startParede+i)+"";
				//Deleta coordenada antiga
				String del="DELETE WHERE {"
						 + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeParede+""+numParede+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaXInicio> ?A; \r\n"
						 +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaXFim> ?B;\r\n"
		                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaYinicio> ?C;\r\n"
		                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaYFim> ?D.\r\n"	
						+"}";
				this.InsertGenerico(del);
				
				//Insere coordenadas
				
				
				String string="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
						+ "INSERT DATA{	\r\n"
		                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeParede+""+numParede+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaXInicio> "+posXini[i]+"; \r\n"
		                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaXFim> "+posXfim[i]+";\r\n"
		                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaYinicio> "+posYini[i]+";\r\n"
		                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaYFim> "+posYfim[i]+".\r\n"	
		                + "};";
		
				this.InsertGenerico(string);
				//Insere referencia no mapa 
				String string2="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
						+ "INSERT DATA{	\r\n"
		                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomemapa+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#uri> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeParede+""+numParede+">. \r\n"
		                + "};";
				
				this.InsertGenerico(string2);
				
				
			}	
		}
		/**
		 * {@return JSON de pontos de interesse obitidos pelo  metodo getPontosDeInteresse}
		 * @throws InterruptedException
		 */
		
		@GetMapping("/Interesses")
		public PontosDeInteresse[] getInteresses() throws InterruptedException {
			
			
			String[][] strPontosDeInteresse=this.getPontosDeInteresse();
			PontosDeInteresse[] resp=new PontosDeInteresse[strPontosDeInteresse.length];
			for(int i=0;i<resp.length;i++) {
			PontosDeInteresse a=new PontosDeInteresse();
			a.setNomeinteresse(strPontosDeInteresse[i][0]);
			a.setCordX(strPontosDeInteresse[i][1]);
			a.setCordY(strPontosDeInteresse[i][2]);
			resp[i]=a;
			}
			
			return resp;
		}
		/**
		 * Formulario para criação de ponto de interesse
		 * Caso o valor não seja o default "CH0", produz uma string de insert sparql com os valores de variavel nos parametros, então chama a função InsertGenerico que faz o update com essa string passada.
		 * As classes alvos dessa inserção são gml::Point e IndoorNavi::RoutenodeType
		 * @param nomemapa
		 * @param nomeInteresse
		 * @param posXini
		 * @param posYini
		 * @return
		 */
		
		@GetMapping("/criaPontosDeInteresse")
		public String criaInteresse(@RequestParam(value = "lugar", defaultValue = "mapaEach") String nomemapa,@RequestParam(value = "nomeInteresse", defaultValue = "CH0") String nomeInteresse,  @RequestParam(value = "posXini", defaultValue = "0") String posXini,  @RequestParam(value = "posYini", defaultValue = "0") String posYini) {
			StringBuilder sb=new StringBuilder();
			//int[][][] intmap=getMapa(nomemapa);
			
			
			sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
					+ "\r\n"
					+ "<form>"   //Fazendo um form que leva a propria pagina
					+ "<input name='nomeInteresse' id='nomeInteresse' value='"+nomeInteresse+"'><br>"
					+ "<input name='posXini' id='posXini' value='"+posXini+"'><br>"
					+ "<input name='posYini' id='posYini' value='"+posYini+"'><br>"					
					+ "<button>Cria ponto de interesse</button><br>"
					+ "</form>"
					+ "</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
			if(!nomeInteresse.equals("CH0")){
			//Adiciona GML Point(GML pos foi substituido pra posX e posY na adaptação de XSD para TTL
			String string="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
					+ "INSERT DATA{	\r\n"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeInteresse+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#gml:posX> "+posXini+"; \r\n"
	                +"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#gml:posY> "+posYini+"."	
	                + "};";
			this.InsertGenerico(string);
			//RDN:RoutenodeType
			String string2="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
					+ "INSERT DATA{	\r\n"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#rdn"+nomeInteresse+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#gml:point> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeInteresse+">. \r\n"
	                + "};";
			this.InsertGenerico(string2);
			}
			
			
			return sb.toString();
		}
		
		/**
		 * Formulario para criação de conexões de pontos de interesse
		 * Os parametros são que um ponto se conecta com outro ponto e qual direção isso é feito
		 * Caso o valor não seja o default "CH0", produz uma string de insert sparql com os valores de variavel nos parametros, então chama a função InsertGenerico que faz o update com essa string passada.
		 * A classe alvo deste insert é IndoorNavi::RouteSegmentType. Foi adicionada o predicado direct aqui, deve ser tratado apropriadamente no futuro
		 * @param nomemapa
		 * @param nomeInteresse
		 * @param nomeConecta
		 * @param direcao
		 * @param distancia
		 * @return
		 */
		
		@GetMapping("/ConectaPontoDeInteresse")
		public String ConectaPontoDeInteresse(@RequestParam(value = "lugar", defaultValue = "mapaEach") String nomemapa,@RequestParam(value = "nomeInteresse", defaultValue = "CH0") String nomeInteresse,@RequestParam(value = "nomeConecta", defaultValue = "CH1") String nomeConecta,@RequestParam(value = "direcao", defaultValue = "norte") String direcao,@RequestParam(value = "distancia", defaultValue = "0") String distancia ) {
			StringBuilder sb=new StringBuilder();
			//int[][][] intmap=getMapa(nomemapa);
			
			
			sb.append("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<body>\r\n"
					+ "\r\n"
					+ "<form>"   //Fazendo um form que leva a propria pagina
					+ "<input name='nomeInteresse' id='nomeInteresse' value='"+nomeInteresse+"'> Conecta com: "
					+ "<input name='nomeConecta' id='nomeConecta' value='"+nomeConecta+"'><br>"
					+ "direcao: <input name='direcao' id='direcao' value='"+direcao+"'><br>"					
					+ "distancia: <input name='distancia' id='distancia' value='"+distancia+"'><br>"	
					+ "<button>Cria conexao</button><br>"
					+ "</form>"
					+ "</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
			if(!nomeInteresse.equals("CH0")){
			//RST:RouteSegmentType Conecta
			String string="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
					+ "INSERT DATA{	\r\n"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nomeInteresse+""+nomeConecta+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#indoorgml:RouteNodeType> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#rdn"+nomeInteresse+">; \r\n"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#indoorgml:RouteNodeType> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#rdn"+nomeConecta+">;"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct> \""+direcao+"\";"
	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#weight> \""+distancia+"\"^^xsd:double."
	                + "};";
			
			this.InsertGenerico(string);
			}
			
			return sb.toString();
		}
		
		/**
		 * Faz a conexão com o fuseki e utiliza a classe UpdateRequest para passar uma String SPARQL 
		 * @param string A string que deve ser passada para o SPARQL endpoint de update do Fuseki
		 */
		
		public void InsertGenerico(String string) {			
			 RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/update");
		        try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

		        	/*String do insert exemplo:
		        	String string="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
		        					+ "INSERT DATA{	\r\n"
		        	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Usuario"+rA+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> "+rA+" \r\n"
		        	                + "};";
*/
		            try{
		                UpdateRequest updateDevice = UpdateFactory.create(string);
		                conn.update(updateDevice);
		                System.out.println("Insert feito com sucesso \n"+string);
		            }catch(Exception e){
		                System.out.println("Erro no insert");
		            }

		        }catch (Exception e) {
		            System.out.println("Não foi possível instanciar conexão");
		            e.printStackTrace();
		            throw new RuntimeException();
		        }
		
	}
		private String[][] getPontosDeInteresse() throws InterruptedException {
			String[][] genSize=this.genericSearch1("?C", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#gml:point>","?A");
			String[][] resp=new String[genSize.length][3]; //String array[index] 0-: Nome 1:-Pos X, 2:Pos Y
			String uriCoord;
			String[] uriCoords=new String[2550];
			String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#gml:point> ?C}";
			
			String nomeCoord;
			String[] nomesCoords=new String[genSize.length];
			Query query=QueryFactory.create(string);
			int vi=0;
			int maxVi=0;
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
		
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
					QueryExecution qe = conn.query(query);
					
					ResultSet rsService = qe.execSelect();
					
			        do {
				            QuerySolution qs = rsService.next();
				            RDFNode uriRDN = qs.get("A");
				            RDFNode uriCoordRaw = qs.get("C");
				            StringBuilder sb=new StringBuilder();
				            
				            
				          
				            //Remove a uri do nome
				            uriCoord=(uriCoordRaw+"").replace("^^http://www.w3.org/2001/XMLSchema#anyURI","");
				            uriCoord=(uriCoord+"").replace("eger","");
				            sb.append("<");
				            sb.append(uriCoord);
				            sb.append(">"); //Adiciona abre e fecha
				            uriCoord=sb.toString();
				            nomeCoord=uriCoord.replace("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#", "");
				            nomeCoord=nomeCoord.replace(">","");
				            
				            uriCoords[vi]=uriCoord+"";
				            resp[vi][0]=nomeCoord+"";
				            vi++;
			        }while(rsService.hasNext());
			        conn.close();
			}
				            
			          //Faz uma nova query pra cima dessas uri
			maxVi=vi;
			vi=0;   
			while(vi<maxVi) {
				String[][] resultsQuery=this.genericSearchPoints(uriCoords[vi], "?B", "?C");        
				
				//Primeira [PosX]
				            String coordTypeRaw =resultsQuery[0][1];
				            String value = resultsQuery[0][2];
				            //Descibre de que tipo é
				            String coordType=(coordTypeRaw+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#","");
				            int cType=-1;
				            if(coordType.equals("gml:posX")) {
				            	cType=1; 
				            }
				            if(coordType.equals("gml:posY")) {
				            	cType=2; 
				            }
				            
				            
				            //Se nao for nenhum tipo,vai para o proximo e nao faz nada
				            if(cType!=-1) {
				            	//Remove o XSD:Int e XSD:String do valor
					            String keylimpa=(value+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
					            keylimpa=(keylimpa+"").replace("^^http://www.w3.org/2001/XMLSchema#String","");
				            	keylimpa=(keylimpa+"").replace("eger","");
					            
					    	
				            	System.out.println(""+coordType+ " :"+ keylimpa);	
					            resp[vi][cType]=keylimpa;
				            }		
				            
		//Segunda [PosY]
				             coordTypeRaw =resultsQuery[1][1];
				             value = resultsQuery[1][2];
				            //Descibre de que tipo é
				            coordType=(coordTypeRaw+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#","");
				            cType=-1;
				            if(coordType.equals("gml:posX")) {
				            	cType=1; 
				            }
				            if(coordType.equals("gml:posY")) {
				            	cType=2; 
				            }
				            
				            System.out.println(""+coordType+ " "+cType);
				            //Se nao for nenhum tipo,vai para o proximo e nao faz nada
				            if(cType!=-1) {
				            	//Remove o XSD:Int e XSD:String do valor
					            String keylimpa=(value+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
					            keylimpa=(keylimpa+"").replace("^^http://www.w3.org/2001/XMLSchema#String","");
				            	keylimpa=(keylimpa+"").replace("eger","");
					            
					          
				            	
					            resp[vi][cType]=keylimpa;
					            vi++;
				            }			            				        

				            
					}				
				return resp;
			}
		
	

		
		private String UriToURL(String uri) {
			StringBuilder sb=new StringBuilder();
            //Remove a uri do nome
            uri=(uri+"").replace("^^http://www.w3.org/2001/XMLSchema#anyURI","");
            uri=(uri+"").replace("eger","");
            sb.append("<");
            sb.append(uri);
            sb.append(">"); //Adiciona abre e fecha
			return sb.toString();
		}
		
		
		private String CleanRST(String uri, String nomeConecta) {
			StringBuilder sb=new StringBuilder();
            //Remove a uri do nome, RST e qualquer outro NomeConecta junto
            uri=(uri+"").replace("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST","");
            //uri=(uri+"").replace("RST","");
            uri=(uri+"").replace(">","");
            uri=(uri+"").replace(nomeConecta,"");          
			return uri;
		}
		private String CleanXSD(String value) {
			//"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nomeInteresse+""+nomeConecta+">
            
			String keylimpa=(value+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
            keylimpa=(keylimpa+"").replace("^^http://www.w3.org/2001/XMLSchema#String","");
            keylimpa=(keylimpa+"").replace("^^http://www.w3.org/2001/XMLSchema#double","");
        	keylimpa=(keylimpa+"").replace("eger","");
        	keylimpa=(keylimpa+"").replace("e0","");
            return keylimpa;
		}
		
		private String[][] getConexoesPontosDeInteresse(String nomeInteresse) throws InterruptedException {
						
				String[][] resultsQuery=this.genericSearch2("?A", "?B", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#rdn"+nomeInteresse+">");
				if(resultsQuery==null) {
					return null;
				}
				String[][] resp=new String[resultsQuery.length][3]; //String array[index] 0-: NomeConectado 1-:Distancia
				//Pega todas strings RDN e busca novamente pelo peso
				for(int in=0;in<resultsQuery.length;in++) {
				resp[in][0]=CleanRST(resultsQuery[in][0],nomeInteresse);
				resp[in][0]=resp[in][0].replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST", "");
				String[][] rq2=this.genericSearch1(UriToURL(resultsQuery[in][0]), "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#weight>", "?A");
				resp[in][1]=CleanXSD(rq2[0][0]);
				String[][] rq3=this.genericSearch1(UriToURL(resultsQuery[in][0]), "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct>", "?A");
				resp[in][2]=CleanXSD(rq3[0][0]);
				}

				return resp;
			}

		
		
		private void InsereNum(int rA)
		{
			 RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/update");
		        try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

		        	//String do insert
		        	String string="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
		        					+ "INSERT DATA{	\r\n"
		        	                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Usuario"+rA+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> "+rA+" \r\n"
		        	                + "};";

		            try{
		                UpdateRequest updateDevice = UpdateFactory.create(string);
		                conn.update(updateDevice);
		            }catch(Exception e){
		                System.out.println("Erro no insert");
		            }

		        }catch (Exception e) {
		            System.out.println("Não foi possível instanciar conexão");
		            e.printStackTrace();
		            throw new RuntimeException();
		        }
		}
		
		
		
		public Rota[] getrota(String nomeMapa) {
			Rota[] route=new Rota[200];
			int[][] pontosDoGrafo=new int[50][2];
			int count=0;
			String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:coordenadaX> ?C}";
			Query query=QueryFactory.create(string);
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
		
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
			QueryExecution qe = conn.query(query);
			
			ResultSet rsService = qe.execSelect();
			
	        do {
	            QuerySolution qs = rsService.next();
	           
	            RDFNode key = qs.get("C");
	            //Remove a uri do nome
	           
	            
	            //Remove o XSD:INT do valor
	            String keylimpa=(key+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
	            
	            keylimpa=(keylimpa+"").replace("eger","");
	            pontosDoGrafo[count][0]=Integer.parseInt(keylimpa);
	            count++;
	            
	        } while (rsService.hasNext());
	        
			}
			
			return route;
		}
		
		
		
		public int[][][] getMapa(String nomeMapa) {
			//Formato:
			//[Tipo do grafo][Num Gafo][Valor em Tipo cordenada]
			//Tipo grafo: 0-: paredes, 1:obstaculos, 2: ponto de andar
			//Num grafo: Qual o index desse ponto
			//Tipo de coordenada: 0:coordenada x inicio, 1: coordenada y inicio, 2: coordenada x fim, 3: coordenada y fim
			int[][][] resp=new int[3][255][4];
		
			String uriParede;
			String string="SELECT ?C {<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeMapa +"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#uri> ?C}";
			Query query=QueryFactory.create(string);
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
		
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
					QueryExecution qe = conn.query(query);
					
					ResultSet rsService = qe.execSelect();
					
			        do {
				            QuerySolution qs = rsService.next();
				            RDFNode uriParedeRaw = qs.get("C");
				            StringBuilder sb=new StringBuilder();				            
				            
				            //Remove a uri do nome
				            uriParede=(uriParedeRaw+"").replace("^^http://www.w3.org/2001/XMLSchema#anyURI","");
				            uriParede=(uriParede+"").replace("eger","");
				            sb.append("<");
				            sb.append(uriParede);
				            sb.append(">"); //Adiciona abre e fecha
				            uriParede=sb.toString();
				            String NumUriParede="";
				            if(uriParede.toLowerCase().contains("indoorplaning#parede")) {
				            NumUriParede=(uriParede+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Parede","");
				            NumUriParede=(NumUriParede+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#parede","");
				            NumUriParede=(NumUriParede+"").replace(">","");
				            NumUriParede=(NumUriParede+"").replace("<","");
				            }
				            else {
				            	//Não é parede, pode ser um botão
				            	NumUriParede=(uriParede+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Botão","");
					            NumUriParede=(NumUriParede+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#botao","");
					            NumUriParede=(NumUriParede+"").replace(">","");
					            NumUriParede=(NumUriParede+"").replace("<","");
				            	
				            	
				            }
				        
				            int vi=Integer.parseInt(NumUriParede);
				            
				            
				            
			          //Faz uma nova query pra cima dessa uri
				            
						            String string2="SELECT ?B ?C {"+uriParede  +" ?B ?C}";
						            System.out.println(string2);
									Query query2=QueryFactory.create(string2);
									
									RDFConnectionRemoteBuilder builder2 = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
									
									
									try(RDFConnectionFuseki conn2 = (RDFConnectionFuseki) builder2.build()) {
										QueryExecution qe2 = conn2.query(query2);
										
										ResultSet rsService2 = qe2.execSelect();
									
								        do {
								            QuerySolution qs2 = rsService2.next();
								            RDFNode coordTypeRaw = qs2.get("B");
								            RDFNode value = qs2.get("C");
								            //Descibre de que tipo é
								            String coordType=(coordTypeRaw+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#","");
								            int cType=-1;
								            if(coordType.equals("idp:coordenadaXInicio")) {
								            	cType=0; 
								            }
								            if(coordType.equals("idp:coordenadaYinicio")) {
								            	cType=1; 
								            }
								            if(coordType.equals("idp:coordenadaXFim")) {
								            	cType=2; 
								            }
								            if(coordType.equals("idp:coordenadaYFim")) {
								            	cType=3; 
								            }
								            
								            System.out.println(""+coordType+ " "+cType);
								            //Se nao for nenhum tipo,vai para o proximo e nao faz nada
								            if(cType!=-1) {
								            	//Remove o XSD:INT do valor
									            String keylimpa=(value+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
								            	keylimpa=(keylimpa+"").replace("eger","");
									            
									            
									            resp[0][vi][cType]=Integer.parseInt(keylimpa);
								            }
								            
								            
								        } while (rsService2.hasNext());
								        conn2.close();    
									}
									
				            
				        } while (rsService.hasNext());
			        conn.close();
			        
			}
			
			
			return resp;
		}
		public String[][] genericSearch1(String one,String two, String three) {
			
			//Volta apenas o A em [0]
			int count=0;
			String string="SELECT DISTINCT ?A {"+one+" "+two+" "+ three+";}";
			System.out.print("\n\nGeneric Search tp is:"+string);
			Query query=QueryFactory.create(string);
			
			
			 
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
			ArrayList<String[]> resp = new ArrayList<String[]>();
			ResultSet rsService;
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
				try {
			QueryExecution qe = conn.query(query);
			qe.setTimeout(400);
			rsService = qe.execSelect();
			if(!rsService.hasNext()) {
				return null;
			}
			}
			catch(Exception ex) {
				System.out.print(ex.getMessage());
				return null;
			}
			
			
	        do {
	            QuerySolution qs = rsService.next();     
	            RDFNode respA = qs.get("A");
	            String[] V= new String[3];
	            V[0]=respA+"";
	            count++;
				resp.add(V);
	            
	            
	        } while (rsService.hasNext());
	        conn.close();
			}
			String[][] moldResp=new String[count][3];
			//Coloca os resultados em um vetor
			for(int i=0;i<moldResp.length;i++) {
			moldResp[i]=resp.get(i);
			}
			
			return moldResp;
			
		}
		public String[][] genericSearch2(String one,String two, String three) {
			int count=0;
			//Volta apenas o A e B em [0] e [1]
			String string="SELECT DISTINCT ?A ?B {"+one+" "+two+" "+ three+";}";
			//String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> ?C}";
			Query query=QueryFactory.create(string);
			System.out.print("\n\nGeneric Search tp is:"+string);
			
			 
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
			ArrayList<String[]> resp = new ArrayList<String[]>();
			
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
			QueryExecution qe = conn.query(query);
			qe.setTimeout(400);
			ResultSet rsService = qe.execSelect();
			if(!rsService.hasNext()) {
				return null;
			}
	        do {
	            QuerySolution qs = rsService.next();     
	            RDFNode respA = qs.get("A");
	            RDFNode respB = qs.get("B");
	            String[] V= new String[3];
	            V[0]=respA+"";
	            V[1]=respB+"";
	            count++;
				resp.add(V);
	            
	            
	        } while (rsService.hasNext());
	        conn.close();
			}
			String[][] moldResp=new String[count][3];
			//Coloca os resultados em um vetor
			for(int i=0;i<moldResp.length;i++) {
			moldResp[i]=resp.get(i);
			}
			
			return moldResp;
			
		}
		public String[][] genericSearchPoints(String one,String two, String three) {
			int count=0;
			String string="SELECT ?B ?C {"+one+" "+two+" "+ three+";}";

			Query query=QueryFactory.create(string);
			System.out.print("\n\nGeneric Search tp is:"+string);
			
			 
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
			ArrayList<String[]> resp = new ArrayList<String[]>();
			String[][] moldResp=new String[2][3];
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
			QueryExecution qe = conn.query(query);
			qe.setTimeout(400);
			ResultSet rsService = qe.execSelect();
			
	        do {
	            QuerySolution qs = rsService.next();     
	            RDFNode respB = qs.get("B");
	            RDFNode respC = qs.get("C");
	            String[] V= new String[3];
	            V[1]=respB+"";
	            V[2]=respC+"";
	            count++;
				resp.add(V);
	            
	            
	        } while (rsService.hasNext());
	        conn.close();
			}
			moldResp[0]=resp.get(0);
			moldResp[1]=resp.get(1);
			
			return moldResp;
		}
		
		
		
		public Object[] getChaves() {
			int count=0;
			String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> ?C}";
			Query query=QueryFactory.create(string);
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
			ArrayList<Person> resp = new ArrayList<Person>();
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
			QueryExecution qe = conn.query(query);
			
			ResultSet rsService = qe.execSelect();
			
	        do {
	            QuerySolution qs = rsService.next();
	            RDFNode nome = qs.get("A");
	            RDFNode key = qs.get("C");
	            //Remove a uri do nome
	            String nomelimpa=(nome+"").replace("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#","");
	            
	            //Remove o XSD:INT do valor
	            String keylimpa=(key+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
	            
	            keylimpa=(keylimpa+"").replace("eger","");
	            Person p=new Person();
	            count++;
	            p.setId(count+"");	            
				p.setKey(keylimpa);
				p.setNome(nomelimpa);
				resp.add(p);
	            
	            
	        } while (rsService.hasNext());
	        conn.close();
			}
			return  (Object[]) resp.toArray();
		}
	
}
		

