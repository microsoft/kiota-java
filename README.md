# Kiota Java Libraries

[![Release](https://img.shields.io/github/v/release/microsoft/kiota-java)](https://search.maven.org/search?q=g:com.microsoft.kiota%20a:kiota-abstractions)

The Kiota Java Libraries for Java are:

- [abstractions] Defining the basic constructs Kiota projects need once an SDK has been generated from an OpenAPI definition
- [authentication/azure] Implementing Azure authentication mechanisms
- [http/okHttp] Implementing a default OkHttp client
- [serialization/form] Implementing default serialization for forms
- [serialization/json] Implementing default serialization for json
- [serialization/text] Implementing default serialization for text
- [serialization/multipart] Implementing default serialization for multipart

Read more about Kiota [here](https://github.com/microsoft/kiota/blob/main/README.md).

## Using the Libraries

### With Gradle:

In `build.gradle` in the `dependencies` section:

```Groovy
implementation 'com.microsoft.kiota:microsoft-kiota-abstractions:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-authentication-azure:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-http-okHttp:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-serialization-json:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-serialization-text:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-serialization-form:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-serialization-multipart:1.2.0'
implementation 'com.microsoft.kiota:microsoft-kiota-bundle:1.2.0'
implementation 'jakarta.annotation:jakarta.annotation-api:2.1.1'
```

### With Maven:

In `pom.xml` in the `dependencies` section:

```xml
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-abstractions</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-authentication-azure</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-http-okHttp</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-serialization-json</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-serialization-text</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-serialization-form</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>com.microsoft.kiota</groupId>
      <artifactId>microsoft-kiota-serialization-multipart</artifactId>
      <version>1.2.0</version>
    </dependency>
    <dependency>
      <groupId>jakarta.annotation</groupId>
      <artifactId>jakarta.annotation-api</artifactId>
      <version>2.1.1</version>
    </dependency>
```

## Extra

Third party tools and extensions:

- [Extra utilities for Java projects based](https://github.com/redhat-developer/kiota-java-extra) by [RedHat](https://www.redhat.com/). Kiota Maven plugin to ease the usage of the Kiota CLI from Maven projects.

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft
trademarks or logos is subject to and must follow
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion or imply Microsoft sponsorship.
Any use of third-party trademarks or logos are subject to those third-party's policies.
