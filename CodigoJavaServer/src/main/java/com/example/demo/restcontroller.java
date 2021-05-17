package com.example.demo;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.*;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
		
		public  String getFeaturesOfInterest() {
	        return getfuseki() + "SELECT ?uri ?hasProperty ?type WHERE{\n" +
	                "  ?uri a ?subClass;\n" +
	                "  ssn:hasProperty ?hasProperty ;\n" +
	                "  rdf:type ?type .\n" +
	                "  ?subClass rdfs:subClassOf sosa:FeatureOfInterest\n" +
	                "  FILTER(?type != owl:NamedIndividual)\n" +
	                "}";
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
		

