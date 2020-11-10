package com.xiaoma.util;

import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;

public class ConfigFileUtil {
	private static PropertiesConfiguration props;
	private static String configFileUrl = null;
	
	static {
        try {
            if (props==null){
            	props = new PropertiesConfiguration(getConfigFileUrl()+"config.properties");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
	}
	
	public static String getConfigFileUrl(){
		if (configFileUrl != null){
			return configFileUrl;
		}
		String configUrl = ConfigFileUtil.class.getResource("/").toString();
		
		if (configUrl.indexOf("jar:file:") >= 0){
			configUrl = configUrl.substring(9);
		}else if (configUrl.indexOf("file:") >= 0){
			configUrl = configUrl.substring(5);
		}
		if (configUrl.indexOf("target") > 0){
			configUrl = configUrl.substring(0,configUrl.indexOf("target"));
			return configUrl;
		}
		int toLen = configUrl.indexOf("BOOT-INF");
		if (toLen < 0){
			toLen = configUrl.indexOf("classes");
		}
		configUrl = configUrl.substring(0,toLen-2);
		toLen = configUrl.lastIndexOf("/");
		configUrl = configUrl.substring(0,toLen+1);
		configFileUrl = configUrl;
		return configUrl;
	}
	
	public static String[] getStringArray(String key){
		return props == null ? null :  props.getStringArray(key);
	}
	
	public static String[] getStringArray(String key, String[] defaultValue){
		String[] ary = props == null ? null :  props.getStringArray(key);
		
		if (ary == null || ary.length == 0){
			return defaultValue;
		}
		return ary;
	}
	
	public static String getString(String key){
		return props == null ? null :  props.getString(key);
	}
	
    public static String getString(String key,String defaultValue){
         return props == null ? null : props.getString(key, defaultValue);
    }
	
	public static Integer getInt(String key){
		return props == null ? null :  props.getInt(key);
	}
	
	public static Integer getInt(String key, int defaultValue){
		return props == null ? null :  props.getInt(key, defaultValue);
	}
	
    public static Boolean getBoolean(String key){
        return props == null ? null : props.getBoolean(key);
    }
	
    public static Boolean getBoolean(String key, boolean defaultValue){
         return props == null ? null : props.getBoolean(key, defaultValue);
    }
    
    public static Properties getProperties(String key){
        return props.getProperties(key);
    }
}
