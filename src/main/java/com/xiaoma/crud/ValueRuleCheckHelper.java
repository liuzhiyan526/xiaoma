package com.xiaoma.crud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.xiaoma.db.mongodb.MongoClientHelper;
import com.xiaoma.messages.MessagesSource;
import com.xiaoma.util.JsonUtil;
import com.xiaoma.util.StringUtil;
import com.xiaoma.util.UUIDUtil;

public class ValueRuleCheckHelper {

	public static void checkRule(String crudName, BasicDBObject tableInfo, BasicDBObject field, BasicDBObject tableData){
		String checkRule = field.getString("checkRule");
		String[] aryRule = checkRule.split("\\.");
		
		for (String rule : aryRule){
			check(crudName, rule, tableInfo, field, tableData);
			if (JsonUtil.keyExistAndValueNotBlank(tableData, "errMsg")){
				return;
			}
		}
	}
	
	private static void check(String crudName, String checkRule, BasicDBObject tableInfo, BasicDBObject field, BasicDBObject tableData){
		if ("NotNull".equals(checkRule)){
			checkNotNull(field, tableData);
		}else if ("NotBlank".equals(checkRule)){
			checkNotBlank(field, tableData);
		}else if ("NoRepetition".equals(checkRule)){
			checkNoRepetition(crudName, tableInfo, field, tableData);
		}else if ("NoRepetitionSuggest".equals(checkRule)){
			checkNoRepetitionSuggest(crudName, tableInfo, field, tableData);
		}else{
			checkRestrictedInput(checkRule, field, tableData);
		}
	}
	
	/**
	 * 是否存在并且不为null
	 * @param field      字段定义信息
	 * @param tableData  参数
	 * @param language   语言
	 */
	private static void checkNotNull(BasicDBObject field, BasicDBObject tableData){
		if (!JsonUtil.keyExistNotNull(tableData, field.getString("name"))){
			tableData.put("errorMessage", 
					field.getString("name")+MessagesSource.getStringByKeyAry("comma.not.blank"));
			return;
		}
	}
	
	/**
	 * 是否存在并且不为null，不为空字符""
	 * @param field      字段定义信息
	 * @param tableData  参数
	 * @param language   语言
	 */
	private static void checkNotBlank(BasicDBObject field, BasicDBObject tableData){
		if (!JsonUtil.keyExistAndValueNotBlank(tableData, field.getString("name"))){
			tableData.put("errorMessage", 
					field.getString("name")+MessagesSource.getStringByKeyAry("comma.not.blank"));
			return;
		}
	}
	
	/**
	 * 值不能重复
	 * @param field      字段定义信息
	 * @param coll       表名
	 * @param tableData  参数
	 * @param language   语言
	 */
	private static void checkNoRepetition(String crudName, BasicDBObject tableInfo, BasicDBObject field, BasicDBObject tableData){
		BasicDBObject query = new BasicDBObject(field.getString("name"), tableData.get(field.getString("name")));
		
		if ("add".equals(crudName)){
			long dataSize = MongoClientHelper.count(tableInfo.getString("id"), query);
			if (dataSize < 0){
				tableData.put("errorMessage", MessagesSource.getString("internalError"));
				return;
			}else if (dataSize > 0){
				tableData.put("errorMessage", 
						field.getString("name")+"="+tableData.get(field.getString("name"))+MessagesSource.getStringByKeyAry("comma.existed"));
				return;
			}
		}else if ("modify".equals(crudName)){
			BasicDBObject proj = getProjByPrimaryKey(tableInfo);
			boolean appendIdFalg = ParamValidator.chkAppendId(tableInfo);
			List<BasicDBObject> list = MongoClientHelper.findList(tableInfo.getString("id"), query, proj, null, appendIdFalg);
			
			if (StringUtil.isEmpty(list)){
				return;
			}
			
			if (list.size() > 1){
				tableData.put("errorMessage", 
						field.getString("name")+"="+tableData.get(field.getString("name"))+MessagesSource.getStringByKeyAry("comma.existed"));
				return;
			}
			
			BasicDBObject dataPrimary = list.get(0);
			List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
			for (BasicDBObject primaryField : primaryKey){
				if (!tableData.getString(primaryField.getString("name"))
						.equals(dataPrimary.getString(primaryField.getString("name")))){
					tableData.put("errorMessage", 
							field.getString("name")+"="+tableData.get(field.getString("name"))+MessagesSource.getStringByKeyAry("comma.existed"));
					return;
				}
			}
		}
		
	}
	
	/**
	 * 值不能重复，如果重复，给出建议值
	 * @param field      字段定义信息
	 * @param coll       表名
	 * @param tableData  参数
	 * @param language   语言
	 */
	private static void checkNoRepetitionSuggest(String crudName, BasicDBObject tableInfo, BasicDBObject field, BasicDBObject tableData){
		BasicDBObject query = new BasicDBObject(field.getString("name"), tableData.get(field.getString("name")));
		
		if ("add".equals(crudName)){
			long dataSize = MongoClientHelper.count(tableInfo.getString("id"), query);
			if (dataSize < 0){
				tableData.put("errorMessage", MessagesSource.getString("internalError"));
				return;
			}else if (dataSize > 0){
				tableData.put("errorMessage", 
						field.getString("name")+"="+tableData.get(field.getString("name"))+MessagesSource.getStringByKeyAry("comma.already.exist.comma.suggest.colon")
						+tableData.get(field.getString("name"))+"-"+UUIDUtil.getUUID_8());
				return;
			}
		}else if ("modify".equals(crudName)){
			BasicDBObject proj = getProjByPrimaryKey(tableInfo);
			boolean appendIdFalg = ParamValidator.chkAppendId(tableInfo);
			List<BasicDBObject> list = MongoClientHelper.findList(tableInfo.getString("id"), query, proj, null, appendIdFalg);
			
			if (StringUtil.isEmpty(list)){
				return;
			}
			
			if (list.size() > 1){
				tableData.put("errorMessage", 
						field.getString("name")+"="+tableData.get(field.getString("name"))+MessagesSource.getStringByKeyAry("comma.already.exist.comma.suggest.colon")
						+tableData.get(field.getString("name"))+"-"+UUIDUtil.getUUID_8());
				return;
			}
			
			BasicDBObject dataPrimary = list.get(0);
			List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
			for (BasicDBObject primaryField : primaryKey){
				if (!tableData.getString(primaryField.getString("name"))
						.equals(dataPrimary.getString(primaryField.getString("name")))){
					tableData.put("errorMessage", 
							field.getString("name")+"="+tableData.get(field.getString("name"))+MessagesSource.getStringByKeyAry("comma.already.exist.comma.suggest.colon")
							+tableData.get(field.getString("name"))+"-"+UUIDUtil.getUUID_8());
					return;
				}
			}
		}
		
	}
	
	private static BasicDBObject getProjByPrimaryKey(BasicDBObject tableInfo){
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		BasicDBObject proj = new BasicDBObject();
		
		boolean removeIdFlag = true;
		for (BasicDBObject field : primaryKey){
			if ("id".equals(field.getString("name")) && "Auto_Increment_Db".equals(field.getString("generateRule"))){
				removeIdFlag = false;
			}else {
				proj.put(field.getString("name"),1);
			}
		}
		
		if (removeIdFlag){
			proj.put("_id", 0);
		}
		
		return proj;
	}
	
	private static void checkRestrictedInput(String checkRule, BasicDBObject field, BasicDBObject tableData){
		if (!JsonUtil.keyExistAndValueNotBlank(tableData, field.getString("name"))){
			return;
		}
		String[] aryRule = checkRule.split("_");
		
		if (!checkStringFortmat(tableData.getString(field.getString("name")), aryRule)){
			tableData.put("errorMessage", 
					field.getString("name")+MessagesSource.getStringByKeyAry("comma.value.canOnly."+checkRule.replaceAll("_", "\\.")));
			return;
		}
	}
	
	public static void main(String[] args) {
		String checkRule = "Chinese_Letters_Numbers_Underscores_Line";
		String[] aryRule = checkRule.split("_");
		String obj = "sdds中";
		
		System.out.println(checkStringFortmat(obj, aryRule));
	}
	
	private static boolean checkStringFortmat(String obj, String[] aryRule) {
		StringBuilder matchesStr = new StringBuilder();
		for (String rule : aryRule){
			if (ruleMatches.containsKey(rule)){
				matchesStr.append(ruleMatches.get(rule));
			}
		}
		if (matchesStr.length() > 0){
			return checkStringFortmatByMatches(obj, matchesStr.toString());
		}
		
		return true;
	}
	
	
	private static boolean checkStringFortmatByMatches(String obj, String matchesStr) {
		if (obj == null || "".equals(obj)){
			return true;
		}
    	if (obj.matches("["+matchesStr+"]*")) {
    		return true;
    	}

        return false;
	}
	
	private static Map<String,String> ruleMatches = new HashMap<>();
	
	static{
		ruleMatches.put("Chinese", "\u4E00-\u9FA5");
		ruleMatches.put("Letters", "a-zA-Z");
		ruleMatches.put("Numbers", "0-9");
		ruleMatches.put("Underscores", "_");
		ruleMatches.put("Line", "\\-");
	}
}
