package com.xiaoma.crud;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.xiaoma.db.mongodb.MongoClientHelper;
import com.xiaoma.messages.MessagesSource;
import com.xiaoma.util.JsonUtil;
import com.xiaoma.util.StringUtil;

public class ParamValidator {

	public static void entityParamCheck(String crudName, BasicDBObject tableData, BasicDBObject tableInfo){
		if (crudName.startsWith("query")){//查询时不用校验
			return;
		}
		entityParamCheckPrimaryKey(crudName, tableData,tableInfo);
		if (JsonUtil.keyExistAndValueNotBlank(tableData, "errorMessage")){
			return;
		}
		entityParamCheckImpKey(crudName, tableData, tableInfo);
		if (JsonUtil.keyExistAndValueNotBlank(tableData, "errorMessage")){
			return;
		}
		if ("del".equals(crudName)){//删除时，只校验主键和是否被引用
			return;
		}
		if ("add".equals(crudName) || "modify".equals(crudName)){
			entityParamCheckForeignKey(crudName, tableData, tableInfo);
			if (JsonUtil.keyExistAndValueNotBlank(tableData, "errorMessage")){
				return;
			}
		}
		
		List<BasicDBObject> fieldList = JsonUtil.getListJson(tableInfo, "fieldList");
		for (BasicDBObject field : fieldList){
			if (JsonUtil.keyExistAndValueNotBlank(field, "checkRule")){
				ValueRuleCheckHelper.checkRule(crudName, tableInfo, field, tableData);
				if (JsonUtil.keyExistAndValueNotBlank(tableData, "errorMessage")){
					return;
				}
			}
			if (JsonUtil.keyExistAndValueNotBlank(field, "valueLengh")){
				entityParamCheckValueLengh(field, tableData);
				if (JsonUtil.keyExistAndValueNotBlank(tableData, "errorMessage")){
					return;
				}
			}
		}
	}
	
	private static void entityParamCheckValueLengh(BasicDBObject field, BasicDBObject tableData){
		if (!JsonUtil.keyExistAndValueNotBlank(tableData, field.getString("name"))){
			return;
		}
		String value = tableData.getString(field.getString("name"));
		int valueLen = value.length();
		String valueLengh = field.getString("valueLengh");
		String[] lenAry = valueLengh.split(",");
		
		if ((lenAry[0].startsWith("[") || lenAry[0].startsWith("【")) && lenAry[0].length() > 1){
			int len = Integer.parseInt(lenAry[0].substring(1));
			if (valueLen < len){
				tableData.put("errorMessage", 
						field.getString("name")+MessagesSource.getStringFormatByKeyAry("comma.minimumLengthValue",String.valueOf(len)));
				return;
			}
		}else if ((lenAry[0].startsWith("(") || lenAry[0].startsWith("（")) && lenAry[0].length() > 1){
			int len = Integer.parseInt(lenAry[0].substring(1))+1;
			if (valueLen < len){
				tableData.put("errorMessage", 
						field.getString("name")+MessagesSource.getStringFormatByKeyAry("comma.minimumLengthValue",String.valueOf(len)));
				return;
			}
		}else{
			if (valueLen > Integer.parseInt(lenAry[0])){
				tableData.put("errorMessage", 
						field.getString("name")+MessagesSource.getStringFormatByKeyAry("comma.maxLengthValue",String.valueOf(lenAry[0])));
				return;
			}
		}
		
		if (lenAry.length >= 2){
			if ((lenAry[1].endsWith("]") || lenAry[1].endsWith("】")) && lenAry[1].length() > 1){
				int len = Integer.parseInt(lenAry[1].substring(0,lenAry[1].length()-1));
				if (valueLen > len){
					tableData.put("errorMessage", 
							field.getString("name")+MessagesSource.getStringFormatByKeyAry("comma.maxLengthValue",String.valueOf(len)));
					return;
				}
			}else if ((lenAry[1].startsWith(")") || lenAry[1].startsWith("）")) && lenAry[1].length() > 1){
				int len = Integer.parseInt(lenAry[1].substring(0,lenAry[1].length()-1))-1;
				if (valueLen > len){
					tableData.put("errorMessage", 
							field.getString("name")+MessagesSource.getStringFormatByKeyAry("comma.maxLengthValue",String.valueOf(len)));
					return;
				}
			}else{
				if (valueLen > Integer.parseInt(lenAry[1])){
					tableData.put("errorMessage", 
							field.getString("name")+MessagesSource.getStringFormatByKeyAry("comma.maxLengthValue",String.valueOf(lenAry[1])));
					return;
				}
			}
		}
	}
	
	private static void entityParamCheckPrimaryKey(String crudName, BasicDBObject tableData, BasicDBObject tableInfo){
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		BasicDBObject query = new BasicDBObject();
		
		for (BasicDBObject field : primaryKey){
			if ("add".equals(crudName)){
				if ("Auto_Increment_Db".equals(field.getString("generateRule"))){
					//在添加操作时，数据库自增长类型的主键，不需要添加
					if (JsonUtil.keyExistNotNull(tableData, field.getString("name"))){
						tableData.remove(field.getString("name"));
					}
					return;
				}
				//在添加操作时，需要校验主键是否重复
				query.put(field.getString("name"),tableData.get(field.getString("name")));
			}
			
			if (!JsonUtil.keyExistNotNull(tableData, field.getString("name"))){
				//主键缺失
				tableData.put("errorMessage", 
						field.getString("name")+MessagesSource.getStringByKeyAry("comma.not.blank"));
				return;
			}
			
		}
		
		//在添加操作时，如果是联合主键，需要校验：主键是否重复
		if ("add".equals(crudName) && !JsonUtil.isEmpty(query) && primaryKey.size() > 1){
			BasicDBObject proj = new BasicDBObject();
			proj.put("_id", 1);
			BasicDBObject oneData = MongoClientHelper.findOne(tableInfo.getString("id"), query, proj);
			if (!JsonUtil.isEmpty(oneData)){
				//主键重复
				tableData.put("errorMessage", 
						tableInfo.getString("id")+queryToString(query)+MessagesSource.getStringByKeyAry("comma.existed"));
			}
		}
	}
	
	private static void entityParamCheckForeignKey(String crudName, BasicDBObject tableData, BasicDBObject tableInfo){
		if (!JsonUtil.keyExistNotNull(tableInfo, "foreignKey")){
			return;
		}
		List<BasicDBObject> foreignKey = JsonUtil.getListJson(tableInfo, "foreignKey");
		BasicDBObject query = null;
		BasicDBObject proj = null;
		BasicDBObject oneData = null;
		
		for (BasicDBObject field : foreignKey){
			if (!JsonUtil.keyExistNotNull(tableData, field.getString("field"))){
				//没有数据，不用检测
				continue;
			}
			query = new BasicDBObject(field.getString("foreignField"), tableData.get(field.getString("field")));
			
			proj = new BasicDBObject();
			proj.put("_id", 1);
			oneData = MongoClientHelper.findOne(field.getString("tableName"), query, proj);
			if (JsonUtil.isEmpty(oneData)){
				//外键不存在
				tableData.put("errorMessage", 
						field.getString("tableName")+queryToString(query)+MessagesSource.getStringByKeyAry("comma.value.outOfRange"));
				return;
			}
		}
	}
	
	private static void entityParamCheckImpKey(String crudName, BasicDBObject tableData, BasicDBObject tableInfo){
		if (!"del".equals(crudName) && !"modify".equals(crudName)){
			return;
		}
		if (!JsonUtil.keyExistNotNull(tableInfo, "impKey")){
			return;
		}
		List<BasicDBObject> impKey = JsonUtil.getListJson(tableInfo, "impKey");
		if (StringUtil.isEmpty(impKey)){
			return;
		}
		
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		BasicDBObject query = new BasicDBObject();
		for (BasicDBObject field : primaryKey){
			if ("id".equals(field.getString("name")) && "Auto_Increment_Db".equals(field.getString("generateRule"))){
				query.put("_id",new ObjectId(tableData.getString("id")));
			}else{
				query.put(field.getString("name"),tableData.get(field.getString("name")));
			}
		}
		
		boolean appendIdFalg = ParamValidator.chkAppendId(tableInfo);
		BasicDBObject oneData = MongoClientHelper.findOne(tableInfo.getString("id"), query, null, appendIdFalg);
		
		if (JsonUtil.isEmpty(oneData)){
			tableData.put("errorMessage", 
					tableInfo.getString("id")+queryToString(query)+MessagesSource.getStringByKeyAry("comma.no.exist"));
			return;
		}
		
		BasicDBObject queryChk = null;
		BasicDBObject dataChk = null;
		BasicDBObject projChk = new BasicDBObject();
		projChk.put("_id", 1);
		for (BasicDBObject field : impKey){
			if ("modify".equals(crudName)){
				if (!JsonUtil.keyExistNotNull(tableData, field.getString("field"))){
					//修改操作时，如果没有修改对应的字段，不用校验
					continue;
				}
				//修改操作时，需要比较数据是否发生变化，如果没有变化，不用校验
				if (oneData.getString(field.getString("field")).equals(tableData.getString(field.getString("field")))){
					continue;
				}
			}
			queryChk = new BasicDBObject(field.getString("impField"), oneData.get(field.getString("field")));
			dataChk = MongoClientHelper.findOne(field.getString("tableName"), queryChk, projChk);
			if (!JsonUtil.isEmpty(dataChk)){
				//正在被使用，不允许被删除和修改
				tableData.put("errorMessage", 
						tableInfo.getString("id")+","+field.getString("field")+"="+oneData.get(field.getString("field"))+
						MessagesSource.getStringByKeyAry("comma.used"));
				return;
			}
			dataChk = null;
		}
	}
	
	public static boolean chkAppendId(BasicDBObject tableInfo){
		List<BasicDBObject> fieldList = JsonUtil.getListJson(tableInfo, "fieldList");
		for (BasicDBObject field : fieldList){
			if ("id".equals(field.getString("name"))){
				if ("Auto_Increment_Db".equals(field.getString("generateRule"))){
					return true;
				}
				break;
			}
		}
		
		return false;
	}
	
	public static String queryToString(BasicDBObject query){
		StringBuilder queryStr = new StringBuilder();
		
		Iterator<Entry<String, Object>> it = query.entrySet().iterator();
		
		while (it.hasNext()){
			Entry<String, Object> e = it.next();
			if ("_id".equals(e.getKey())){
				ObjectId oid = (ObjectId)e.getValue();
				queryStr.append(",id="+oid.toHexString());
			}else{
				queryStr.append(","+e.getKey()+"="+e.getValue());
			}
		}
		return queryStr.toString();
	}
}
