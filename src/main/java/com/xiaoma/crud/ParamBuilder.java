package com.xiaoma.crud;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.xiaoma.util.JsonUtil;
import com.xiaoma.util.StringUtil;

public class ParamBuilder {

	/**
	 * 构建实体参数
	 * @param crudName   操作名称：add、modify、del、query、queryEquals、queryOne
	 * @param dataJson
	 * @param tableInfo
	 * @param authority
	 * @return
	 */
	public static BasicDBObject entityParamBuilder(String crudName, BasicDBObject dataJson, 
			BasicDBObject tableInfo, BasicDBObject appendInfo){
		if ("del".equals(crudName)){
			return entityDelParamBuilder(dataJson, tableInfo);
		}else if (crudName.startsWith("query")){
			return entityQueryParamBuilder(crudName, dataJson, tableInfo, appendInfo);
		}
		List<BasicDBObject> fieldList = JsonUtil.getListJson(tableInfo, "fieldList");
		
		BasicDBObject tableData = new BasicDBObject();
		
		for (BasicDBObject field : fieldList){
			if ("Auto_Increment_Db".equals(field.getString("generateRule"))
					&& "add".equals(crudName)){
				continue;
			}else if ("modify".equals(crudName)){
				if ("create_user".equals(field.getString("name"))
						|| "create_time".equals(field.getString("name"))){
					continue;
				}
			}
			
			if ("Get_System_Timestamp".equals(field.getString("generateRule"))){
				if ("String".equals(field.getString("type"))){
					tableData.put(field.getString("name"), String.valueOf(StringUtil.getCurrentTimeSeconds()));
				}else{
					tableData.put(field.getString("name"), StringUtil.getCurrentTimeSeconds());
				}
			}else if ("Get_Header_Key".equals(field.getString("generateRule"))){
				tableData.put(field.getString("name"), appendInfo.getString(field.getString("name")));
			}else if ("Get_Header_ToStrAry".equals(field.getString("generateRule"))){
				if (appendInfo.get(field.getString("name")) instanceof String){
					tableData.put(field.getString("name"), appendInfo.getString(field.getString("name")).split(","));
				}else{
					tableData.put(field.getString("name"), appendInfo.get(field.getString("name")));
				}
			}else{
				if (JsonUtil.keyExistNotNull(dataJson, field.getString("name"))){
					tableData.put(field.getString("name"), dataJson.get(field.getString("name")));
				}
			}
		}
		return tableData;
	}
	
	private static BasicDBObject entityDelParamBuilder(BasicDBObject dataJson, BasicDBObject tableInfo){
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		BasicDBObject tableData = new BasicDBObject();
		for (BasicDBObject field : primaryKey){
			if (JsonUtil.keyExistNotNull(dataJson, field.getString("name"))){
				tableData.put(field.getString("name"), dataJson.get(field.getString("name")));
			}
		}
		return tableData;
	}
	
	private static BasicDBObject entityQueryParamBuilder(String crudName, BasicDBObject dataJson, 
			BasicDBObject tableInfo, BasicDBObject appendInfo){
		//crudName   操作名称：query、queryEquals、queryOne
		List<BasicDBObject> fieldList = JsonUtil.getListJson(tableInfo, "fieldList");
		
		BasicDBObject tableData = new BasicDBObject();
		BasicDBObject between = null;
		
		for (BasicDBObject field : fieldList){
			if ("Get_Header_Key".equals(field.getString("generateRule"))){
				if (isAdmin(appendInfo)){
					continue;
				}
				tableData.put(field.getString("name"), appendInfo.getString(field.getString("name")));
			}else if ("Get_Header_ToStrAry".equals(field.getString("generateRule"))){
				if (isAdmin(appendInfo)){
					continue;
				}
				if (appendInfo.get(field.getString("name")) instanceof String){
					tableData.put(field.getString("name"), appendInfo.getString(field.getString("name")).split(","));
				}else{
					tableData.put(field.getString("name"), appendInfo.get(field.getString("name")));
				}
			}else{
				if (JsonUtil.keyExistNotNull(dataJson, field.getString("name"))){
					between = betweenField(dataJson, field);
					if (!JsonUtil.isEmpty(between)){
						tableData.put(field.getString("name"), between);
						between = null;
						continue;
					}
					if ("query".equals(crudName) && "String".equals(field.getString("type"))
							&& !"Auto_Increment_Db".equals(field.getString("generateRule"))){
						tableData.put(field.getString("name"), likeMatch(dataJson, field));
					}else{
						if ("id".equals(field.getString("name")) && "Auto_Increment_Db".equals(field.getString("generateRule"))){
							tableData.put("_id", new ObjectId(dataJson.getString("id")));
						}else{
							tableData.put(field.getString("name"), dataJson.get(field.getString("name")));
						}
					}
				}
			}
		}
		
		return tableData;
	}
	
	private static BasicDBObject betweenField(BasicDBObject dataJson, BasicDBObject fieldObj){
		if (!(dataJson.get(fieldObj.getString("name")) instanceof String)){
			return null;
		}
		String objValue = dataJson.getString(fieldObj.getString("name"));
		
		if (objValue.indexOf(",") < 0){
			return null;
		}
		
		String[] values = objValue.split(",");
		if (values.length != 2){
			return null;
		}
		
		if ((values[0].startsWith("[") || values[0].startsWith("【") || values[0].startsWith("［")
				|| values[0].startsWith("(") || values[0].startsWith("（") || values[0].startsWith("（"))
			&& 
			(values[1].endsWith("]") || values[1].endsWith("】") || values[1].endsWith("］")
				|| values[1].endsWith(")") || values[1].endsWith("）") || values[1].endsWith("）"))){
			return operatorFormula(values, fieldObj.getString("type"));
		}
		
		return null;
	}
	
	private static BasicDBObject operatorFormula(String[] values, String javaType){
		BasicDBObject query = new BasicDBObject();
		StringBuilder strFormula = new StringBuilder();
		Object obj = null;
		if (values[0].startsWith("[") || values[0].startsWith("【") || values[0].startsWith("［")){
			strFormula.append(values[0].substring(1));
			if (strFormula.length() > 0){
				obj = StringUtil.stringToJavaType(strFormula.toString(), javaType);
				if (obj != null){
					query.put("$gte", obj);
				}
			}
		}else if (values[0].startsWith("(") || values[0].startsWith("（") || values[0].startsWith("（")){
			strFormula.append(values[0].substring(1));
			if (strFormula.length() > 0){
				obj = StringUtil.stringToJavaType(strFormula.toString(), javaType);
				if (obj != null){
					query.put("$gt", obj);
				}
			}
		}else{
			obj = StringUtil.stringToJavaType(values[0], javaType);
			if (obj != null){
				query.put("$gt", obj);
			}
		}
		obj = null;
		strFormula.delete(0, strFormula.length());
		if (values[1].endsWith("]") || values[1].endsWith("】") || values[1].endsWith("］")){
			strFormula.append(values[1].substring(0,values[1].length()-1));
			if (strFormula.length() > 0){
				obj = StringUtil.stringToJavaType(strFormula.toString(), javaType);
				if (obj != null){
					query.put("$lte", obj);
				}
			}
		}else if (values[1].endsWith(")") || values[1].endsWith("）") || values[1].endsWith("）")){
			strFormula.append(values[1].substring(0,values[1].length()-1));
			if (strFormula.length() > 0){
				obj = StringUtil.stringToJavaType(strFormula.toString(), javaType);
				if (obj != null){
					query.put("$lt", obj);
				}
			}
		}else{
			obj = StringUtil.stringToJavaType(values[1], javaType);
			if (obj != null){
				query.put("$lt", obj);
			}
		}
		
		return query;
	}
	
	public static BasicDBObject getQueryByPrimaryKey(BasicDBObject tableData, BasicDBObject tableInfo, boolean removeFlag){
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		BasicDBObject query = new BasicDBObject();
		
		for (BasicDBObject field : primaryKey){
			if ("id".equals(field.getString("name")) && "Auto_Increment_Db".equals(field.getString("generateRule"))){
				query.put("_id",new ObjectId(tableData.getString("id")));
			}else{
				query.put(field.getString("name"),tableData.get(field.getString("name")));
			}
			
			if (removeFlag){
				tableData.remove(field.getString("name"));
			}
		}
		
		return query;
	}
	
	public static BasicDBObject getQueryById(String id, BasicDBObject tableInfo){
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		BasicDBObject query = new BasicDBObject();
		
		boolean bl = false;
		for (BasicDBObject field : primaryKey){
			if ("id".equals(field.getString("name"))){
				if ("Auto_Increment_Db".equals(field.getString("generateRule"))){
					bl = true;
				}
				break;
			}
		}
		
		if (bl){
			query.put("_id",new ObjectId(id));
		}else{
			query.put("id",id);
		}
		
		return query;
	}
	
	private static Pattern likeMatch(BasicDBObject dataJson, BasicDBObject fieldObj){
        //模糊匹配
		return Pattern.compile("^.*" + dataJson.getString(fieldObj.getString("name")) +".*$", Pattern.CASE_INSENSITIVE);
	}
	
    /**
     * 判断用户是否为管理员
     * @return =true：管理员；=false：普通角色
     */
	private static boolean isAdmin(BasicDBObject appendInfo){
		String roleType = appendInfo.getString("roleType");
		if ("1".equals(roleType)){
			return true;
		}
		return false;
	}
}
