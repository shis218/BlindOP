package com.example.demo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.*;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class restcontroller {

		private static final String template = "Hello, %s!";
		private final AtomicLong counter = new AtomicLong();
		@GetMapping("/greeting")
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
		
		
		
		/*PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix owl: <http://www.w3.org/2002/07/owl#>



INSERT DATA{	
<https://blindop.herokuapp.com/indoorplaning#Usuario2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://blindop.herokuapp.com/indoorplaning#idp::person>
};

INSERT DATA{ 
<https://blindop.herokuapp.com/indoorplaning#Usuario2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> owl:NamedIndividual
};


INSERT DATA{	
<https://blindop.herokuapp.com/indoorplaning#Usuario2> <https://blindop.herokuapp.com/indoorplaning#idp:key> "29"^^xsd:int
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
		
		public Object[] getChaves() {
			int count=0;
			String string="SELECT ?A ?C {?A <https://blindop.herokuapp.com/indoorplaning#idp:key> ?C}";
			Query query=QueryFactory.create(string);
			
			RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination("https://blindop.herokuapp.com/indoorplaning/sparql");
			
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
	            String nomelimpa=(nome+"").replace("https://blindop.herokuapp.com/indoorplaning#","");
	            
	            //Remove o XSD:INT do valor
	            String keylimpa=(key+"").replace("^^http://www.w3.org/2001/XMLSchema#int","");
	            Person p=new Person();
	            count++;
	            p.setId(count+"");	            
				p.setKey(keylimpa);
				p.setNome(nomelimpa);
				resp.add(p);
	            
	            
	        } while (rsService.hasNext());
	        
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
		

