# To-do

- [ ] checkstyles
- [ ] spotbugs
- [ ] javadoc
- [ ] cobertura

## Using the Form URI encoded Serialization implementations

1. In `build.gradle` in the `repositories` section:

    ```Groovy
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }  
    ```

1. In `build.gradle` in the `dependencies` section:

    ```Groovy
    implementation 'com.microsoft.kiota:microsoft-kiota-serialization-form:0.0.1-SNAPSHOT'
    ```

