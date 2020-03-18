// 聚合接口配置
var aggrAPIConfig = {
    name: "input name", // 自定义的聚合接口名  
    config: {
        type: "REQUEST", // 类型，REQUEST/MYSQL
        method: "GET/POST",
        path: "/aggr-hotel/hotel/rates", // 格式：分组名+路径， 分组名以aggr-开头，表示聚合接口
        headers: {
            "a": "b"
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
                headers: {
                    "abc": "step1.requests.request1.headers.xyz"
                },
                body: {
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
                        method: GET, // GET/POST, default GET
                        connectTimeout: 1, // second
                        readTimeout: 3, // second
                        writeTimeout: 3, // second
                        condition: [
                            {
                                // TODO
                            }
                        ],
                        fallback: {
                            mode: "stop|continue", // 当请求失败时是否继续执行
                            defaultResult: "" // 当mode=continue时，可设置默认的响应报文(json string)
                        },
                        dataMapping: { // 数据转换规则
                            request:{
                                headers: {
                                    "abc": "step1.requests.request1.headers.xyz"
                                },
                                body:{
                                    "inn.innId": "step1.requests.request1.response.id"
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
                        }, 
                        headers: {
                            "a": "b"
                        },
                        params: {
                            "a": "b"
                        },
                        body: {

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