package tools;

import com.mongodb.*;
import java.util.Arrays;
import play.Configuration;
import uk.co.panaxiom.playjongo.MongoClientFactory;

public class MongoFactory extends MongoClientFactory {
    private Configuration config;
    public MongoFactory(Configuration config) {
    	super(config);
        this.config = config;
    }

    public MongoClient createClient() throws Exception {
        MongoClientOptions options = MongoClientOptions.builder()
            .connectionsPerHost(100)
            .maxConnectionIdleTime(60000)
            .build();

        return new MongoClient(Arrays.asList(
            new ServerAddress("localhost", 27017),
            new ServerAddress("localhost", 27018),
            new ServerAddress("localhost", 27019)),
            options);
    }

    public String getDBName() {
        return config.getString("myappconfig.dbname");
    }

}