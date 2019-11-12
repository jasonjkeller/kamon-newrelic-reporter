/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package kamon.newrelic.metrics

import com.newrelic.telemetry.Attributes
import com.newrelic.telemetry.metrics._
import com.typesafe.config.{Config, ConfigValue, ConfigValueFactory}
import kamon.Kamon
import kamon.metric.{MetricSnapshot, PeriodSnapshot}
import org.mockito.Mockito._
import org.scalatest.{Matchers, WordSpec}

import scala.jdk.CollectionConverters._

class NewRelicMetricsReporterSpec extends WordSpec with Matchers {

  private val countAttributes = new Attributes()
    .put("description", "flam")
    .put("dimensionName", "percentage")
    .put("magnitudeName", "percentage")
    .put("scaleFactor", 1.0d)
    .put("foo", "bar")
    .put("sourceMetricType", "counter")

  private val gaugeAttributes = new Attributes()
    .put("description", "another one")
    .put("magnitudeName", "finch")
    .put("dimensionName", "information")
    .put("scaleFactor", 11.0)
    .put("foo", "bar")
    .put("sourceMetricType", "gauge")

  private val histogramSummaryAttributes = new Attributes()
    .put("magnitude.name", "eimer")
    .put("magnitude.scaleFactor", 603.3d)
    .put("lowestDiscernibleValue", 1L)
    .put("highestTrackableValue", 3600000000000L)
    .put("significantValueDigits", 2)
    .put("twelve", "bishop")
    .put("dimension", "information")
    .put("sourceMetricType", "histogram")

  private val timerSummaryAttributes = new Attributes()
    .put("magnitude.name", "timer")
    .put("magnitude.scaleFactor", 333.3)
    .put("lowestDiscernibleValue", 1L)
    .put("highestTrackableValue", 3600000000000L)
    .put("significantValueDigits", 2)
    .put("thirteen", "queen")
    .put("dimension", "information")
    .put("sourceMetricType", "timer")

  private val count1: Metric = new Count("flib", TestMetricHelper.value1, TestMetricHelper.start, TestMetricHelper.end, countAttributes)
  private val count2: Metric = new Count("flib", TestMetricHelper.value2, TestMetricHelper.start, TestMetricHelper.end, countAttributes)

  private val gauge: Metric = new Gauge("shirley", 15.6d, TestMetricHelper.end, gaugeAttributes)

  private val histogramGauge: Metric = new Gauge("trev.percentiles", 2.0, TestMetricHelper.end,
    histogramSummaryAttributes.copy().put("percentile.countAtRank", 816L).put("percentile", 90.0d))
  private val histogramSummary: Metric = new Summary("trev.summary", 44, 101.0, 13.0, 17.0,
    TestMetricHelper.start, TestMetricHelper.end, histogramSummaryAttributes)

  private val timerGauge: Metric = new Gauge("timer.percentiles", 4.0, TestMetricHelper.end,
    timerSummaryAttributes.copy().put("percentile.countAtRank", 1632L).put("percentile", 95.0d))
  private val timerSummary: Metric = new Summary("timer.summary", 88, 202.0, 26.0, 34.0,
    TestMetricHelper.start, TestMetricHelper.end, timerSummaryAttributes)

  "The metrics reporter" should {
    "send some metrics" in {
      val counter: MetricSnapshot.Values[Long] = TestMetricHelper.buildCounter
      val kamonGauge = TestMetricHelper.buildGauge
      val histogram = TestMetricHelper.buildHistogramDistribution
      val timer = TestMetricHelper.buildTimerDistribution
      val periodSnapshot = new PeriodSnapshot(TestMetricHelper.startInstant, TestMetricHelper.endInstant,
        Seq(counter), Seq(kamonGauge), Seq(histogram), Seq(timer), Seq())

      val expectedCommonAttributes: Attributes = new Attributes()
        .put("service.name", "kamon-application")
        .put("instrumentation.source", "kamon-agent")
      val expectedBatch: MetricBatch = new MetricBatch(Seq(count1, count2, gauge, histogramGauge, histogramSummary, timerGauge, timerSummary).asJava, expectedCommonAttributes)

      val sender = mock(classOf[MetricBatchSender])

      val reporter = new NewRelicMetricsReporter(sender)
      reporter.reportPeriodSnapshot(periodSnapshot)

      verify(sender).sendBatch(expectedBatch)
    }

    "be reconfigurable" in {
      val counter = TestMetricHelper.buildCounter
      val kamonGauge = TestMetricHelper.buildGauge
      val histogram = TestMetricHelper.buildHistogramDistribution
      val periodSnapshot = new PeriodSnapshot(TestMetricHelper.startInstant, TestMetricHelper.endInstant,
        Seq(counter), Seq(kamonGauge), Seq(histogram), Seq(), Seq())

      val expectedCommonAttributes: Attributes = new Attributes()
        .put("service.name", "cheese-whiz")
        .put("instrumentation.source", "kamon-agent")
      val expectedBatch: MetricBatch = new MetricBatch(Seq(count1, count2, gauge, histogramGauge, histogramSummary).asJava, expectedCommonAttributes)

      val configObject: ConfigValue = ConfigValueFactory.fromMap(Map("service" -> "cheese-whiz").asJava)
      val config: Config = Kamon.config().withValue("kamon.environment", configObject)

      val sender = mock(classOf[MetricBatchSender])
      val reporter = new NewRelicMetricsReporter(sender)

      reporter.reconfigure(config)
      reporter.reportPeriodSnapshot(periodSnapshot)

      verify(sender).sendBatch(expectedBatch)
    }
  }

}
