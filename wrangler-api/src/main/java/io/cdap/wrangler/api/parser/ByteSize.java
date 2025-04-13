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
 * The {@code ByteSize} class parses string representations of byte sizes
 * (e.g., "10KB", "512MB") and converts them to a canonical value in bytes.
 */
@PublicEvolving
public class ByteSize implements Token {

  private final String originalValue;
  private final long bytes;

  /**
   * Allocates a {@code ByteSize} token by parsing the provided string.
   *
   * @param tokenValue the input token string (e.g., "10KB")
   * @throws IllegalArgumentException if the string cannot be parsed.
   */
  public ByteSize(String tokenValue) {
    this.originalValue = tokenValue;
    this.bytes = parseBytes(tokenValue);
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
   * @return TokenType.BYTE_SIZE
   */
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  /**
   * Returns the canonical byte value.
   *
   * @return the number of bytes as a {@code long}.
   */
  public long getBytes() {
    return bytes;
  }

  /**
   * Returns the JSON representation of this token.
   *
   * @return a JsonElement representation.
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.BYTE_SIZE.name());
    object.addProperty("value", originalValue);
    object.addProperty("bytes", bytes);
    return object;
  }

  /**
   * Parses the input token string to compute the number of bytes.
   * Supports units: KB, MB, GB, TB, KiB, and MiB.
   *
   * @param tokenValue the input token string.
   * @return the value in bytes.
   * @throws IllegalArgumentException if the input format is invalid.
   */
  private long parseBytes(String tokenValue) {
    // Pattern to capture the numeric value and the unit.
    Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)(KB|MB|GB|TB|KiB|MiB)", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(tokenValue);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size token: " + tokenValue);
    }
    double number = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toUpperCase();

    // Multiply based on unit.
    switch (unit) {
      case "KB":
      case "KIB":
        return (long) (number * 1024L);
      case "MB":
      case "MIB":
        return (long) (number * 1024L * 1024L);
      case "GB":
        return (long) (number * 1024L * 1024L * 1024L);
      case "TB":
        return (long) (number * 1024L * 1024L * 1024L * 1024L);
      default:
        throw new IllegalArgumentException("Unsupported byte size unit: " + unit);
    }
  }
}
