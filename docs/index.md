# index

## TODO

* Document default priming

## Usage

### Starting JZONbie

JZONbie can be started either as an embedded server within any JVM application, or as a standalone process.

#### Embedded JZONbie

A JZONbie instance can be started within a JVM application by using the Jzonbie class. This JZONbie instance can be configured with options for port, zombie header name, additional routes \(both explained later\), and a custom object mapper to be used for deserialization. There is also an option to define a duration to wait after calling stop on the JZONbie instance. This may be required where multiple JZONbie instances are created in quick succession using the same port, which can start a JZONbie instance that is not ready to handle traffic instantly. In most cases of JZONbie usage, this option should not be required.

```java
// Default Jzonbie: Random port, zombie header name 'zombie', and default object mapper  
final Jzonbie defaultJzonbie = new Jzonbie();

// Jzonbie with port 8080
final Jzonbie customPort = new Jzonbie(options().withPort(8080));

// Jzonbie that blocks for 1 second after stopping
final Jzonbie customPort = new Jzonbie(options().withWaitAfterStopping(Duaration.ofSeconds(1)));
```

#### Standalone JZONbie

When running as a standalone server, it is recommended to use the JZONbie docker container. This can be found on Docker Hub [here](https://hub.docker.com/r/jonnymatts/jzonbie/). The docker image can also be built locally by cloning the repository and running the following command.

```bash
./gradlew clean docker
```

The standalone server within the docker container will default to starting on port 8080 with zombie header `zombie`. These can be configured by setting the enviroment variables `JZONBIE_PORT` and `JZONBIE_PORT` respectively within the container.

```bash
# Runs latest JZONbie with server started within container on port 30000 mapped to port 8080 on the host
docker run -p 8080:30000 -e JZONBIE_PORT=30000 jonnymatts/jzonbie:latest
```

#### JUnit Rule

When you are using the embedded JZONbie instance within a test, you must initialize and destroy it yourself. Instead of handling this yourself you can use the JzonbieRule JUnit rule. An example usage is shown below.

```java
@Rule public JzonbieRule jzonbie = JzonbieRule.jzonbie();
```

This example will create a jzonbie instance with the default options before, and stop the instance after each test case. The JZONbie instance can be configured with the same options the standard JZONbie is configured with. You can create a JZONbie instance to be used for all test cases by using the `@ClassRule` annotation, though you will need to reset the instance before each test is ran.

```java
@ClassRule public static JzonbieRule jzonbie = JzonbieRule.jzonbie();

// Reset JZONbie before each test
@Before
public void setUp() {
    jzonbie.reset();
}
```

### Adding Additional Routes

One option only available when using the embedded JZONbie instance is the ability to add custom handlers for additional routes. These custom handlers have access to the JZONbie instance, a Deserializer \(and the underlying ObjectMapper for serialization\), and both the request and response. An simple example of a custom handler is shown below:

```java
final JzonbieRoute readyRoute = get("/ready", ctx -> ctx.getRouteContext().getResponse().ok());
```

The route above will configure the JZONbie to always return a `200(OK)` response. With access to the Deserializer and JZONbie instance, much more complex routes can be added, such as a custom priming endpoint that receives your own domain objects and transforms them.

One caveat to adding a custom route is that it will always ignore any priming on that route.

### Stubbing

The main usage of JZONbie is the stubbing of external services required by your application within integration tests.

#### Embedded Stubbing

To prime JZONbie using the embedded instance, a request and response must first be created. This can be done easily using the included builders as shown below.

```java
final AppRequest appRequest = AppRequest.builder("POST", "/blah")
    .withBody(singletonMap("one", 1))
    .build();

final AppResponse appResponse = AppResponse.builder(200)
    .contentType("application/json")
    .withBody(singletonMap("message", "Well done!"))
    .build();
```

There are builders for common request methods and response codes, allowing the above to be rewritten.

```java
final AppRequest appRequest = post("/blah")
    .withBody(singletonMap("one", 1))
    .build();

final AppResponse appResponse = ok()
    .contentType("application/json")
    .withBody(singletonMap("message", "Well done!"))
    .build();
```

**App Requests**

For app requests, the required fields are the HTTP method and path of the expected request. This request mapping will map any requests matching against these two fields. This request can then become more specific by specifying headers, query parameters and a request body.

There are multiple places in the request priming where regex values can be used. These include in header keys and values, query param keys and values, and in JSON object string keys and values.

The app request \(and response\) body is primed with a value of type BodyContent. There are multiple variations of BodyContent including:

```java
// Body content representing a JSON object, taking in a map
final ObjectBodyContent object = objectBody(singletonMap("key", "val"));

// Body content representing a JSON array, taking in a list
final ArrayBodyContent array = arrayBody(asList("val1", "val2"));

// Body content representing a JSON string, taking in a string
final StringBodyContent string = stringBody("randomString");

// Body content representing a literal, taking in a string, number or boolean
final LiteralBodyContent string = literalBody("<xml>val</xml>");
```

Each of these body contents can be inferred from the input type to the `withBody` method on each of the builders, except for string body content which must be explicit.

**App Responses**

For app responses, the only required field is the status code. The requests can also optionally be primed with headers and a response body. Responses can also be primed to respond with a delay, which takes a Duration.

Once the app request and response have been created, they can then be used to prime the JZONbie instance.

```java
// Using the request and response objects defined above
jzonbie.prime(request, response);
```

The above snippet will prime the jzonbie to respond with a 200 response with a JSON object body for a POST request to `/blah` with a JSON object request body. Once this primed request has been matched and the corresponding request has been returned, the priming will be removed from the JZONbie instance.

As this may not be the desired functionality, JZONbie can also be primed to respond with a default response for a given request.

```java
// Using the request and response objects defined above
jzonbie.prime(request, staticDefault(response));
```

This will prime the zombie to return the default response whenever the primed request is matched. If the JZONbie is primed with a standard response while also primed with a default response, when the primed request is matched the standard priming will always be consumed first.

In addition to the StaticDefaultResponse used above, there is also a DynamicDefaultResponse that can be primed.

```java
// Infinite integer iterator
Iterator<Integer> iterator;

// Using the request and response objects defined above
jzonbie.prime(request, dynamicDefault(() -> response.contentType("application/xml").withBody("<number>" + iterator.next() + "</number>")));
```

This is useful for defining sequences that can be returned for similar primed requests. It's constructor takes a supplier of AppResponse.

**Templating App Responses**

It is possible to populate the headers and bodies of app responses with attributes from the app request using [Handlebars templates](http://handlebarsjs.com/). To use this functionality, a TemplatingAppResponse must be used instead of a standard AppResponse. An example of this shown below.

```java
// Match any GET request with a  number following '/resources/'
final AppRequest request = get("/resources/\\d+").build();

// Return an object with a value of the second segment of the requests path. In this case, the number following '/resources/'
final TemplatedAppResponse templatedResponse = templated(ok().withBody(objectBody(singletonMap("id", "{{ request.pathSegment.[1] }}"))).build());

jzonbie.prime(request, templatedResponse);
```

From the example above, if JZONbie was hit with a request to `/resources/12345` then the response body would be `{"id": "12345"}`. As can be seen, it is trivial to convert an app response into a templating app response, simply by wrapping the response in a call to `templated`. TemplatingAppResponses can be used in place of a standard AppResponse, including in default responses.

**Request Attributes**

The following are all the attributes that can be extracted from the incoming request. Examples are for an incoming `GET` request to `http://jzonbie.example.com:8080/resources/12345/entries?state=ACTIVE` with the request header `"version":"2"`:

| Attribute | Description | Example |
| :--- | :--- | :--- |
| `request.url` | The full URL of the request. | [http://jzonbie.example.com:8080/resources/12345/entries?state=ACTIVE](http://jzonbie.example.com:8080/resources/12345/entries?state=ACTIVE) |
| `request.protocol` | The protocol of the request. | http |
| `request.host` | The hostname of the request. | jzonbie.example.com |
| `request.port` | The port of the request. | 8080 |
| `request.baseUrl` | The URL of the request excluding the path. | [http://jzonbie.example.com:8080](http://jzonbie.example.com:8080) |
| `request.path` | The full path of the request. | /resources/12345/entries |
| `request.pathSegment.[<i>]` | The segment of the requests path, zero-based. | 12345 \(given \ is 1\) |
| `request.queryParam.<paramName>.[<i>]` | The ith value of the query parameter with the name `<paramName>`, zero-based. | ACTIVE \(given \ is state and \ is 1\) |
| `request.header.<headerName>` | The value of the header with the name `<headerName>`. | 2 \(given \ is version\) |
| `request.method` | The method of the request. | GET |
| `request.body` | The body of the request. | "" |

**JsonPath Helper**

The `jsonPath` hepler has been provided to extract JSON values from app requests. For example, given a request with the body:

```javascript
{
  "field": "value"
}
```

the following template will extract `value`:

```text
{{jsonPath request.body '$.field'}}
```

#### Stubbing Over HTTP

To prime the JZONbie using HTTP with the same priming as used above, a request containing the following body must be sent to the server.

```javascript
{
  "request" : {
    "path" : "/blah",
    "method" : "GET",
    "body" : {
            "JZONBIE_CONTENT_TYPE": "J_OBJECT",
            "one" : 1
    }
  },
  "response" : {
    "statusCode" : 200,
    "headers" : {
      "Content-Type" : "application/json"
    },
    "body" : {
      "JZONBIE_CONTENT_TYPE": "J_OBJECT",
      "message" : "Well done!"
    }
  }
}
```

The HTTP method used when sending this can be either POST, PATCH or PUT. It is necessary to put the header `zombie:priming` when sending this request so that JZONbie can see that it is a priming request. This is a usage of the previously mentioned zombie header name.

There are other usages of this header when priming, `priming-template`, `priming-default`, `priming-default-template` and `priming-file`. The `priming-template` header will tell JZONbie to process this response as a template. Using `priming-default` and `priming-default-template` headers will prime the JZONbie to respond with the response as the default for the request, with and without template processing respectively. Currently, this only supports static default responses. The `priming-file` value can be used to prime the JZONbie instance with multiple mappings defined in a file via a multi-part form request. A common use case for this is to prime JZONbie with the same priming from a previous test scenario. Downloading the current mappings into a file will be shown later.

#### Stubbing Using HTTP Client

As trying to stub a JZONbie instance over HTTP can become complicated, a Java client has been provided. The client has the same interface as the embedded JZONbie, and can be used interchangeably.

```java
final File primingfile = new File("/path/to/file");

// Interacts with a local JZONbie instance serving on port 8080
final JzonbieClient client = new ApacheJzonbieHttpClient("http://localhost:8080");

client.prime(file);
```

### Verification

Another integral function of JZONbie is to allow for verification that a request has been called.

#### Verifying Using Embedded JZONbie And HTTP Client

```java
// Verifies that the request defined earlier was received by JZONbie at most 3 times 
jzonbie.verify(request, atMost(3))
```

As well as the `atMost` verification criteria, there are also `equalTo`, `atLeast` and `between`. If the verification fails, a VerificationException is thrown.

#### Verifying Over HTTP

There is no direct way to verify over HTTP. However, sending a request with the zombie header value `count` and a request in the body will return the number of times the JZONbie matched against the given request.

```javascript
{
  "request" : {
    "path" : "/blah",
    "method" : "GET",
    "body" : {
            "JZONBIE_CONTENT_TYPE": "J_OBJECT",
            "one" : 1
    }
  }
}
```

### Other Commands

The other values for the zombie header are: `current`, `current-file`, `history`, `failed`, and `reset`

#### Get Current Mapping

There are two methods for getting the current mapping of the JZONbie, either using the zombie header value `current` or `current-file`. Using `current` will return the list of primed mappings for the JZONbie instance, an example of which is shown below.

```javascript
[ {
  "request" : {
    "path" : "/path",
    "headers" : { },
    "method" : "GET"
  },
  "responses" : {
    "default" : {
      "statusCode" : 200,
      "headers" : {
        "Content-Type" : "application/json"
      },
      "body" : {
        "JZONBIE_CONTENT_TYPE": "J_OBJECT",
        "key" : "val"
      }
    },
    "primed" : [ {
      "statusCode" : 201,
      "headers" : {
        "Content-Type" : "application/json"
      },
      "body" : {
        "JZONBIE_CONTENT_TYPE": "J_OBJECT",
        "key" : "val"
      }
    } ]
  }
} ]
```

Using the following snippet will return the current state of JZONbie using the Java implementations.

```java
final List<ZombieMapping> currentPriming = jzonbie.getCurrentPriming()
```

The `current-file` zombie header value will respond with a file download of the current state in the form above. This is not supported by the Java implementations currently.

#### Get History

Getting the history of the JZONbie will return an ordered list of the request received and responses served by the JZONbie in it's current session. This can be done over HTTP by using the `history` zombie header value.

Using the following snippet will return the call history of the current JZONbie session using the Java implementations.

```java
final List<ZombiePriming> history = jzonbie.getHistory()
```

#### Get Failed Requests

In addition to getting the successful requests received by JZONbie, it is also possible to get the requests for which JZONbie could find nor priming.

Using the following snippet will return the failed requests received during the current JZONbie session using the Java implementations.

```java
final List<AppRequest> failedRequests = jzonbie.getFailedRequests()
```

#### Resetting The Session

The current session state can be cleared from the JZONbie instance by using the `reset` zombie header value over HTTP, or via the following code snippet using the embedded JZONbie or HTTP client:

```java
jzonbie.reset()
```

