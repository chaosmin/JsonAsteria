# JsonAsteria

English | [简体中文](README.md)

Read original json message and generate a new JSON defined in schema.

## Introduction

When dealing with third-party format json messages (such as mobile phone reports, etc.), we usually encounter such problems as json format is not fixed, json path does not exist or is empty, filling value compensation and so on. To solve these problems, we build this project to handle these possible situations.

This tool **mainly** use [Jackson](https://github.com/FasterXML/jackson-databind) and [JsonPath](https://github.com/json-path/JsonPath) as the parsing and generating tools. The specific usage of these tools can refer to the corresponding official documents.

## Usage

Using `ParserFactory`to create a format parser class, and call `execute` method to parse the original json message. The execute result is a `JsonNode` defined in `jackson` package, which can be processed and modified accordingly.

## Schema Definition

### example

```json
[
  {
    "name": "name",
    "type": "string",
    "jsonPath": "$.name",
    "doc": "name of person"
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
        "doc": "name of school"
      },
      {
        "name": "math-score",
        "type": "int",
        "isArray": true,
        "jsonPath": "$.graduated[*].score[?(@.name=='math')].score",
        "optional": false,
        "default": [],
        "doc": "score of math project"
      }
    ]
  }
]
```

Definition support hierarchical structure nesting, the minimum structure is:

```json
{
   "name": "name",
   "type": "string",
   "jsonPath": "$.name"
}
```

Among them, `name` is the key value of the converted json message; `type` define the javaType of the value in the message, which currently supports *string*, *int*, *long*, *double*, *Boolean*, *date*, and the other types will be converted to *Object* uniformly; `jsonPath` is the value path point of the original json message, using the open source project `jsonPath` to fetch data.

The most complete structure is:

```json
{
	"name": "math-score",
	"type": "int",
	"isArray": true,
 	"jsonPath": "$.graduated[*].score[?(@.name=='math')].score",
 	"optional": false,
 	"default": [],
 	"doc": "socre of math project"
 }
```

Where `isArray` identifies whether the parsed result of the original message needs to be stored using `List` , default *false*, when `isArray` is *false* and the parsed result of the original message is an array, the default value is the first value of the node; `optional`  identifies whether the node value is allowed to be null, and `optional`. When  is *true* and the node's analytical value and default value are null, the final result of the node will be null. When `optional` is *false* and the node's analytical value and default value are null, the final result of obtaining the node will throw `RuntimeException('illegal value')` exception; the `default`  field represents the node. When the node resolution value is null, the default value will be attempted to be returned as the final result; `doc` is a supplementary description of the node.

When we use the above example schema to parse the following original message, we will get the following results:

Original json message:

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

parse result:

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
