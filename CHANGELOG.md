# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [Unreleased]

### Added

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
