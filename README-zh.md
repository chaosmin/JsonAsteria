# Json报文转换工具

[English](README.md) | 简体中文

读取原始JSON报文并根据定义生成新的JSON示例。

## 介绍

当处理第三方json格式报文时(如手机报告等)，我们通常会遇到报文格式不固定，json路径不存在或为空，填充值补偿等问题。为了解决这些问题，我们创建列这个项目，用来处理可能遇到的情况。

该工具主要使用 [Jackson](https://github.com/FasterXML/jackson-databind) 与 [JsonPath](https://github.com/json-path/JsonPath) 作为json报文的主要解析与生成工具，具体使用方式可参考相应的官方文档。

### 作者

| 姓名   | 邮箱                  |
| ------ | --------------------- |
| Romani | minchao@juxiangfen.com |

## 使用方式

使用 `ParserFactory` 创建符合实际情况的 Parser 解析类，调用 `execute` 方法进行原始报文的解析，返回结果为 `jackson` 包中的 `JsonNode`，可根据需要对结果进行相应的加工和修饰。

## 解析报文定义

### 报文示例

```json
[
  {
    "name": "name",
    "type": "string",
    "jsonPath": "$.name",
    "doc": "姓名"
  },
  {
    "name": "graduationSchools",
    "fields": [
      {
        "name": "school-name",
        "type": "string",
        "isArray": true,
        "jsonPath": "$.graduated[*].school-name",
        "optional": false,
        "default": [],
        "doc": "院校名称"
      },
      {
        "name": "math-score",
        "type": "int",
        "isArray": true,
        "jsonPath": "$.graduated[*].score[?(@.name=='math')].score",
        "optional": false,
        "default": [],
        "doc": "数学成绩"
      }
    ]
  }
]
```

报文支持层级结构嵌套，最小结构为：

```json
{
	"name": "name",
	"type": "string",
	"jsonPath": "$.name"
}
```

其中，`name` 为转换后 json 报文的的 key 值；`type` 为报文 value 值的类型，目前支持 *string*，*int*，*long*，*double*，*boolean*，*date*，其余类型将被统一转换为 `Object` 类型；`jsonPath` 为原始报文中的 value 路径点，使用开源项目 `jsonPath` 进行数据的获取。

最全结构为：

```json
{
	"name": "math-score",
	"type": "int",
	"isArray": true,
	"jsonPath": "$.graduated[*].score[?(@.name=='math')].score",
	"optional": false,
	"default": [],
	"doc": "数学成绩"
}
```

其中，`isArray` 标识是否需要将原始报文的解析结果使用 `List` 进行存储，默认 *false*，当 `isArray` 为 *false* 且原始报文解析结果为数组时，将默认取第一个值作为该节点的解析值；`optional` 标识该节点值是否允许为 null，当 `optional` 为 *true* 且节点解析值与默认值都为 null 时，该节点的最终结果将会是 null，当 `optional` 为 *false* 且节点解析值与默认值都为 null 时，获取该节点的最终结果将会抛出 `RuntimeException("illegal value")` 异常；`default` 字段表示该节点的默认值(填充值)，当节点解析值为 null 时，将会尝试使用该默认值作为最终结果返回；`doc` 是对该节点的补充说明。

当我们使用上述示例报文对下面的原始报文进行解析后，将得到如下结果：

原始报文：

```json
{
  "name": "romani",
  "idCard": "300000000000000000",
  "mobile": "18600000000",
  "graduated": [
    {
      "graduation-time": "2013-06-01",
      "school-name": "Xingzhi High School",
      "degree": "high school student",
      "score": [
        {
          "name": "math",
          "score": 100
        },
        {
          "name": "english",
          "score": 100
        }
      ]
    },
    {
      "graduation-time": "2017-06-01",
      "school-name": "Shanghai University of Electric Power",
      "degree": "undergraduate",
      "score": [
        {
          "name": "math",
          "score": 80
        },
        {
          "name": "english",
          "score": 70
        }
      ]
    }
  ]
}
```

解析结果：

```json
{
  "name" : "romani",
  "graduationSchools" : {
    "school-name" : [ "Xingzhi High School", "Shanghai University of Electric Power" ],
    "school-date" : [ "2013-06-01", "2017-06-01" ],
    "math-score" : [ 100, 80 ],
    "english-score" : [ 100, 70 ]
  }
}
```

