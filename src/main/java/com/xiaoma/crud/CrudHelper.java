package com.xiaoma.crud;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.xiaoma.config.TableInfoConfig;
import com.xiaoma.db.mongodb.MongoClientHelper;
import com.xiaoma.messages.MessagesSource;
import com.xiaoma.util.JsonUtil;

public class CrudHelper {

	public static BasicDBObject crudOperating(String table, String crudName, String language, 
			BasicDBObject queryParam, BasicDBObject appendInfo){
		MessagesSource.init(language);
		BasicDBObject result = new BasicDBObject();
		
		BasicDBObject tableInfo = TableInfoConfig.getValue(table);
		if (JsonUtil.isEmpty(tableInfo)){
			result.put("errorMessage", MessagesSource.getStringByKeyAry("illegal.operation"));
			return result;
		}
		
		BasicDBObject tableData = ParamBuilder.entityParamBuilder(crudName, queryParam, tableInfo, appendInfo);
		
		ParamValidator.entityParamCheck(crudName, tableData, tableInfo);
		
		if (JsonUtil.keyExistAndValueNotBlank(tableData, "errorMessage")){
			result.put("errorMessage", tableData.getString("errorMessage"));
			return result;
		}
		
		if ("add".equals(crudName)){
			crudAdd(crudName, tableInfo, tableData);
		}else if ("modify".equals(crudName)){
			crudModify(crudName, tableInfo, tableData);
		}else if ("del".equals(crudName)){
			crudDel(crudName, tableInfo, tableData);
		}else if (crudName.startsWith("query")){
			crudQuery(crudName, queryParam, tableInfo, tableData);
		}
		
		result.put("result", tableData.get("result"));
		
		return result;
	}

	private static void crudAdd(String crudName, BasicDBObject tableInfo, BasicDBObject tableData){
		boolean bl = MongoClientHelper.insert(tableInfo.getString("id"), tableData);
		
		if (bl){
			tableData.put("result", getReturnMsgPrefix(crudName, tableInfo, tableData) + 
					MessagesSource.getStringByKeyAry("comma.added.success"));
		}else{
			tableData.put("result", getReturnMsgPrefix(crudName, tableInfo, tableData) + 
					MessagesSource.getStringByKeyAry("comma.added.failed"));
		}
	}
	
	private static void crudModify(String crudName, BasicDBObject tableInfo, BasicDBObject tableData){
		BasicDBObject query = ParamBuilder.getQueryByPrimaryKey(tableData, tableInfo, true);
		boolean bl = MongoClientHelper.updateOne(tableInfo.getString("id"), query, tableData);
		
		if (query.containsField("_id")){
			query.put("id", query.getString("_id"));
		}
		if (bl){
			tableData.put("result", getReturnMsgPrefix(crudName, tableInfo, query) + 
					MessagesSource.getStringByKeyAry("comma.modified.success"));
		}else{
			tableData.put("result", getReturnMsgPrefix(crudName, tableInfo, query) + 
					MessagesSource.getStringByKeyAry("comma.modified.failed"));
		}
	}
	
	private static void crudDel(String crudName, BasicDBObject tableInfo, BasicDBObject tableData){
		BasicDBObject query = ParamBuilder.getQueryByPrimaryKey(tableData, tableInfo, false);
		boolean bl = MongoClientHelper.deleteOne(tableInfo.getString("id"), query);
		
		if (bl){
			tableData.put("result", getReturnMsgPrefix(crudName, tableInfo, tableData) + 
					MessagesSource.getStringByKeyAry("comma.deletion.success"));
		}else{
			tableData.put("result", getReturnMsgPrefix(crudName, tableInfo, tableData) + 
					MessagesSource.getStringByKeyAry("comma.deletion.failed"));
		}
	}
	
	private static void crudQuery(String crudName, BasicDBObject queryParam, 
			BasicDBObject tableInfo, BasicDBObject tableData){
		BasicDBObject projInfo = JsonUtil.getJson(queryParam, "projInfo");
		if (JsonUtil.isEmpty(projInfo)){
			projInfo = null;
		}
		BasicDBObject orderby = JsonUtil.getJson(queryParam, "orderby");
		if (JsonUtil.isEmpty(orderby)){
			orderby = null;
		}
		BasicDBObject pageInfo = JsonUtil.getJson(queryParam, "pageInfo");
		boolean appendIdFalg = ParamValidator.chkAppendId(tableInfo);
		
		if ("queryOne".equals(crudName)){
			BasicDBObject oneData = MongoClientHelper.findOne(tableInfo.getString("id"), tableData, projInfo, orderby, appendIdFalg);
			oneData.remove("_id");
			tableData.put("result", oneData);
		}else{
			if (!JsonUtil.isEmpty(pageInfo)){
				long count = MongoClientHelper.count(tableInfo.getString("id"), tableData);
				
				if (count <= 0){
					tableData.put("result", new BasicDBObject("totalCount",0).append("data", null));
					return;
				}
				
				int skipCount = pageInfo.getInt("skip");
				int limitCount = pageInfo.getInt("limit");
				List<BasicDBObject> list = MongoClientHelper.findListByPage(tableInfo.getString("id"), tableData, projInfo, orderby, skipCount, limitCount, appendIdFalg);
				
				tableData.put("result", new BasicDBObject("totalCount",count).append("data", list));
			}else{
				List<BasicDBObject> list = MongoClientHelper.findList(tableInfo.getString("id"), tableData, orderby, projInfo, appendIdFalg);
				tableData.put("result", list);
			}
		}
	}
	
	private static String getReturnMsgPrefix(String crudName, BasicDBObject tableInfo, BasicDBObject tableData){
		List<BasicDBObject> primaryKey = JsonUtil.getListJson(tableInfo, "primaryKey");
		StringBuilder msgPrefix = new StringBuilder();
		msgPrefix.append("Collection="+tableInfo.getString("id"));
		if ("add".equals(crudName)){
			return msgPrefix.toString();
		}
		for (BasicDBObject field : primaryKey){
			msgPrefix.append(","+field.getString("name")+"="+tableData.get(field.getString("name")));
		}
		return msgPrefix.toString();
	}
	
	public static long isExistId(String table, String id){
		BasicDBObject tableInfo = TableInfoConfig.getValue(table);
		BasicDBObject query = ParamBuilder.getQueryById(id, tableInfo);
		
		return MongoClientHelper.count(tableInfo.getString("id"), query);
	}
}
