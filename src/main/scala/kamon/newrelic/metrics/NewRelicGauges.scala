/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package kamon.newrelic.metrics

import com.newrelic.telemetry.metrics.{Gauge, Metric}
import kamon.metric.{Instrument, MetricSnapshot}
import kamon.newrelic.AttributeBuddy.addTagsFromTagSets
import kamon.newrelic.metrics.ConversionSupport.buildAttributes
import org.slf4j.LoggerFactory

object NewRelicGauges {
  private val logger = LoggerFactory.getLogger(getClass)

  def apply(timestamp: Long, gauge: MetricSnapshot.Values[Double]): Seq[Metric] = {
    val attributes = buildAttributes(gauge)
    logger.debug("name: {} ; numberOfInstruments: {}", gauge.name, gauge.instruments.size)
    gauge.instruments.map { inst: Instrument.Snapshot[Double] =>
      new Gauge(gauge.name, inst.value, timestamp, addTagsFromTagSets(Seq(inst.tags), attributes.copy().put("sourceMetricType", "gauge")))
    }
  }

}
