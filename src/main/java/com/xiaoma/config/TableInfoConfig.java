package com.xiaoma.config;

import com.mongodb.BasicDBObject;
import com.xiaoma.cache.TableConfigCache;

public class TableInfoConfig {
	
	public static boolean idIsExist(String id){
		return TableConfigCache.idIsExist(id);
	}
	
	public static BasicDBObject getValue(String id){
		return TableConfigCache.getValue(id);
	}
}
