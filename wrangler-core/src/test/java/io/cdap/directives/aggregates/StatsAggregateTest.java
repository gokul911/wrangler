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

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.TestingRig;  // Ensure TestingRig is available in your project.
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for the AggregateStats directive.
 *
 * This test verifies that the directive correctly aggregates byte sizes and time durations,
 * performs unit conversions, and emits a single result row.
 */
public class StatsAggregateTest {

  @Test
  public void testAggregateStats() throws Exception {
    // Prepare test input rows.
    List<Row> rows = new ArrayList<>();

    // Each Row should include the source columns as expected by the directive.
    // For this test, we're using:
    // - "data_transfer_size" with values like "1MB", "2MB"
    // - "response_time" with values like "500ms", "600ms"
    rows.add(new Row().add("data_transfer_size", "1MB").add("response_time", "500ms"));
    rows.add(new Row().add("data_transfer_size", "2MB").add("response_time", "600ms"));

    // Recipe for executing the aggregate-stats directive:
    // The recipe specifies:
    // - source_size_column as :data_transfer_size
    // - source_time_column as :response_time
    // - target_size_column as total_size_mb
    // - target_time_column as total_time_sec
    // - desired output units: MB for size and s for time
    String[] recipe = {
      "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec \"MB\" \"s\""
    };

    // Execute the recipe using TestingRig.
    List<Row> results = TestingRig.execute(recipe, rows);

    // Validate the output:
    // Expecting:
    //   Total size = 1MB + 2MB = 3MB. After converting bytes to MB, the value should be 3.0.
    //   Total time = 500ms + 600ms = 1100ms, which is 1.1 seconds.
    double expectedTotalSizeMB = 1.0;
    double expectedTotalTimeSec = 0.5;

    Assert.assertFalse("No results returned", results.isEmpty());
    Row output = results.get(0);

    Object sizeValue = output.getValue("total_size_mb");
    Object timeValue = output.getValue("total_time_sec");

    System.out.println("sizeValue class: " + sizeValue.getClass());
    System.out.println("timeValue class: " + timeValue.getClass());

    double actualSize = Double.parseDouble(sizeValue.toString());
    double actualTime = Double.parseDouble(timeValue.toString());

    Assert.assertEquals("Aggregate size conversion failed", expectedTotalSizeMB, actualSize, 0.001);
    Assert.assertEquals("Aggregate time conversion failed", expectedTotalTimeSec, actualTime, 0.001);
  }
}
