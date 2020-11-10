package com.xiaoma.util;

import java.util.List;

import com.mongodb.BasicDBObject;

public class JsonUtil {

	public static BasicDBObject parseJson(String json) {
		BasicDBObject obj = null;
		try {
			obj = BasicDBObject.parse(json);
		} catch(Exception e) {
			//e.printStackTrace();
		}
		return obj;
	}
	
	@SuppressWarnings("rawtypes")
	public static BasicDBObject getJson(BasicDBObject jsonObject, String key) {
		BasicDBObject value = null;
        if (keyExistNotNull(jsonObject, key)) {
        	if (jsonObject instanceof java.util.LinkedHashMap){
        		value = new BasicDBObject((java.util.LinkedHashMap)jsonObject.get(key));
        	}else{
                value = (BasicDBObject)jsonObject.get(key);
        	}
        }
        return value;
    }
	
	@SuppressWarnings("unchecked")
	public static List<BasicDBObject> getListJson(BasicDBObject jsonObject, String key) {
		List<BasicDBObject> value = null;
        if (keyExistNotNull(jsonObject, key)) {
            value = (List<BasicDBObject>)jsonObject.get(key);
        }
        return value;
    }
	
	public static boolean isEmpty(BasicDBObject jsonObject){
		if (jsonObject != null && !jsonObject.isEmpty()){
			return false;
		}
		return true;
	}
	
	public static boolean keyExistNotNull(BasicDBObject jsonObject, String key){
		if (jsonObject != null && jsonObject.containsField(key) && jsonObject.get(key) != null) {
			return true;
		}
		return false;
	}
	
	public static boolean keyExistAndValueNotBlank(BasicDBObject jsonObject, String key){
		if (jsonObject != null && jsonObject.containsField(key) 
				&& jsonObject.get(key) != null && !"".equals(jsonObject.getString(key))) {
			return true;
		}
		return false;
	}
}
