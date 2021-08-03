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



@RestController
public class restcontroller {

		private static final String template = "Hello, %s!";
		private final AtomicLong counter = new AtomicLong();
		@GetMapping("/")
		public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
			name=getfuseki();
			return new Greeting(counter.incrementAndGet(), String.format(template, name));
		}
		@GetMapping("/login")
		public Person person(@RequestParam(value = "name", defaultValue = "World") String nome,@RequestParam(value = "key", defaultValue = "42") String key) {
			Person p= new Person();
			p.setId(counter.incrementAndGet()+"");
			p.setKey(key);
			p.setNome(nome);
			return p;
		}
		@GetMapping("/searchLogins")
		public Object[] searchLogins() {
			Object[] pt= getChaves();

		    return pt;
			
			
			//return p;
		}
		
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
		
		@GetMapping("/rota")
		public Rota[] rota(@RequestParam(value = "lugar", defaultValue = "mapaEach") String mapa,@RequestParam(value = "inicio", defaultValue = "Entrada") String vertIniName,@RequestParam(value = "fim", defaultValue = "Saida") String vertFimName) throws InterruptedException {
			String txt=Dijkm(vertIniName,vertFimName);
			
			System.out.println(txt+"\n\n");
		//	Rota[] rtm=getrota(mapa);
			txt=txt.replace("[", "")	;
			txt=txt.replace("]", "")	;
			String[] pontos= txt.split(",");
			//Tamanho é -1 pois na ultima posição já chegou no destino e não precisa andar mais
			Rota[] rts=new Rota[pontos.length-1];
			
			for(int in=0;in<pontos.length-1;in++) {
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
				r.setPassos((int)Float.parseFloat(rq2[0][0])/10);
				String[][] rq3=this.genericSearch1("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nome1+""+nome2+">", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct>", "?A");
				if(rq3==null) {
					rq3=this.genericSearch1("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nome2+""+nome1+">", "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct>", "?A");
				}
				rq3[0][0]=CleanXSD(rq3[0][0]);
				r.setDirecao(CleanXSD(rq3[0][0]));
				rts[in]=r;
				}
				
			
			return rts;
		}
		@GetMapping("/mapa")
		public String mapear(@RequestParam(value = "lugar", defaultValue = "mapaEach") String nomemapa) throws InterruptedException {
			StringBuilder sb=new StringBuilder();
			int[][][] intmap=getMapa(nomemapa);
			//int[][][] intmap=null;
			String[][] strPontosDeInteresse=this.getPontosDeInteresse();
			System.out.println(strPontosDeInteresse[0][1]);
			
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
					+ "ctx.lineWidth = 0.3;\r\n");
			
			for(int i=0;i<255;i++) {
			
			sb.append("ctx.moveTo("+intmap[0][i][0]+","+ intmap[0][i][1]+");");
			sb.append("ctx.lineTo("+intmap[0][i][2]+","+ intmap[0][i][3]+");");
			sb.append("ctx.stroke();");
			sb.append("ctx.moveTo("+strPontosDeInteresse[i][1]+","+ strPontosDeInteresse[i][2]+");");
		//	int a=Integer.parseInt(strPontosDeInteresse[i][1])+3;
			//int b=Integer.parseInt(strPontosDeInteresse[i][2]);
		//	sb.append("ctx.moveTo("+a+","+ b+");");
		//	sb.append("ctx.stroke();");
			}
			
			
			
					sb.append("</script>\r\n");
					for(int i=0;i<255;i++) {
						if(!(strPontosDeInteresse[i][0]==null)) {
							if(!strPontosDeInteresse[i][0].contains("meio")) {
							sb.append(strPontosDeInteresse[i][0]+"<br> \n");
							sb.append("<button style='position: absolute; top:"+strPontosDeInteresse[i][2]+"px; left:"+strPontosDeInteresse[i][1]+"px;' onclick='myFunction()'>"+strPontosDeInteresse[i][0]+"</button>"); 
					//		sb.append(strPontosDeInteresse[i][1]+"<br> \n");
					//		sb.append(strPontosDeInteresse[i][2]+"<br> \n");
							}
						}
					}
					sb.append("\r\n"
					+ "</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
			return sb.toString();
		}
		
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
		
		public String Dijkm(String vertIniName,String vertFimName) throws InterruptedException {

			//Source: https://gist.github.com/artlovan/a07f29e16ab725f8077157de7abdf125
			String[][] strPontosDeInteresse=this.getPontosDeInteresse();
			
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
			//	 System.out.print("\n"+resp[i][0]+ "   = "+resp[i][1]);
				// Vertex V=g.get(nomes[i]);
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
						 Vertex m=g.get(conectaCom[j][0]);
						g.get(nomes[i]).addNeighbour(new Edge(Float.parseFloat(conectaCom[j][1]),g.get(nomes[i]),g.get(conectaCom[j][0])));
					 }
				 }
				 
			 }
			 Dijkstra dijkstra = new Dijkstra();
			 dijkstra.computePath(g.get(vertIniName));
		
		        String txt=dijkstra.getShortestPathTo(g.get(vertFimName)).toString();
		        StringBuilder sb=new StringBuilder();

		        sb.append(txt);        
		       
						
			return sb.toString();
		}
		
		
		@GetMapping("/dijExemplo")
		public String dijExemplo() {
			
			//Source: https://gist.github.com/artlovan/a07f29e16ab725f8077157de7abdf125
			
			  Vertex v1 = new Vertex("A");
		        Vertex v2 = new Vertex("B");
		        Vertex v3 = new Vertex("C");

		        v1.addNeighbour(new Edge(1, v1, v2));
		        v1.addNeighbour(new Edge(10, v1, v2));

		        v2.addNeighbour(new Edge(1, v2, v3));

		        Dijkstra dijkstra = new Dijkstra();
		        dijkstra.computePath(v1);

		        String txt=dijkstra.getShortestPathTo(v2).toString();
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
		@GetMapping("/String")
		public String Vertices() throws InterruptedException {
			StringBuilder sb=new StringBuilder();
			sb.append(" "
						+ "<html>\r\n"
						+ "<body>\r\n");
		        sb.append(this.getConexoesPontosDeInteresse("Entrada")[0][0]);
		        sb.append(this.getConexoesPontosDeInteresse("Entrada")[0][1]);  
		        sb.append("</body>\r\n"
					+ "</html>\r\n"
					+ "\r\n"
					+ "");
						
			return sb.toString();
		}
		
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
				System.out.print("\n"+string);
				this.InsertGenerico(string);
				//Insere referencia no mapa 
				String string2="PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n prefix owl: <http://www.w3.org/2002/07/owl#> \n"
						+ "INSERT DATA{	\r\n"
		                + "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomemapa+"> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#uri> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#"+nomeParede+""+numParede+">. \r\n"
		                + "};";
				
				this.InsertGenerico(string2);
				
				
			}	
		}
		
		
		
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
		
		public void InsertGenerico(String string) {			
			 RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/update");
		        try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

		        	/*String do insert
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
			String[][] resp=new String[2550][3]; //String array[index] 0-: Nome 1:-Pos X, 2:Pos Y
			String uriCoord;
			String[] uriCoords=new String[2550];
			String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#gml:point> ?C}";
			String nomeCoord;
			String[] nomesCoords=new String[2550];
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
				            
				            
				            System.out.println("\n BareURL "+uriCoordRaw+"\n" );
				            //Remove a uri do nome
				            uriCoord=(uriCoordRaw+"").replace("^^http://www.w3.org/2001/XMLSchema#anyURI","");
				            uriCoord=(uriCoord+"").replace("eger","");
				            sb.append("<");
				            sb.append(uriCoord);
				            sb.append(">"); //Adiciona abre e fecha
				            uriCoord=sb.toString();
				            nomeCoord=uriCoord.replace("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#", "");
				            nomeCoord=nomeCoord.replace(">","");
				            System.out.println("Nome coord:"+nomeCoord);
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
				System.out.println("Fim do problema1");
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
				            
				            System.out.println(""+coordType+ " "+cType);
				            //Se nao for nenhum tipo,vai para o proximo e nao faz nada
				            if(cType!=-1) {
				            	//Remove o XSD:Int e XSD:String do valor
					            String keylimpa=(value+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
					            keylimpa=(keylimpa+"").replace("^^http://www.w3.org/2001/XMLSchema#String","");
				            	keylimpa=(keylimpa+"").replace("eger","");
					            
					            System.out.print(keylimpa); 	
				            	
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
					            
					            System.out.print(keylimpa); 	
				            	
					            resp[vi][cType]=keylimpa;
					            vi++;
				            }			            				        

				            
					}				
				System.out.println("Prox Exec");
				return resp;
			}
		
	

		
		private String UriToURL(String uri) {
			StringBuilder sb=new StringBuilder();
            
            
//            System.out.println("\n BareURL "+uri+"\n" );
            //Remove a uri do nome
            uri=(uri+"").replace("^^http://www.w3.org/2001/XMLSchema#anyURI","");
            uri=(uri+"").replace("eger","");
            sb.append("<");
            sb.append(uri);
            sb.append(">"); //Adiciona abre e fecha
           // System.out.println("\n Transformed URL "+sb.toString()+"\n" );
			return sb.toString();
		}
		
		
		private String CleanRST(String uri, String nomeConecta) {
			StringBuilder sb=new StringBuilder();
			//"<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST"+nomeInteresse+""+nomeConecta+">
            
            System.out.println("\n BareURL "+uri+"\n" );
            //Remove a uri do nome
            uri=(uri+"").replace("<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#RST","");
            uri=(uri+"").replace(">","");
            uri=(uri+"").replace(nomeConecta,"");
            
            System.out.println("\n Transformed URL "+uri+"\n" );
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
				//System.out.println(resultsQuery[in][0]);
				String[][] rq2=this.genericSearch1(UriToURL(resultsQuery[in][0]), "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#weight>", "?A");
				resp[in][1]=CleanXSD(rq2[0][0]);
				String[][] rq3=this.genericSearch1(UriToURL(resultsQuery[in][0]), "<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#direct>", "?A");
				resp[in][2]=CleanXSD(rq3[0][0]);
				}
				/*for(int i=0;i<resp.length;i++) {
					System.out.print("\n"+resp[i][0]+ "   = "+resp[i][1]);
				}*/
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
		
		
		/*PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix owl: <http://www.w3.org/2002/07/owl#>



INSERT DATA{	
<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Usuario2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp::person>
};

INSERT DATA{ 
<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Usuario2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> owl:NamedIndividual
};


INSERT DATA{	
<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Usuario2> <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> "29"^^
};		 
		 */
		public  String getFeaturesOfInterest() {
	        return getfuseki() + "SELECT ?uri ?hasProperty ?type WHERE{\n" +
	                "  ?uri a ?subClass;\n" +
	                "  ssn:hasProperty ?hasProperty ;\n" +
	                "  rdf:type ?type .\n" +
	                "  ?subClass rdfs:subClassOf sosa:FeatureOfInterest\n" +
	                "  FILTER(?type != owl:NamedIndividual)\n" +
	                "}";
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
			/*resp[0][0][0]=172;
			resp[0][0][1]=0;
			resp[0][0][2]=172;
			resp[0][0][3]=25;
			
			resp[0][1][0]=172;
			resp[0][1][1]=25;
			resp[0][1][2]=578;
			resp[0][1][3]=100;
			*/
			//int vi=1;
		//	String uriParede="<http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#Parede"+vi+">";
			
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
				            
				            
				            System.out.println("\n BareURL "+uriParedeRaw+"\n \n \n " );
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
				            System.out.println("\n ProcessedURL "+NumUriParede+"\n \n \n " );
				            int vi=Integer.parseInt(NumUriParede);
				            
				            
				            
			          //Faz uma nova query pra cima dessa uri
				            
						            String string2="SELECT ?B ?C {"+uriParede  +" ?B ?C}";
						            System.out.println(string2);
									Query query2=QueryFactory.create(string2);
									
									RDFConnectionRemoteBuilder builder2 = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
									
									
									try(RDFConnectionFuseki conn2 = (RDFConnectionFuseki) builder2.build()) {
										QueryExecution qe2 = conn2.query(query2);
										
										ResultSet rsService2 = qe2.execSelect();
										 System.out.println("Lendo"+vi);
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
									            
									            System.out.print(keylimpa); 	
								            	
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
			//String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> ?C}";
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
			//String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> ?C}";
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
	            //org.apache.jena.rdf.model.Resource nome = qs.getResource("A");           
	            //org.apache.jena.rdf.model.impl.LiteralImpl key= (LiteralImpl) qs.getLiteral("C");
	        //  org.apache.jena.rdf.model.Resource type = qs.getResource("object");
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
		
		//Metodo de pegar infos do fuseki
		public String getfuseki() {
			StringBuilder resp=new StringBuilder();
			String string="SELECT ?subject ?predicate ?object\n"
					+"WHERE {\n"
					+"?subject ?predicate ?object\n"
					+"}\n"
					+"LIMIT 25";
		
			Query query=QueryFactory.create(string);
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
	
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
			QueryExecution qe = conn.query(query);
			
			ResultSet rsService = qe.execSelect();
	
	        do {
	            QuerySolution qs = rsService.next();
	            org.apache.jena.rdf.model.Resource hasProperty = qs.getResource("subject");
	            org.apache.jena.rdf.model.Resource uri = qs.getResource("predicate");
	        //    org.apache.jena.rdf.model.Resource type = qs.getResource("object");
	
	            resp.append(hasProperty+" "+uri+"\n");
	            
	        } while (rsService.hasNext());
	        
			}
		return resp.toString();	
		}
		
		/*proxima instrução*/
}
		

