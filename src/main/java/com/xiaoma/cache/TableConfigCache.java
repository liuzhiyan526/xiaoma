package com.xiaoma.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.BasicDBObject;
import com.xiaoma.util.ConfigFileUtil;
import com.xiaoma.util.FileUtil;
import com.xiaoma.util.StringUtil;

public class TableConfigCache {
	private static Map<String, BasicDBObject> cacheData = new ConcurrentHashMap<>();

	static{
		init();
	}
	
	public static void init(){
		if (!StringUtil.isEmpty(cacheData)){
			return;
		}
		//加载规则字段
		String fileUrl = ConfigFileUtil.getConfigFileUrl()+"TableConfig.properties";
		System.out.println("fileUrl="+fileUrl);
		List<BasicDBObject> dataList = FileUtil.readreadByLineBasicDBObject(fileUrl);
		
		for (BasicDBObject data : dataList){
			cacheData.put(data.getString("id"), data);
		}
	}
	
	public static void reLoad(){
		cacheData.clear();
		init();
	}
	
	public static boolean idIsExist(String id){
    	return cacheData.containsKey(id);
    }
	
	public static BasicDBObject getValue(String id){
		if (idIsExist(id)){
    		return cacheData.get(id);
    	}
    	return null;
	}
}
