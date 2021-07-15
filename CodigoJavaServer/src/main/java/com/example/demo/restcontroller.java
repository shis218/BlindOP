package com.example.demo;
import java.util.ArrayList;
import java.util.HashMap;
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
		public Rota[] rota(@RequestParam(value = "lugar", defaultValue = "mapaEach") String mapa,@RequestParam(value = "inicio", defaultValue = "portao1") String init,@RequestParam(value = "fim", defaultValue = "sala211") String end) {
			
			Rota[] rts=new Rota[10];
			Rota[] rtm=getrota(mapa);
			for(int i=0;i<10;i++) {
				Rota r=new Rota();
				if(i%2==0) {
					r.setDirecao("Cima");
					r.setMetros(2);
					r.setNumeroSequencia(i);
					r.setPassos(16);
				}
				else {
					r.setDirecao("Direita");
					r.setMetros(5);
					r.setNumeroSequencia(i);
					r.setPassos(4);
				}
				rts[i]=r;
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
			for(int i=0;i<255;i++) {
					if(!(strPontosDeInteresse[i][0]==null)) {
						sb.append(strPontosDeInteresse[i][0]+"<br> \n");
				//		sb.append(strPontosDeInteresse[i][1]+"<br> \n");
				//		sb.append(strPontosDeInteresse[i][2]+"<br> \n");
					}
				}
					sb.append("<br><canvas id=\"myCanvas\" width=\"1500\" height=\"1500\"\r\n"
					+ "style=\"border:1px solid #c3c3c3;\">\r\n"
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
			}
			
			
			
					sb.append("</script>\r\n"
					+ "\r\n"
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
		
		
		@GetMapping("/EntopeMapa")
		public String EntopeMapa(@RequestParam(value = "lugar", defaultValue = "mapaEach") String nomemapa,@RequestParam(value = "nomeParede", defaultValue = "Parede0") String nomeParede,  @RequestParam(value = "posXini", defaultValue = "0") String posXini,  @RequestParam(value = "posXfim", defaultValue = "0") String posXfim,  @RequestParam(value = "posYini", defaultValue = "0") String posYini,  @RequestParam(value = "posYfim", defaultValue = "0") String posYfim) {
			StringBuilder sb=new StringBuilder();
			//int[][][] intmap=getMapa(nomemapa);		
			
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
			
			
			return sb.toString();
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
					+ "<button>Cria conexão</button><br>"
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
					+ "<button>Cria interesse</button><br>"
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
		
		public String[] genericSearch(String one,String two, String three) {
			int count=0;
			String string="SELECT ?A ?B ?C { "+one+" "+two+" "+ three+" }";
			//String string="SELECT ?A ?C {?A <http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning#idp:key> ?C}";
			Query query=QueryFactory.create(string);
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("http://ip-50-62-81-50.ip.secureserver.net:8080/fuseki/indoorplaning/sparql");
			
			ArrayList<String[]> resp = new ArrayList<String[]>();
			try(RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
			QueryExecution qe = conn.query(query);
			
			ResultSet rsService = qe.execSelect();
			
	        do {
	            QuerySolution qs = rsService.next();
	            //org.apache.jena.rdf.model.Resource nome = qs.getResource("A");           
	            //org.apache.jena.rdf.model.impl.LiteralImpl key= (LiteralImpl) qs.getLiteral("C");
	        //  org.apache.jena.rdf.model.Resource type = qs.getResource("object");
	            RDFNode respA = qs.get("A");
	            RDFNode respB = qs.get("B");
	            RDFNode respC = qs.get("C");
	            String[] V= new String[3];
	            V[0]=respA+"";
	            V[1]=respB+"";
	            V[2]=respC+"";
	            count++;
				resp.add(V);
	            
	            
	        } while (rsService.hasNext());
	        
			}
			return  (String[]) resp.toArray();
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
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("https://blindop.herokuapp.com/person");
			
	
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
		
}
		

