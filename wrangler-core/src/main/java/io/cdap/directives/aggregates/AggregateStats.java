/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregates;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

/**
 * Aggregates total byte size and time duration from two columns each,
 * outputs a single row with totals.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates total byte size and time duration from "
    + "two columns each outputs a single row with totals.")
public final class AggregateStats implements Directive {

  /** Conversion constant for kilobytes. */
  private static final int KB = 1024;
  /** Conversion constant for megabytes. */
  private static final int MB = 1024 * 1024;
  /** Conversion constant for gigabytes. */
  private static final int GB = 1024 * 1024 * 1024;
  /** Nanoseconds in a microsecond. */
  private static final long NS_IN_US = 1_000L;
  /** Nanoseconds in a millisecond. */
  private static final long NS_IN_MS = 1_000_000L;
  /** Nanoseconds in a second. */
  private static final long NS_IN_S = 1_000_000_000L;
  /** Nanoseconds in a minute. */
  private static final long NS_IN_M = 60 * 1_000_000_000L;

  /** Source size column name. */
  private String sourceSizeCol;
  /** Source time column name. */
  private String sourceTimeCol;
  /** Target size column name. */
  private String targetSizeCol;
  /** Target time column name. */
  private String targetTimeCol;
  /** Desired size unit for conversion. */
  @Nullable
  private String sizeUnit;
  /** Desired time unit for conversion. */
  @Nullable
  private String timeUnit;

  /** {@inheritDoc} */
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder =
        UsageDefinition.builder("aggregate-stats");
    builder.define("source_size_column", TokenType.COLUMN_NAME);
    builder.define("source_time_column", TokenType.COLUMN_NAME);
    builder.define("target_size_column", TokenType.IDENTIFIER);
    builder.define("target_time_column", TokenType.IDENTIFIER);
    builder.define("size_unit", TokenType.TEXT);
    builder.define("time_unit", TokenType.TEXT);
    return builder.build();
  }

  /** {@inheritDoc} */
  @Override
  public void initialize(final Arguments args)
      throws DirectiveParseException {

    System.out.println(">>>>>>> RUNNING UPDATED CODE VERSION - DEBUG MARKER <<<<<<<<");
    sourceSizeCol = ((ColumnName) args.value("source_size_column")).value();
    sourceTimeCol = ((ColumnName) args.value("source_time_column")).value();

    targetSizeCol = ((io.cdap.wrangler.api.parser.Identifier) args.value("target_size_column")).value();
    targetTimeCol = ((io.cdap.wrangler.api.parser.Identifier) args.value("target_time_column")).value();

    sizeUnit = ((io.cdap.wrangler.api.parser.Text) args.value("size_unit")).value();
    timeUnit = ((io.cdap.wrangler.api.parser.Text) args.value("time_unit")).value();

    System.out.println("Target size type: " + args.value("target_size_column").getClass());
    System.out.println("sizeUnit = [" + sizeUnit + "]");
    System.out.println("timeUnit = [" + timeUnit + "]");
  }

  /** {@inheritDoc} */
  @Override
  public List<Row> execute(final List<Row> rows,
      final ExecutorContext context)
      throws DirectiveExecutionException, ErrorRowException {
    long totalSizeBytes = 0;
    long totalTimeNanos = 0;

    for (Row row : rows) {
      Object sizeObj = row.getValue(sourceSizeCol);
      Object timeObj = row.getValue(sourceTimeCol);

      // Debug print
      System.out.println("Row value -> Size: " + sizeObj + ", Time: " + timeObj);

      if (sizeObj instanceof String) {
        long parsedSize = parseByteSize((String) sizeObj);
        System.out.println("Parsed size: " + parsedSize);
        totalSizeBytes += parsedSize;
      }
      if (timeObj instanceof String) {
        long parsedTime = parseTimeDuration((String) timeObj);
        System.out.println("Parsed time: " + parsedTime);
        totalTimeNanos += parsedTime;
      }
    }

    // Debug total sum before conversion
    System.out.println("Total bytes before conversion: " + totalSizeBytes);
    System.out.println("Total nanos before conversion: " + totalTimeNanos);

    double convertedSize = convertByteSize(totalSizeBytes, sizeUnit);
    double convertedTime = convertTimeDuration(totalTimeNanos, timeUnit);
    
    Row result = new Row();
    result.add(targetSizeCol, convertedSize);
    result.add(targetTimeCol, convertedTime);

    return Collections.singletonList(result);
  }

  /**
   * Parses a human-readable byte size string to bytes.
   * @param value the string containing size with unit.
   * @return the size in bytes.
   */
  private long parseByteSize(final String value) {
    String normalized = value.trim().toUpperCase();
    if (normalized.endsWith("KB")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 2)) * KB);
    }
    if (normalized.endsWith("MB")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 2)) * MB);
    }
    if (normalized.endsWith("GB")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 2)) * GB);
    }
    if (normalized.endsWith("B")) {
      return (long) Double.parseDouble(
          normalized.substring(0, normalized.length() - 1));
    }
    
    return 0;
  }

  /**
   * Parses a human-readable time duration string to nanoseconds.
   * @param value the string containing time with unit.
   * @return time in nanoseconds.
   */
  private long parseTimeDuration(final String value) {
    String normalized = value.trim().toLowerCase();
    if (normalized.endsWith("ns")) {
      return (long) Double.parseDouble(
          normalized.substring(0, normalized.length() - 2));
    }
    if (normalized.endsWith("us")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 2)) * NS_IN_US);
    }
    if (normalized.endsWith("ms")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 2)) * NS_IN_MS);
    }
    if (normalized.endsWith("s")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 1)) * NS_IN_S);
    }
    if (normalized.endsWith("m")) {
      return (long) (Double.parseDouble(
          normalized.substring(0, normalized.length() - 1)) * NS_IN_M);
    }
    return 0;
  }

  /**
   * Converts total byte size to the desired unit.
   * @param bytes the total bytes to convert.
   * @param unit the unit to convert into.
   * @return the converted size.
   */
  private double convertByteSize(final long bytes,
      @Nullable final String unit) {
    if (unit == null) {
      return bytes;
    }
    switch (unit.toUpperCase()) {
      case "KB":
        return bytes / (double) KB;
      case "MB":
        return bytes / (double) MB;
      case "GB":
        return bytes / (double) GB;
      default:
        return bytes;
    }
  }

  /**
   * Converts total time duration to the desired unit.
   * @param nanos the total nanoseconds to convert.
   * @param unit the unit to convert into.
   * @return converted time.
   */
  private double convertTimeDuration(final long nanos,
      @Nullable final String unit) {
    if (unit == null) {
      return nanos;
    }
    switch (unit.toLowerCase()) {
      case "us":
        return nanos / (double) NS_IN_US;
      case "ms":
        return nanos / (double) NS_IN_MS;
      case "s":
        return nanos / (double) NS_IN_S;
      case "m":
        return nanos / (double) NS_IN_M;
      default:
        return nanos;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void destroy() {
    // No cleanup necessary
  }
}