// 聚合接口配置
var aggrAPIConfig = {
    name: "input name", // 自定义的聚合接口名  
    config: {
        type: "REQUEST", // 类型，REQUEST/MYSQL
        method: "GET/POST",
        path: "/aggr-hotel/hotel/rates", // 格式：分组名+路径， 分组名以aggr-开头，表示聚合接口
        headers: { // 
            "a": "b"
<<<<<<< HEAD
        }, 
        fields:[ // 定义聚合接口参数
            {
                field: "userId",
                name: "用户名",
                desc: "描述",
                type: "string",
                required: true,
                validates: [
                    // TODO
                ]
            }
        ],
=======
        },
        requestBodySchema: { // 定义聚合接口参数,使用JSON Schema规范，详见：http://json-schema.org/specification.html
            type:"object",
            properties:{
                userId:{
                    type:"string",
                    title:"用户名",
                    description:"描述"
                }
            },
            required: ["userId"]
        },
>>>>>>> 53c1e0624575fb885a2fba590124bd0e505346cd
        dataMapping: {// 聚合接口数据转换规则
            request:{
                script: { // 校验聚合入参是否合法
                    type: "", // groovy
                    source: "",
                    variables: { // 环境变量
                    	
                    }
                }
            },
            response:{
            	fixedBody: { // 固定的body
            		"a":"b"
                },
                fixedHeaders: {// 固定header
                	"a":"b"
                },
            	headers: { // 引用的header
                    "abc": "step1.requests.request1.headers.xyz"
                },
                body: { // 引用的header
                    "abc": "step1.requests.request1.response.id",
                    "inn.innName": "step1.requests.request2.response.hotelName"
                },
                script: {
                    type: "", // groovy
                    source: "",
                    variables: { // 环境变量
                    	
                    }
                    
                }
            }
        },
        stepConfigs: [{ // step的配置
            name: "step1", // 步骤名称
            stop: false, // 是否在执行完当前step就返回
            dataMapping: {// step response数据转换规则
                response: { 
                	fixedBody: { // 固定的body
                    	"a":"b"
                    },
                    body: { // step result
                        "abc": "step1.requests.request1.response.id",
                        "inn.innName": "step1.requests.request2.response.hotelName"
                    },
                    script: {
                        type: "", // groovy
                        source: "",
                        variables: { // 环境变量
                        	
                        }
                    }
                }
            }, 
            requests: { //每个step可以调用多个接口
                request1: // 自定义的接口名
                {
                    type: "REQUEST", // 类型，REQUEST/MYSQL
                    config: {
                        url: "http://baidu.com", // 
                        method: "GET", // GET/POST, default GET
                        connectTimeout: 1, // second
                        readTimeout: 3, // second
                        writeTimeout: 3, // second
                        condition: {
                            type: "", // groovy
                            source: "return \"ABC\".equals(variables.get(\"param1\")) && variables.get(\"param2\") >= 10;", // 脚本执行结果返回TRUE执行该接口调用，FALSE不执行
                            variables: { // 环境变量
                                "param1": "input step1.requests.request2.response.body.user", // value前缀"input "开头，参数值从StepContext获取
                                "param2": 10 // 常量
                         },
                        fallback: {
                            mode: "stop|continue", // 当请求失败时是否继续执行
                            defaultResult: "" // 当mode=continue时，可设置默认的响应报文(json string)
                        },
                        dataMapping: { // 数据转换规则
                            request:{
                            	fixedBody: {
                                	
                                },
                                fixedHeaders: {
                                	
                                },
                                fixedParams: {
                                	
                                },
                                headers: {
                                    "abc": "step1.requests.request1.headers.xyz"
                                },
                                body:{
                                    "inn.innId": "step1.requests.request1.response.id" // 默认为源数据类型，如果要转换类型则以目标类型+空格开头，如："int "
                                },
                                params:{
                                    "userId": "input.requestBody.userId"
                                },
                                script: {
                                    type: "", // groovy
                                    source: "",
                                    variables: { // 环境变量
                                    	
                                    }
                                }
                            },
                            response: {
                            	fixedBody: {
                                	
                                },
                                fixedHeaders: {
                                	
                                },
                                headers: {
                                    "abc": "step1.requests.request1.headers.xyz"
                                },
                                body:{
                                    "inn.innId": "step1.requests.request1.response.id"
                                },
                                script: {
                                    //type: "", // groovy
                                    source: "",
                                    variables: { // 环境变量
                                    	
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }]
    }

}



var stepContext = {
    // input data
    input: {
        request:{
            url: "",
            method: "GET/POST",
            headers: {},
            body: {},
            params: {}
        },
        response: { // 聚合接口的响应
            headers: {},
            body: {}
        }
    },

    // step name
    stepName: {
        // step request data
        requests: {
            request1: {
                request:{
                    url: "",
                    method: "GET/POST",
                    headers: {},
                    body: {}
                },
                response: {
                    headers: {},
                    body: {}
                }
            },
    
            request2: {
                request:{
                    url: "",
                    method: "GET/POST",
                    headers: {},
                    body: {}
                },
                response: {
                headers: {},
                    body: {}
                }
            }
            //...
        },

        // step result 
        result: {}

    }

}