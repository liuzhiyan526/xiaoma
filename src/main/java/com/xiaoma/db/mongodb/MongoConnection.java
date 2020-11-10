package com.xiaoma.db.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.xiaoma.util.ConfigFileUtil;
import com.xiaoma.util.StringUtil;

public class MongoConnection {
	private MongoClient[] mongoClientAry;
    private MongoDatabase[] dbAry;

    private static class SingletonMongoConnection {
        private static final MongoConnection instance = new MongoConnection();
    }

    public static MongoConnection getInstance() {
        return SingletonMongoConnection.instance;
    }

    private MongoConnection() {
    }
    
    public void init() throws UnknownHostException, MongoException {
    	mongoClientAry = new MongoClient[1];
    	dbAry = new MongoDatabase[1];
    	
        MongoClientOptions.Builder build = new MongoClientOptions.Builder();
        build.connectionsPerHost(50);
        build.threadsAllowedToBlockForConnectionMultiplier(50);
        build.maxWaitTime(1000 * 60 * 2);
        build.connectTimeout(1000 * 30);
        build.socketTimeout(1000 * 30);
        
        createConnection(build, 0, ConfigFileUtil.getStringArray("mongodb.host"), 
        		ConfigFileUtil.getString("mongodb.dbname"),
        		ConfigFileUtil.getString("mongodb.dbuser"),
        		ConfigFileUtil.getString("mongodb.dbpassword"));
    }
    
    public void init(String hosts, String dbname, String dbuser, String dbpassword) throws UnknownHostException, MongoException {
    	mongoClientAry = new MongoClient[1];
    	dbAry = new MongoDatabase[1];
    	
        MongoClientOptions.Builder build = new MongoClientOptions.Builder();
        build.connectionsPerHost(50);
        build.threadsAllowedToBlockForConnectionMultiplier(50);
        build.maxWaitTime(1000 * 60 * 2);
        build.connectTimeout(1000 * 30);
        build.socketTimeout(1000 * 30);
        
        createConnection(build, 0, hosts.split(","), dbname, dbuser, dbpassword);
    }
    
    public void init(int dbNum, String[] hosts, String[] dbname, String[] dbuser, String[] dbpassword) throws UnknownHostException, MongoException {
    	mongoClientAry = new MongoClient[dbNum];
    	dbAry = new MongoDatabase[dbNum];
    	MongoClientOptions.Builder build = new MongoClientOptions.Builder();
        build.connectionsPerHost(50);
        build.threadsAllowedToBlockForConnectionMultiplier(50);
        build.maxWaitTime(1000 * 60 * 2);
        build.connectTimeout(1000 * 30);
        build.socketTimeout(1000 * 30);
        
        for (int i=0;i<dbNum;i++){
        	createConnection(build, 0, hosts[i].split(","), dbname[i], dbuser[i], dbpassword[i]);
        }
    }
    
    
    public MongoCollection<Document> getCollection(String collectionName) {
        return dbAry[0].getCollection(collectionName);
    }

    public MongoCollection<Document> getCollection(int dbIndex, String collectionName) {
        return dbAry[dbIndex].getCollection(collectionName);
    }
    
    public Document getMongoVersion(int dbIndex) {
    	Document command = new Document("buildInfo",1);
        return dbAry[dbIndex].runCommand(command);
    }
    
    private void createConnection(MongoClientOptions.Builder build, int dbIndex, String[] hosts, String dbname,String dbuser,String dbpassword) {
    	List<ServerAddress> listhost = new ArrayList<ServerAddress>();
    	if (!StringUtil.isEmpty(hosts)) {
    		for (String host : hosts) {
    			listhost.add(new ServerAddress(host));
    		}
    	}
        MongoCredential credential = MongoCredential.createScramSha1Credential(dbuser, dbname, dbpassword.toCharArray());
        mongoClientAry[dbIndex] = new MongoClient(listhost, credential, build.build());
        dbAry[dbIndex] = mongoClientAry[dbIndex].getDatabase(dbname);
        if (listhost!=null&&listhost.size()>1){
            dbAry[dbIndex].withReadPreference(ReadPreference.secondary());
        }
        dbAry[dbIndex].listCollectionNames();
    }
}
