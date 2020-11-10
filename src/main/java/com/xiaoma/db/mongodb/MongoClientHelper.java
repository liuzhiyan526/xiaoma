package com.xiaoma.db.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;

public class MongoClientHelper {

	public static boolean insert(String collectionName, BasicDBObject jsonObject) {
		return insert(collectionName, jsonObject, false);
	}
	
	public static boolean insert(String collectionName, BasicDBObject jsonObject, boolean objectIdToId) {
		if (objectIdToId && jsonObject.containsField("id")){
			jsonObject.remove("id");
    	}
		try {
			MongoConnection.getInstance().getCollection(collectionName).insertOne(new Document(jsonObject));
        } catch (Throwable e) {
            return false;
        }
		return true;
	}
	
	public static boolean updateOne(String collectionName, BasicDBObject query, BasicDBObject jsonObject) {
    	return updateOne(collectionName, query, jsonObject, false);
    }
	
    public static boolean updateOne(String collectionName, BasicDBObject query, BasicDBObject jsonObject, boolean objectIdToId) {
    	if (objectIdToId && query.containsField("id")){
    		query.put("_id", new ObjectId(query.getString("id")));
    		query.remove("id");
    		if (jsonObject.containsField("id")){
    			jsonObject.remove("id");
    		}
    	}
        try {
        	MongoConnection.getInstance().getCollection(collectionName).updateOne(query, new BasicDBObject("$set",jsonObject));
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
    
    public static boolean updateMany(String collectionName, BasicDBObject query, BasicDBObject jsonObject) {
    	return updateMany(collectionName, query, jsonObject, false);
    }
    
    public static boolean updateMany(String collectionName, BasicDBObject query, BasicDBObject jsonObject, boolean objectIdToId) {
    	if (objectIdToId && query.containsField("id")){
    		query.put("_id", new ObjectId(query.getString("id")));
    		query.remove("id");
    		if (jsonObject.containsField("id")){
    			jsonObject.remove("id");
    		}
    	}
        try {
        	MongoConnection.getInstance().getCollection(collectionName).updateMany(query, new BasicDBObject("$set",jsonObject));
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
    
    public static boolean findOneAndReplace(String collectionName, BasicDBObject query, BasicDBObject jsonObject) {
    	return findOneAndReplace(collectionName, query, jsonObject, false);
    }
    
	public static boolean findOneAndReplace(String collectionName, BasicDBObject query, BasicDBObject jsonObject, boolean objectIdToId) {
		if (objectIdToId && query.containsField("id")){
    		query.put("_id", new ObjectId(query.getString("id")));
    		query.remove("id");
    		if (jsonObject.containsField("id")){
    			jsonObject.remove("id");
    		}
    	}
		try {
			MongoConnection.getInstance().getCollection(collectionName).findOneAndReplace(query, new Document(jsonObject));
        } catch (Throwable e) {
            return false;
        }
		return true;
	}
	
	public static boolean deleteOne(String collectionName, BasicDBObject query) {
    	return deleteOne(collectionName, query, false);
    }
	
    public static boolean deleteOne(String collectionName, BasicDBObject query, boolean objectIdToId) {
    	if (objectIdToId && query.containsField("id")){
    		query.put("_id", new ObjectId(query.getString("id")));
    		query.remove("id");
    	}
        try {
        	MongoConnection.getInstance().getCollection(collectionName).deleteOne(query);
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
    
    public static boolean deleteMany(String collectionName, BasicDBObject query) {
    	return deleteMany(collectionName, query, false);
    }
    
    public static boolean deleteMany(String collectionName, BasicDBObject query, boolean objectIdToId) {
    	if (objectIdToId && query.containsField("id")){
    		query.put("_id", new ObjectId(query.getString("id")));
    		query.remove("id");
    	}
        try {
        	MongoConnection.getInstance().getCollection(collectionName).deleteMany(query);
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
    
    public static long count(String collectionName, BasicDBObject query) {
    	return count(collectionName, query, false);
    }
    
    public static long count(String collectionName, BasicDBObject query, boolean objectIdToId) {
    	if (objectIdToId && query.containsField("id")){
    		query.put("_id", new ObjectId(query.getString("id")));
    		query.remove("id");
    	}
    	long cnt = -1;
        try {
            cnt = MongoConnection.getInstance().getCollection(collectionName).countDocuments(query);
        } catch (Throwable e) {
        }
        return cnt;
    }
    
    public static BasicDBObject findOne(String collectionName, BasicDBObject query) {
    	return findOne(collectionName, query, null, false);
    }
    
    public static BasicDBObject findOne(String collectionName, BasicDBObject query, BasicDBObject proj) {
    	return findOne(collectionName, query, proj, null, false);
    }
    
    public static BasicDBObject findOne(String collectionName, BasicDBObject query, boolean objectIdToId) {
    	return findOne(collectionName, query, null, objectIdToId);
    }
    
    public static BasicDBObject findOne(String collectionName, BasicDBObject query, BasicDBObject proj, boolean objectIdToId) {
    	return findOne(collectionName, query, proj, null, objectIdToId);
    }
    
    public static BasicDBObject findOne(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby, boolean objectIdToId) {
    	BasicDBObject resObj = null;
        try {
            Document doc = null;
            if (orderby != null) {
            	if (proj != null){
            		doc = MongoConnection.getInstance().getCollection(collectionName).find(query).sort(orderby).projection(proj).first();
            	}else{
            		doc = MongoConnection.getInstance().getCollection(collectionName).find(query).sort(orderby).first();
            	}
            } else {
            	if (proj != null){
            		doc = MongoConnection.getInstance().getCollection(collectionName).find(query).projection(proj).first();
            	}else{
            		doc = MongoConnection.getInstance().getCollection(collectionName).find(query).first();
            	}
            }
            if (doc != null) {
                resObj = BasicDBObject.parse(doc.toJson());
                if (objectIdToId && resObj.containsField("_id")){
					resObj.put("id",resObj.getString("_id"));
					resObj.remove("_id");
				}
            }
        } catch (Throwable e) {
            return null;
        }
        return resObj;
    }
    
    public static List<BasicDBObject> findList(String collectionName, BasicDBObject query) {
    	return findList(collectionName, query, null, null, false);
    }
    
    public static List<BasicDBObject> findList(String collectionName, BasicDBObject query, BasicDBObject proj) {
    	return findList(collectionName, query, proj, null, false);
    }
    
    public static List<BasicDBObject> findList(String collectionName, BasicDBObject query, boolean objectIdToId) {
    	return findList(collectionName, query, null, null, objectIdToId);
    }
    
    public static List<BasicDBObject> findList(String collectionName, BasicDBObject query, BasicDBObject proj, boolean objectIdToId) {
    	return findList(collectionName, query, proj, null, objectIdToId);
    }
    
    public static List<BasicDBObject> findList(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby, boolean objectIdToId) {
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();
		MongoCursor<Document> cursor = null;
		try {
			cursor = find(collectionName, query, proj, orderby);
			if (cursor != null) {
				while (cursor.hasNext()) {
					Document doc = cursor.next();
					BasicDBObject resObj = BasicDBObject.parse(doc.toJson());
					if (objectIdToId && resObj.containsField("_id")){
						resObj.put("id",resObj.getString("_id"));
						resObj.remove("_id");
					}
					list.add(resObj);
				}
			}
		} catch (Throwable e) {
		}
		return list;
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject page) {
    	return findListByPage(collectionName, query, null, page.getInt("skip", 0), page.getInt("limit", 20), false);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, int skip, int limit) {
    	return findListByPage(collectionName, query, null, skip, limit, false);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject page) {
    	return findListByPage(collectionName, query, proj, null, page.getInt("skip", 0), page.getInt("limit", 20), false);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, 
    		int skip, int limit) {
    	return findListByPage(collectionName, query, proj, null, skip, limit, false);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby,
    		BasicDBObject page) {
    	return findListByPage(collectionName, query, proj, orderby, page.getInt("skip", 0), page.getInt("limit", 20), false);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby,
            int skip, int limit) {
    	return findListByPage(collectionName, query, proj, orderby, skip, limit, false);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject page, boolean objectIdToId) {
    	return findListByPage(collectionName, query, null, page.getInt("skip", 0), page.getInt("limit", 20), objectIdToId);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, int skip, int limit, boolean objectIdToId) {
    	return findListByPage(collectionName, query, null, skip, limit, objectIdToId);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, 
    		BasicDBObject page, boolean objectIdToId) {
    	return findListByPage(collectionName, query, proj, null, page.getInt("skip", 0), page.getInt("limit", 20), objectIdToId);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, 
    		int skip, int limit, boolean objectIdToId) {
    	return findListByPage(collectionName, query, proj, null, skip, limit, objectIdToId);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby,
    		BasicDBObject page, boolean objectIdToId) {
    	return findListByPage(collectionName, query, proj, orderby, page.getInt("skip", 0), page.getInt("limit", 20), objectIdToId);
    }
    
    public static List<BasicDBObject> findListByPage(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby,
            int skip, int limit, boolean objectIdToId) {
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();
		MongoCursor<Document> cursor = null;
		try {
			cursor = findByPage(collectionName, query, proj, orderby, skip, limit);
			if (cursor != null) {
				while (cursor.hasNext()) {
					Document doc = cursor.next();
					BasicDBObject resObj = BasicDBObject.parse(doc.toJson());
					if (objectIdToId && resObj.containsField("_id")){
						resObj.put("id",resObj.getString("_id"));
						resObj.remove("_id");
					}
					list.add(resObj);
				}
			}
		} catch (Throwable e) {
		}
		return list;
	}
    
    public static MongoCursor<Document> find(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby) {
    	MongoCursor<Document> cursor = null;
    	try {
			if (orderby != null) {
				if (proj != null){
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).sort(orderby)
							.projection(proj).iterator();
				}else{
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).sort(orderby).iterator();
				}
			} else {
				if (proj != null){
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).projection(proj).iterator();
				}else{
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).iterator();
				}
			}
		} catch (Throwable e) {
			
		}
    	return cursor;
    }
    
    public static MongoCursor<Document> findByPage(String collectionName, BasicDBObject query, BasicDBObject proj, BasicDBObject orderby,
            int skip, int limit) {
    	MongoCursor<Document> cursor = null;
    	try {
			if (orderby != null) {
				if (proj != null){
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).sort(orderby)
							.projection(proj).skip(skip).limit(limit).iterator();
				}else{
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).sort(orderby)
							.skip(skip).limit(limit).iterator();
				}
			} else {
				if (proj != null){
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query).projection(proj)
							.skip(skip).limit(limit).iterator();
				}else{
					cursor = MongoConnection.getInstance().getCollection(collectionName).find(query)
							.skip(skip).limit(limit).iterator();
				}
			}
		} catch (Throwable e) {
			
		}
    	return cursor;
    }
}
