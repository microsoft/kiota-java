# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

## [0.0.6] - 2022-10-10

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
