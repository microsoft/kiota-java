# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.3.0] - 2024-08-22

### Changed

- Ensure interceptors don't drain request body stream before network call [#2037](https://github.com/microsoftgraph/msgraph-sdk-java/issues/2037)

## [1.2.0] - 2024-08-09

### Changed

- Adds bundle package for Kiota [#1420](https://github.com/microsoft/kiota-java/issues/1420).
- Continuous Access Evaluation is now enabled by default for Azure Identity.

## [1.1.14] - 2024-06-10

### Changed

- Fixed a bug where `Double` instances in the `additionalData` would lead to failed serialization with an `IllegalStateException`.

## [1.1.13] - 2024-05-31

### Changed

- Fixed a bug where Parsable instances in the `additionalData` would lead to failed serialization with an `IllegalStateException`. [microsoftgraph/msgraph-sdk-java#1969](https://github.com/microsoftgraph/msgraph-sdk-java/issues/1969)

## [1.1.12] - 2024-05-21

### Changed

- Fixed a bug where large responses would make the client fail. [microsoftgraph/msgraph-sdk-java#2009](https://github.com/microsoftgraph/msgraph-sdk-java/issues/2009)

## [1.1.11] - 2024-05-07

### Changed

- Downgraded jakarta annotation api dependency from 3.0.0 to 2.1.1 for Java 8 backward compatibility

## [1.1.10] - 2024-05-06

### Changed

- Fixing a bug where the type associated the opentelemetry metric attribute `server.port` was defined as a string instead of a long. [#1241](https://github.com/microsoft/kiota-java/issues/1241)

## [1.1.9] - 2024-04-26

### Changed

- Normalize UUID path/query parameter values to string

## [1.1.8] - 2024-04-25

### Changed

- Fixed a bug where options could not be added to request information. [#1238](https://github.com/microsoft/kiota-java/issues/1238)

## [1.1.7] - 2024-04-23

### Changed

- Fixes performance bottleneck when using the backing store due to unnecessary subscription invocations.

## [1.1.6] - 2024-04-18

### Changed

- Remove the OpenTelemetry `-alpha` dependencies to avoid classpath issues.

### Added

### Changed

## [1.1.5]

### Changed

- Fixed exception thrown when setting content length on stream request bodies.

## [1.1.4] - 2024-04-04

### Added

- Introduces a `filename` directive in the `MultipartBody`.

### Changed

- Replaces `@Nullable` annotations to `@Nonnull` in the `BaseRequestConfiguration`.

## [1.1.3] - 2024-04-02

### Changed

- Fixes a bug in the seriliazer that would `IllegalStateException` for json arrays in the additional data.

## [1.1.2] - 2024-03-26

### Changed

- Fixes a bug in the InMemoryBackingStore that would not leave out properties in nested IBackedModel properties.

## [1.1.1] - 2024-03-20

### Changed

- Fixed a bug where not providing scopes to `AzureIdentityAccessTokenProvider` failed with `UnsupportedOperationException` when attempting to fetch the token. [microsoftgraph/msgraph-sdk-java#1882](https://github.com/microsoftgraph/msgraph-sdk-java/issues/1882)

## [1.1.0] - 2024-02-14

### Added

- Adds support for untyped nodes.

## [1.0.6] - 2023-03-04

### Changed

- Fixed a regression with the content length request header from 1.0.5.

## [1.0.5] - 2023-02-28

### Changed

- Added contentLength property to RequestInformation to facilitate in setting the content length of the Okhttp3 RequestBody object within the OkhttpRequestAdapter.

## [1.0.4] - 2024-02-26

### Changed

- Fixed a bug where regex would fail to compile on an Android runtime. [microsoftgraph/msgraph-sdk-java#1851](https://github.com/microsoftgraph/msgraph-sdk-java/issues/1851)

## [1.0.3] - 2024-02-21

### Changed

- Fixed compatibility with Java 8 by replacing `isBlank` with `Compatibility.isBlank`

## [1.0.2] - 2024-02-13

### Changed

- Add default UTC offset when deserializing to OffsetDateTime fails due to a missing time offset value.

## [1.0.1] - 2024-02-09

### Changed

- Allow authentication for localhost HTTP urls

## [1.0.0] - 2024-02-07

### Changed

- Release 1.0.0 of the Kiota Java Libraries as part of Java-SDK GA release.
- Map `XXX` error status code range to Parsable Exception object if more specific error status code range is not found.

## [0.12.2] - 2024-02-01

### Changed

- Removed methods using reflection from `KiotaSerialization`
- Improve `AllowedHostsValidator` to throw an error if `https://` or `http://` prefix is present in a allowed host value.

## [0.12.1] - 2024-01-10

### Changed

- Fixed a bug when handling null QueryParameters instances

## [0.12.0] - 2024-01-10

### Changed

- [breaking] Removed the reflective extraction of Query Parameters in favor of plain methods invocations

## [0.11.2] - 2023-12-14

### Changed

- Fixed a bug where the URI replacement middleware would mangle base64 encoded IDs.

## [0.11.1] - 2023-12-14

### Changed

- Fixed a bug where trying to get a child node for a non exsiting property in JSON would fail instead of returning null.

## [0.11.0] - 2023-12-06

### Changed

- [breaking] Removed the usage of reflection in `ApiClientBuilder`

## [0.10.0] - 2023-11-22

### Changed

- Added Spotless as an automatic formatting tool for the entire codebase
- Changed some internal implementations of JsonParse for performance and readability reasons
- [breaking] Removed the usage of reflection for enum deserialization and reordered `RequestAdapter` arguments order

## [0.9.2] - 2023-11-16

### Changed

- Reviewed transitive dependencies removing Guava and Javatuple

## [0.9.1] - 2023-11-13

### Changed

- Fixed a bug where path or query parameters of enum types would not be serialized properly. [microsoft/kiota#3693](https://github.com/microsoft/kiota/issues/3693)

## [0.9.0] - 2023-11-10

### Added

- Added helper methods to request information to reduce the amount of generated code. [Kiota #3651](https://github.com/microsoft/kiota/issues/3651)

### Changed

- Kiota-Java has moved away from Async/Completable futures, thus Async components are no longer utilized and have been removed. Furthermore, requestAdapter methods no longer use the async suffix. [#175](https://github.com/microsoft/kiota-java/issues/175)
- ApiException class now extends RuntimeException instead of Exception.
- Changed OkHttpRequestAdapter dependency from OkHttpClient to Call.Factory (parent interface implemented by OkHttpClient).

## [0.8.0] - 2023-10-31

### Added

- Added a default implementation of `BasicAccessAuthenticationProvider`
- Added helper methods to serialize kiota models. [microsoft/kiota#3406](https://github.com/microsoft/kiota/issues/3406)

## [0.7.8] - 2023-10-13

### Fixed

- Fixed a bug to preserve the user defined error instead of converting it to generic ApiException.

## [0.7.7] - 2023-10-12

### Added

- Added an overload method to specify the content type of stream request body.

## [0.7.6] - 2023-10-09

### Changed

- Use `tryAdd` instead of `add` in all of the `setContentFrom...` methods.

## [0.7.5] - 2023-10-04

### Added

- Added a `tryAdd` method to the `RequestHeaders` Map

### Changed

- Better encapsulation of the mutable field `responseStatusCode` in `ApiException`

## [0.7.4] - 2023-09-08

### Fixed

- Fixed a regression where query parameters name replacement would fail with group like regex syntax ($ sign)

## [0.7.3] - 2023-09-04

### Fixed

- Fixed bug that caused the ParametersNameDecodingHandler to decode query parameter values in addition to names

## [0.7.2] - 2023-09-01

### Changed

- Swapped custom implementation of RFC6570 URI templates to std uritemplates.

## [0.7.1] - 2023-08-21

### Changed

- Add PeriodAndDuration constructor to create new object from a PeriodAndDuration object.

## [0.7.0] - 2023-08-18

### Added

- Added headers inspection option and handler.

## [0.6.0] - 2023-08-08

### Changed

- Javax annotations replaced in favor of Jakarta annotations.

## [0.5.0] - 2023-07-26

### Added

- Added support for multipart form data request bodies.

## [0.4.7] - 2023-07-21

### Added

- Adds the `UrlReplaceHandler` middleware to the Okhttp component to allow for customizing the URL before sending the request.

## [0.4.6] - 2023-07-20

### Added

- Adds the `PeriodAndDuration` type to aggregate `Period` and `Duration` serialization

### Changed

- Drops the `getPeriodValue` function in favour of `getPeriodAndDurationValue` in the serialization interface.
- Drops the `writePeriodValue` function in favour of `writePeriodAndDurationValue` in the serialization interface.

## [0.4.5] - 2023-06-27

### Changed

- Fixed a bug where composed types would not serialize properly.

## [0.4.4] - 2023-06-09

### Added

- Fix a bug where the OkHttp client would close InputStream responses before they reach the user code

## [0.4.3] - 2023-04-17

### Added

- Adds responseHeader to APIException class

## [0.4.2] - 2023-03-31

### Added

- Adds a NativeResponseHandler to abstractions.
- Adds setResponseHandler method to RequestInformation class in abstractions.

## [0.4.1] - 2023-03-29

### Changed

- Changed the visibility of one of the base constructors in request builders.

## [0.4.0] - 2023-03-22

### Added

- Added a base request builder and request configuration class to reduce the amount of code being generated.

## [0.3.3] - 2023-03-20

### Changed

- Aligns default http client timeout to be 100 seconds
- Updates the JsonParseNodeFactory to pass a JsonElement using `JsonParser.parseReader` rather than creating a string when creating the root parseNode.

## [0.3.2] - 2023-03-16

### Added

### Changed

- Fix add an empty body when method is POST/PUT/PATCH and the body is null

## [0.3.1] - 2023-03-08

# Added

- Adds ResponseStatusCode property to API exception class

## [0.3.0] - 2023-02-21

### Changed

- Adds support for serializing collections of primitive values in form serialization writer and form parse node

## [0.2.1] - 2023-01-13

### Changed

- Fix #165 incorrect/missing substitutions of `queryParameters` after `pathParameters` and other edge cases

## [0.2.0] - 2023-01-17

### Changed

- Removed defaults specific to Microsoft Graph for Azure Identity authentication library.

## [0.1.2] - 2023-01-06

# Added

- Added a method to convert abstract requests to native requests in the request adapter interface.

## [0.1.1] - 2023-01-06

### Added

- Adds a user agent handler to add the product to the header.

## [0.1.0] - 2023-01-05

### Changed

- Prevent empty values from showing up in query parameters values.
- restructure to use gradle multi-modules
- using a single version for all of the components

## [0.0.11] - 2022-12-15

### Added

- Added support for multi-valued request headers
- [serialization/form] Initial release of the library

## [0.0.10] - 2022-10-28

### Changed

- Fixed a bug where collections bodies with a single entry would not serialize properly.

## [0.0.9] - 2022-10-18

### Added

- Added API key authentication provider.
- [http/okHttp] Updated reference to abstractions for multi-valued header support.

## [0.0.8] - 2022-10-11

### Added

- Added enum top level methods in request adapter interface.

## [0.0.7] - 2022-10-10

### Added

- Added ResponseHandlerOption class.

### Changed

- Removed responseHandler parameter from RequestAdapter sendAsync methods.
- Compatibility for Android level 26.

## [0.0.5] - 2022-09-15

### Added

- Added support for tracing through open telemetry

## [0.0.4] - 2022-09-15

### Added

- [http/okHttp] Added support for additional status codes

### Changed

- Lowered compatibility requirements to Java 8.

## [0.0.3] - 2022-09-02

### Added

- Added support for composed types serialization.

## [0.0.2] - 2022-08-11

### Added

- Adds tests to verify DateTime and DateTimeOffsets default to ISO 8601.
- Adds check to throw IllegalStateException when the baseUrl path parameter is not set.

## [0.0.1] - 2022-06-28

### Added

- Initial release on snapshot feed.
