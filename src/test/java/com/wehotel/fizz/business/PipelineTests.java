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
		innerConfig1.put("url","http://172.25.33.79/trip-inninfo-api/inn/getHotelInfo");
		innerConfig1.put("METHOD","GET");
		
		Map <String ,Object>requestConfig = new HashMap<String, Object>();
		requestConfig.put("type", InputType.REQUEST.toString());
		requestConfig.put("config", innerConfig1);
		requests1.put("hotelInfo", requestConfig);
		
		innerConfig1 =  new HashMap<String, Object>();
		innerConfig1.put("url","http://172.25.33.79/we-meb/rest/we-meb/api/getMebInfo");
		innerConfig1.put("Method","POST");
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
		innerConfig2.put("url","http://172.25.62.58:8090/coupon-services/rest/couponServices/queryCoupons_h5order");
		headers = new HashMap<String ,Object>();
		headers.put("X-AUTH-HEADER", "F09854B5B98220CAAE4E1F7DD8CAF94A");
		headers.put("Content-Type", "application/json; charset=UTF-8");
		innerConfig2.put("headers", headers);
		
		innerConfig2.put("pathMapping", "{\n" + 
				"  \"pathMappings\": [{\n" + 
				"    \"source\": \"$.step1.request1\",\n" + 
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
		
		Map<String, Object>input = new HashMap<String, Object>();
		input.put("innId", "JJ65001");
		input.put("channelCode", "CA00001");
		input.put("brandId", "");
		
		String reqStr = "{\"clientInfo\":{\"deviceId\":\"D8920140-4843-4E9F-93C1-4B98F5133BFC\",\"os\":\"ios\",\"appVersion\":\"4.0.3\",\"channelId\":\"309488\",\"hardwareModel\":\"iPhone 6 Plus\",\"systemVersion\":\"11.4.1\",\"versionCode\":\"71\"},\"memberId\":0,\"memberType\":9,\"channelCode\":\"CA00001\",\"bkMemberType\":0,\"primeMeb\":false,\"primeLevel\":0,\"traceId\":\"rbsRHc2c-196272484\",\"languageCode\":\"0\",\"brandId\":\"98\",\"innId\":\"XRH000080\",\"roomTypeId\":null,\"beginDate\":\"2019-10-21\",\"endDate\":\"2019-06-22\",\"days\":1,\"mustPay\":false,\"memberActCode\":null,\"guests\":0,\"children\":0,\"sourceType\":null,\"roomRateCode\":null,\"timeZone\":\"GMT+8\",\"cityCode\":\"AR00252\",\"couponId\":null,\"excludeSecenery\":false,\"excludeSoldOut\":false,\"rooms\":1,\"canBizBook\":false,\"dayUse\":0,\"yaoKaFlag\":false,\"primeRate\":false,\"primeFree\":false}";
		
		Map<String, Object> request;
		try {
			request = new ObjectMapper().readValue(reqStr, HashMap.class);
			request.put("memberId", "196272484");
			// TODO add complete request including headers or something other
			
			Mono<?>result = pipeline.run(request);
			result.block();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	 



}
