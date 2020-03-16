package com.wehotel.fizz.business;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wehotel.fizz.Pipeline;
import com.wehotel.fizz.Step;
import com.wehotel.fizz.input.InputType;
 


class PipelineTests {
	@Test
	void contextLoads() {
		 
//		https://gist.github.com/cer/04ce15ba46f54634312740135fcfdeea
		// 直接返回响应结果，跳过后面的filter和controller
		
		Map<String, Map> requests1 = new HashMap<String, Map>();
		 
		Map <String ,Object>innerConfig1 =  new HashMap<String, Object>();
		innerConfig1.put("url","http://172.25.63.197:9090/trip-inninfo-api/inn/getHotelInfo");
		innerConfig1.put("method","GET");
		
		Map <String ,Object>requestConfig = new HashMap<String, Object>();
		requestConfig.put("type", InputType.REQUEST.toString());
		requestConfig.put("config", innerConfig1);
		requests1.put("hotelInfo", requestConfig);
		
		innerConfig1 =  new HashMap<String, Object>();
		innerConfig1.put("url","http://172.25.33.79/we-meb/rest/we-meb/api/getMebInfo");
		innerConfig1.put("method","POST");
		Map <String ,Object> headers = new HashMap<String ,Object>();
		headers.put("X-AUTH-HEADER", "groovy import com.wehotel.fizz.business.RsaClientUtil; RsaClientUtil.encodeNet(config.get(\"token\")+\",\"+config.get(\"appid\")+\",\"+System.currentTimeMillis() )");
		headers.put("Content-Type", "application/json; charset=UTF-8");
		headers.put("Accept", "application/json; charset=UTF-8");
		innerConfig1.put("headers", headers);
		
		requestConfig = new HashMap<String, Object>();
		requestConfig.put("type", InputType.REQUEST.toString());
		
		Map<String, String> variable = new HashMap<String, String>();
		variable.put("appid","1140");
		variable.put("token","859EE0413BC3F88F1F89C162800FB056");

		requestConfig.put("variable", variable);
		requestConfig.put("config", innerConfig1);
		
		requests1.put("memInfo", requestConfig);
		
		Map<String, Object> config1= new HashMap<String, Object>();
		config1.put("requests", requests1);
		Step step1 = new Step.Builder().read(config1);
		step1.setName( "step1");
		
		// Step2
		Map<String, Map> requests2 = new HashMap<String, Map>();
		Map <String ,Object>requestConfig2 = new HashMap<String, Object>(); 
		Map <String ,Object>innerConfig2 =  new HashMap<String, Object>();
		innerConfig2.put("url","http://172.25.62.58:8080/coupon-services/rest/couponServices/queryCoupons_h5order");
		headers = new HashMap<String ,Object>();
		headers.put("X-AUTH-HEADER", "F09854B5B98220CAAE4E1F7DD8CAF94A");
		headers.put("Content-Type", "application/json; charset=UTF-8");
		innerConfig2.put("headers", headers);
		
		innerConfig2.put("pathMapping", "{\n" + 
				"  \"pathMappings\": [{\n" + 
				"    \"source\": \"$.step1.requests.request1\",\n" + 
				"    \"target\": \"$.memId\"\n" + 
				"  }]\n" + 
				"}");
		requestConfig2.put("type", InputType.REQUEST.toString());
		requestConfig2.put("config", innerConfig2);
		
		requests2.put("coupon", requestConfig2);
		requests2.put("request4", requestConfig2);
		
		Map<String, Object> config2= new HashMap<String, Object>();
		config2.put("requests", requests2);
 
		Step step2 = new Step.Builder().read(config2);
		step2.setName( "step2");
		Pipeline pipeline = new Pipeline();
		pipeline.addStep(step1);
		pipeline.addStep(step2);
		
		Map<String,Object> input = new HashMap<>();
		input.put("url", "/hello");
		input.put("method", "POST");
		Map<String,Object> inputHeaders = new HashMap<String,Object>();
		inputHeaders.put("traceId", "afdfdsfdsfdsfdsfs");
		input.put("headers", inputHeaders);
		Map<String,Object> requestBody = new HashMap<String,Object>();
		input.put("requestBody", requestBody);
		
		requestBody.put("clientInfo",null);
	    requestBody.put("memberId",0);
	    requestBody.put("memberType",1);
	    requestBody.put("channelCode","CA00093");
	    requestBody.put("bkMemberType",0);
	    requestBody.put("traceId","sZjq5Kdw-181818144");
	    requestBody.put("assetReload",false);
	    requestBody.put("languageCode","0");
	    requestBody.put("innId","JJ1090");
	    requestBody.put("beginDate","2020-03-12");
	    requestBody.put("guests",2);
	    requestBody.put("children",0);
	    requestBody.put("days",1);
	    requestBody.put("mustPay",false);
	    requestBody.put("brandId","JINJIANG");
	    requestBody.put("roomTypeId",null);
	    requestBody.put("endDate","2020-03-13");
	    requestBody.put("memberActCode",null);
	    requestBody.put("sourceType",null);
	    requestBody.put("roomRateCode",null);
	    requestBody.put("timeZone","GMT+8");
	    requestBody.put("cityCode","AR04567");
	    requestBody.put("couponId",null);
	    requestBody.put("excludeSecenery",false);
	    requestBody.put("excludeSoldOut",false);
	    requestBody.put("rooms",1);
	    requestBody.put("canBizBook",false);
	    requestBody.put("dayUse",0);
	    requestBody.put("yaoKaFlag",false);
	    requestBody.put("primeFree",false);
	    requestBody.put("addPrice",false);
	    requestBody.put("busyRoom",0);
		

		
		try {
			// TODO add complete request including headers or something other
			
			Mono<?>result = pipeline.run(null, null);
			result.block();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	 



}
