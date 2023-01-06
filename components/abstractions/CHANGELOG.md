# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

## [0.0.12] - 2023-01-06

### Changed

- Prevent empty values from showing up in query parameters values.

## [0.0.11] - 2022-12-15

### Added

- Added support for multi-valued request headers

## [0.0.10] - 2022-10-28

### Changed

- Fixed a bug where collections bodies with a single entry would not serialize properly.

## [0.0.9] - 2022-10-18

### Added

- Added API key authentication provider.

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
