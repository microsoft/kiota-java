# To-do


- [ ] checkstyles
- [ ] spotbugs
- [ ] android api level linting
- [ ] javadoc
- [ ] cobertura

## Using the Azure Authentication library

1. In `build.gradle` in the `repositories` section:

    ```Groovy
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }  
    ```

1. In `build.gradle` in the `dependencies` section:

    ```Groovy
    implementation 'com.microsoft.kiota:microsoft-kiota-authentication-azure:0.0.3-SNAPSHOT'
    ```

