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

import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {

  @Test
  public void testParseKB() {
    ByteSize token = new ByteSize("10KB");
    // 10 KB = 10 * 1024
    long expected = 10 * 1024L;
    Assert.assertEquals("Failed to parse KB correctly", expected, token.getBytes());
  }

  @Test
  public void testParseMB() {
    ByteSize token = new ByteSize("512MB");
    // 512 MB = 512 * 1024 * 1024
    long expected = 512L * 1024 * 1024;
    Assert.assertEquals("Failed to parse MB correctly", expected, token.getBytes());
  }

  @Test
  public void testParseGB() {
    ByteSize token = new ByteSize("1.5GB");
    // 1.5GB = 1.5 * 1024^3
    long expected = (long) (1.5 * 1024 * 1024 * 1024);
    Assert.assertEquals("Failed to parse GB correctly", expected, token.getBytes());
  }

  @Test
  public void testParseTB() {
    ByteSize token = new ByteSize("2TB");
    // 2TB = 2 * 1024^4 bytes
    long expected = 2L * 1024 * 1024 * 1024 * 1024;
    Assert.assertEquals("Failed to parse TB correctly", expected, token.getBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    // This should throw an exception because "20XYZ" is not a valid unit.
    new ByteSize("20XYZ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    // This should throw an exception as there is no valid number/unit format.
    new ByteSize("invalidInput");
  }
}
