# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [Unreleased]

### Added

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
