/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.metrics2.sink.timeline;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import java.io.IOException;
import java.net.ConnectException;

public abstract class AbstractTimelineMetricsSink {
  public static final String TAGS_FOR_PREFIX_PROPERTY_PREFIX = "tagsForPrefix.";
  public static final String MAX_METRIC_ROW_CACHE_SIZE = "maxRowCacheSize";
  public static final String METRICS_SEND_INTERVAL = "sendInterval";
  public static final String METRICS_POST_TIMEOUT_SECONDS = "timeout";
  public static final String COLLECTOR_HOST_PROPERTY = "collector";
  public static final String COLLECTOR_PORT_PROPERTY = "port";

  protected static final int DEFAULT_POST_TIMEOUT_SECONDS = 10;
  protected final Log LOG;
  private HttpClient httpClient = new HttpClient();

  protected static ObjectMapper mapper;

  static {
    mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    mapper.setAnnotationIntrospector(introspector);
    mapper.getSerializationConfig()
        .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
  }

  public AbstractTimelineMetricsSink() {
    LOG = LogFactory.getLog(this.getClass());
    httpClient.getParams().setSoTimeout(getTimeoutSeconds() * 1000);
    httpClient.getParams().setConnectionManagerTimeout(getTimeoutSeconds() * 1000);
  }

  protected void emitMetrics(TimelineMetrics metrics) throws IOException {
    String connectUrl = getCollectorUri();
    try {
      String jsonData = mapper.writeValueAsString(metrics);

      StringRequestEntity requestEntity = new StringRequestEntity(jsonData, "application/json", "UTF-8");

      PostMethod postMethod = new PostMethod(connectUrl);
      postMethod.setRequestEntity(requestEntity);
      int statusCode = httpClient.executeMethod(postMethod);

      if (statusCode != 200) {
        LOG.info("Unable to POST metrics to collector, " + connectUrl + ", " +
          "statusCode = " + statusCode);
      } else {
        LOG.debug("Metrics posted to Collector " + connectUrl);
      }
    } catch (ConnectException e) {
      throw new UnableToConnectException(e).setConnectUrl(connectUrl);
    }
  }

  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  abstract protected String getCollectorUri();

  abstract protected int getTimeoutSeconds();
}
