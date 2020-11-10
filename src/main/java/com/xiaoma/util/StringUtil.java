package com.xiaoma.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class StringUtil {

	public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() <= 0) {
            return true;
        }
        return false;
    }
	
	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}
	
	public static boolean isEmpty(List<?> list) {
		return (list == null || list.isEmpty());
	}
	
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}
	
	
	@SuppressWarnings("unchecked")
	public static<T> T stringToJavaType(String strValue, String javaType) {
    	if (strValue == null && "".equals(strValue)){
    		return null;
    	}
		if ("String".equals(javaType) || "string".equals(javaType)){
			return (T)strValue.toString();
		}else if ("Integer".equals(javaType) || "integer".equals(javaType)
   			 || "int".equals(javaType)){
			try{
				return (T)Integer.valueOf(strValue.toString());
			}catch (Throwable e) {
				return null;
			}
    	}else if ("Long".equals(javaType) || "long".equals(javaType)){
    		try{
    			return (T)Long.valueOf(strValue.toString());
			}catch (Throwable e) {
				return null;
			}
	   	}else if ("Double".equals(javaType) || "double".equals(javaType)){
	   		try{
	   			return (T)Double.valueOf(strValue.toString());
			}catch (Throwable e) {
				return null;
			}
	   	}else if ("Float".equals(javaType) || "float".equals(javaType)){
	   		try{
	   			return (T)Float.valueOf(strValue.toString());
			}catch (Throwable e) {
				return null;
			}
	   	}else if ("Boolean".equals(javaType) || "boolean".equals(javaType)){
	   		try{
	   			return (T)Boolean.valueOf(strValue.toString());
			}catch (Throwable e) {
				return null;
			}
	   	}
    	
    	return null;
    }
	
    /**
     * 获取当前时间 单位秒
     */
    public static long getCurrentTimeSeconds() {
        return LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();
    }
}
