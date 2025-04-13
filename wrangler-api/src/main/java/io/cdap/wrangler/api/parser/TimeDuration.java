/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code TimeDuration} class parses string representations of time durations
 * (e.g., "150ms", "5s", "2h") and converts them to a canonical value in nanoseconds.
 */
@PublicEvolving
public class TimeDuration implements Token {

  private final String originalValue;
  private final long nanoSeconds;

  /**
   * Allocates a {@code TimeDuration} token by parsing the provided string.
   *
   * @param tokenValue the input token string (e.g., "150ms")
   * @throws IllegalArgumentException if the string cannot be parsed.
   */
  public TimeDuration(String tokenValue) {
    this.originalValue = tokenValue;
    this.nanoSeconds = parseNanoSeconds(tokenValue);
  }

  /**
   * Returns the original token value.
   *
   * @return the input string token value.
   */
  @Override
  public String value() {
    return originalValue;
  }

  /**
   * Returns the token type.
   *
   * @return TokenType.TIME_DURATION
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  /**
   * Returns the canonical time value in nanoseconds.
   *
   * @return the number of nanoseconds as a {@code long}.
   */
  public long getNanoSeconds() {
    return nanoSeconds;
  }

  /**
   * Returns the JSON representation of this token.
   *
   * @return a JsonElement representation.
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.TIME_DURATION.name());
    object.addProperty("value", originalValue);
    object.addProperty("nanoSeconds", nanoSeconds);
    return object;
  }

  /**
   * Parses the input token string to compute the duration in nanoseconds.
   * Supports time units: ns, ms, s, m, h, d.
   *
   * @param tokenValue the input token string.
   * @return the duration in nanoseconds.
   * @throws IllegalArgumentException if the input format is invalid.
   */
  private long parseNanoSeconds(String tokenValue) {
    // Pattern to capture the numeric value and the time unit.
    Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)(ns|ms|s|m|h|d)", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(tokenValue);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration token: " + tokenValue);
    }
    double number = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toLowerCase();

    // Convert based on unit.
    switch (unit) {
      case "ns":
        return (long) number;
      case "ms":
        return (long) (number * 1_000_000L);
      case "s":
        return (long) (number * 1_000_000_000L);
      case "m":
        return (long) (number * 60 * 1_000_000_000L);
      case "h":
        return (long) (number * 3600 * 1_000_000_000L);
      case "d":
        return (long) (number * 86400 * 1_000_000_000L);
      default:
        throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }
  }
}
