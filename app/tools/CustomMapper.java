package tools;

import org.jongo.Mapper;
import org.jongo.ReflectiveObjectIdUpdater;
import org.jongo.marshall.jackson.JacksonEngine;
import org.jongo.marshall.jackson.JacksonIdFieldSelector;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.configuration.Mapping;
import org.jongo.query.BsonQueryFactory;

import uk.co.panaxiom.playjongo.JongoMapperFactory;

public class CustomMapper implements JongoMapperFactory {

   public CustomMapper(){
	   
   }

	@Override
	public Mapper create() {
		  JacksonEngine jacksonEngine = new JacksonEngine(Mapping.defaultMapping());
           
		  BsonQueryFactory queryFactory = new BsonQueryFactory(jacksonEngine, "#|#");
         
          ReflectiveObjectIdUpdater objectIdUpdater = new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector());
         
          return new JacksonMapper.Builder().withQueryFactory(queryFactory).withObjectIdUpdater(objectIdUpdater).build();
	}
}