package com.xiaoma.messages;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.xiaoma.util.ConfigFileUtil;

public class MessagesSource {
	private static String fileLanguage = "";
	private static PropertiesConfiguration propsMessages;
	
	static {
		try {
            if (propsMessages==null){
            	propsMessages = new PropertiesConfiguration(ConfigFileUtil.getConfigFileUrl()+"messages_crud.properties");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
	}
	
	public static void init(String language){
		if (fileLanguage.equals(language) && propsMessages != null){
			return;
		}
		fileLanguage = language;
		String fileMsg = "_zh_CN";
		if (language == null || language.length() == 0){
			fileMsg = "_zh_CN";
		}else if (language != null && ("en".equals(language) 
    			|| "en-us".equals(language) || "en-US".equals(language))){
			fileMsg = "_en_US";
		}
		
        try {
        	if ("".equals(fileMsg) || "_zh_CN".equals(fileMsg)){
        		propsMessages = new PropertiesConfiguration();
        		propsMessages.setEncoding("UTF-8");
        		propsMessages.load(ConfigFileUtil.getConfigFileUrl()+"messages_crud"+fileMsg+".properties");
        	}else{
        		propsMessages = new PropertiesConfiguration(ConfigFileUtil.getConfigFileUrl()+"messages_crud"+fileMsg+".properties");
        	}
        } catch (Exception e) {
            //e.printStackTrace();
        }
	}
	
	public static String getString(String key){
		return propsMessages == null ? "" :  propsMessages.getString(key, "");
	}
	
	public static String getStringByKeyAry(String aryKey){
		String[] keys = aryKey.split("\\.");
		StringBuilder str = new StringBuilder();
		for (String key : keys){
			str.append(getString(key));
		}
		return str.toString();
	}
	
	public static String getStringFormat(String key, Object... dataAry){
		if (dataAry == null || dataAry.length < 1){
			return getString(key);
		}
		return String.format(getString(key),dataAry);
	}
	
	public static String getStringFormatByKeyAry(String aryKey,Object... dataAry){
		String[] keys = aryKey.split("\\.");
		StringBuilder str = new StringBuilder();
		for (String key : keys){
			str.append(getStringFormat(key, dataAry));
		}
		return str.toString();
	}
}
