# emblock-sdk-java: Emblock.co SDK for Java

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/co.emblock/emblock-sdk-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/co.emblock/emblock-sdk-java)

emblock-sdk-java is a sdk for the Emblock.co platform.

Please check out our [website](https://emblock.co) before using our sdk.

## Table of Contents

- [Installing](#installing)
- [API docs](#api-docs)
- [Usage examples](#usage-examples)
  - [Creating an instance of EmblockClient](#creating-an-instance-of-emblockclient)
  - [Calling a constant function or get a state value](#calling-a-constant-function-or-get-a-state-value)
  - [Calling a function](#calling-a-function)
  - [Listening to events](#listening-to-events)
- [Changelog](#changelog)
- [License](#license)


## Installing

Download the [latest JAR](https://search.maven.org/remote_content?g=co.emblock&a=emblock-sdk-java&v=LATEST) or configure this dependency:
 
- gradle
```
implementation "co.emblock:emblock-sdk-java:<latest>"
```

- maven
```
<dependency>
  <groupId>co.emblock</groupId>
  <artifactId>emblock-sdk-java</artifactId>
  <version>[LATEST_VERSION]</version>
  <type>pom</type>
</dependency>
```

## Usage examples

### Creating an instance of EmblockClient

```java
EmblockClient emblockClient = new EmblockClient("<API_KEY>", "<PROJECT_ID>");
```

### Calling a constant function or get a state value
```java
Map<String, String> params = new HashMap<>();
emblockClient.callConstant("balanceOf", params, (results, e) -> {
    if (e == null) {
        for (ConstantResult result : results) {
            System.out.println("result name=" + result.getName() + " value=" + result.getValue() + " type=" + result.getType());
        }
    } else e.printStackTrace();
});
```

### Calling a function

We are calling the transfer function of an ERC-20 smart contract.

```java 
String sender = "0xF923c87B3C143a44053C44904724C03CC704342e"; // address of the sender
Map<String, String> params = new HashMap<>();
params.put("to", "0x905C22656bB3a2BC457Ac8AA4131264b89D59e91");
params.put("value", "100");
emblockClient.callFunction(sender, "transfer", params, (success, e) -> {
    if (e == null) {
        if (success) {
            getBalance(emblockClient);
        } else {
            System.out.println("function call failed");
        }
    }
});
```

### Listening to Events

Listening to events emitted by your smart contract.

```java 
emblockClient.addEventsListener((eventName, params, e) -> {
    if (e == null) {
        System.out.println("eventName=" + eventName);
        for (Param param : params) {
            System.out.println("param type=" + param.getType() + " value=" + param.getValue());
        }
    } else e.printStackTrace();
});
```


## Changelog

We're using the GitHub [releases][changelog] for changelog entries.

## License

[MIT](LICENSE)

