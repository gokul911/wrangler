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

package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {

  @Test
  public void testParseNanoseconds() {
    // "150ns" should return 150 (nanoseconds)
    TimeDuration token = new TimeDuration("150ns");
    long expected = 150L;
    Assert.assertEquals("Failed to parse ns correctly", expected, token.getNanoSeconds());
  }

  @Test
  public void testParseMilliseconds() {
    // "150ms" should return 150 * 1,000,000 = 150,000,000 ns
    TimeDuration token = new TimeDuration("150ms");
    long expected = 150L * 1_000_000L;
    Assert.assertEquals("Failed to parse ms correctly", expected, token.getNanoSeconds());
  }

  @Test
  public void testParseSeconds() {
    // "5s" should return 5 * 1,000,000,000 = 5,000,000,000 ns
    TimeDuration token = new TimeDuration("5s");
    long expected = 5L * 1_000_000_000L;
    Assert.assertEquals("Failed to parse s correctly", expected, token.getNanoSeconds());
  }

  @Test
  public void testParseMinutes() {
    // "3m" should return 3 * 60 * 1,000,000,000 = 180,000,000,000 ns
    TimeDuration token = new TimeDuration("3m");
    long expected = 3L * 60 * 1_000_000_000L;
    Assert.assertEquals("Failed to parse m correctly", expected, token.getNanoSeconds());
  }

  @Test
  public void testParseHours() {
    // "2h" should return 2 * 3600 * 1,000,000,000 = 7,200,000,000,000 ns
    TimeDuration token = new TimeDuration("2h");
    long expected = 2L * 3600 * 1_000_000_000L;
    Assert.assertEquals("Failed to parse h correctly", expected, token.getNanoSeconds());
  }

  @Test
  public void testParseDays() {
    // "1d" should return 1 * 86400 * 1,000,000,000 = 86,400,000,000,000 ns
    TimeDuration token = new TimeDuration("1d");
    long expected = 1L * 86400 * 1_000_000_000L;
    Assert.assertEquals("Failed to parse d correctly", expected, token.getNanoSeconds());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    // Providing an unsupported unit should throw an exception.
    new TimeDuration("10weeks");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    // Providing an invalid format (non-numeric) should throw an exception.
    new TimeDuration("abc");
  }
}
