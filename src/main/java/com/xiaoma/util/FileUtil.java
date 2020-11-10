package com.xiaoma.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;

public class FileUtil {
	
    public static List<BasicDBObject> readreadByLineBasicDBObject(String fileUrl) {
    	
    	List<BasicDBObject> list = new ArrayList<>();
    	
        File file = new File(fileUrl);
        if(!file.exists()){
        	return null;
        }
        BufferedReader reader = null;
        BasicDBObject data = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String lineData = null;
            while ((lineData = reader.readLine()) != null) {
            	if (!StringUtil.isEmpty(lineData)){
            		data = JsonUtil.parseJson(lineData.trim());
            		if (!JsonUtil.isEmpty(data)){
            			list.add(data);
            		}
            	}
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            list.clear();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        
        return list;
    }

    public static List<String> readByLineString(String fileUrl) {
    	
    	List<String> list = new ArrayList<>();
    	
        File file = new File(fileUrl);
        if(!file.exists()){
        	return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String lineData = null;
            while ((lineData = reader.readLine()) != null) {
            	list.add(lineData);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            list.clear();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        
        return list;
    }
}
