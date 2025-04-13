## ✨ Assignment: Enhancing Wrangler with Byte Size and Time Duration Units

## Enhancing Wrangler with Byte Size and Time Duration Units

### Overview
This enhancement adds native support to the Wrangler library for parsing and working with byte size (e.g., "10KB", "1.5MB") and time duration (e.g., "150ms", "2.1s") units. It introduces new token types and a custom directive (`aggregate-stats`) that performs aggregation on these values across rows.

### New Token Types
- **BYTE_SIZE**
- **TIME_DURATION**

These tokens allow users to input sizes and durations in familiar string formats, which are then parsed and normalized into bytes or nanoseconds respectively.

### Implementation Summary

#### 1. Grammar Update (`Directives.g4`)
- Added lexer rules: `BYTE_SIZE`, `TIME_DURATION`
- Added fragments: `BYTE_UNIT`, `TIME_UNIT`
- Integrated these into parser rules.

#### 2. API Changes (Module: `wrangler-api`)
- Added `ByteSize.java` and `TimeDuration.java` classes in `io.cdap.wrangler.api.parser`
  - Implements parsing logic.
  - Converts values to canonical units (bytes and nanoseconds).
- Updated `TokenType.java` to include `BYTE_SIZE` and `TIME_DURATION`

#### 3. Core Parser Logic (Module: `wrangler-core`)
- Added visit methods to support `BYTE_SIZE` and `TIME_DURATION` tokens in `io.cdap.wrangler.parser`.
- Modified the `toString()` method in `UsageDefinition.java` to handle optional and new custom token types, ensuring proper usage string generation.

#### 4. New Directive: `AggregateStats`
Location: `wrangler-core\src\main\java\io\cdap\directives\aggregates\AggregateStats.java`
- Accepts arguments:
  - `source_size_column`, `source_time_column`, `target_size_column`, `target_time_column`
  - Optional: `size_unit` (KB, MB), `time_unit` (s, ms)
- Performs aggregation:
  - Totals byte size and duration values.
  - Converts final results to requested output units.

#### 5. Testing (Module: `wrangler-core`)
- **Unit Tests**
  - For `ByteSize` and `TimeDuration` parsing: values like "10kb", "2.5MB", "150ms", "3s".
  - For `AggregateStats`: total byte size and time over a list of rows.
- **Integration Tests**
  - Using `TestingRig` to run the `aggregate-stats` directive as a recipe:
    ```java
    String[] recipe = {
      "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec MB s"
    };
    List<Row> results = TestingRig.execute(recipe, rows);
    Assert.assertEquals(expectedValue, results.get(0).getValue("total_size_mb"), 0.001);
    ```

### Build & Test
- Ensure Maven is installed.
- Run:
  ```bash
  mvn clean install -Ddataset.rewrite.enabled=false
  ```
- All tests should pass (except any legacy dataset rewrite errors).

### AI Tooling Prompts
All prompts used with AI tools during development (e.g., ChatGPT) are documented in `prompts.txt` in the repository root.

### Repository Setup
- Forked from: [https://github.com/data-integrations/wrangler](https://github.com/data-integrations/wrangler)
- Branch: `enhancement-byte-time-support`

### File Modifications Summary
- **Grammar**: `Directives.g4`
- **API**: `ByteSize.java`, `TimeDuration.java`, `TokenType.java`
- **Directive**: `AggregateStats.java`
- **Tests**: `ByteSizeTest.java`, `TimeDurationTest.java`, `StatsAggregateTest.java`