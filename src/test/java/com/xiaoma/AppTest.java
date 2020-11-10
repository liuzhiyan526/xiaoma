package com.xiaoma;

import com.mongodb.BasicDBObject;
import com.xiaoma.crud.CrudHelper;
import com.xiaoma.db.mongodb.MongoConnection;

public class AppTest {

	public static void main(String[] args) throws Exception {
		String language = "zh";
		String table = "Label";
		String crudName = "add";
		
		BasicDBObject queryParam = new BasicDBObject();
		
		queryParam.put("id", "111111");
		queryParam.put("name", "名称测试");
		queryParam.put("password", "222222");
		queryParam.put("categoryName", "测试分类");
		queryParam.put("labelName", "测试标签");

		BasicDBObject appendInfo = new BasicDBObject();
		appendInfo.put("action_user", "liuzhiyan");
		appendInfo.put("create_user", "liuzhiyan");
		appendInfo.put("role", "admin");
		appendInfo.put("roleType", "1");
		
		MongoConnection.getInstance().init();
		BasicDBObject result = CrudHelper.crudOperating(table, crudName, language, queryParam, appendInfo);
		System.out.println("result="+result);
	}

}
