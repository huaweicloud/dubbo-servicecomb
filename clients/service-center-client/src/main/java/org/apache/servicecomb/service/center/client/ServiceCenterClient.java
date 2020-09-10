/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.service.center.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.http.client.common.MessageObjectMapper;
import org.apache.servicecomb.http.client.common.TLSConfig;
import org.apache.servicecomb.http.client.common.TLSHttpsTransport;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.CreateMicroserviceInstanceRequest;
import org.apache.servicecomb.service.center.client.model.CreateMicroserviceRequest;
import org.apache.servicecomb.service.center.client.model.CreateSchemaRequest;
import org.apache.servicecomb.service.center.client.model.HeartbeatsRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.ModifySchemasRequest;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceInstanceResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceCenterClient implements ServiceCenterOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterClient.class);

  private final ObjectMapper mapper = new MessageObjectMapper();

  private ServiceCenterRawClient httpClient;

  /**
   * Use default config parameter
   */
  public ServiceCenterClient() {
    this(new ServiceCenterRawClient());
  }

  /**
   * Add TLS config of client
   * @param tlsConfig
   */
  public ServiceCenterClient(TLSConfig tlsConfig) {
    this(tlsConfig, null);
  }

  /**
   * Add extraGlobalHeaders to http request
   * @param extraGlobalHeaders
   */
  public ServiceCenterClient(Map<String, String> extraGlobalHeaders) {
    this(null, 0, null, null, null, extraGlobalHeaders);
  }

  /**
   * Add TLS config and extraGlobalHeaders
   * @param tlsConfig
   * @param extraGlobalHeaders
   */
  public ServiceCenterClient(TLSConfig tlsConfig, Map<String, String> extraGlobalHeaders) {
    this(null, 0, null, null, tlsConfig, extraGlobalHeaders);
  }

  /**
   * Customized host,port,
   * @param host
   * @param port
   */
  public ServiceCenterClient(String host, int port) {
    this(host, port, null, null, null, null);
  }

  /**
   * Customized host, port, projectName, tenantName, TLSConf, headers and any one parameter can be null.
   * @param host
   * @param port
   * @param projectName
   * @param tenantName
   * @param tlsConfig
   * @param extraGlobalHeaders
   */
  public ServiceCenterClient(String host, int port, String projectName, String tenantName, TLSConfig tlsConfig,
      Map<String, String> extraGlobalHeaders) {
    HttpTransport httpTransport = HttpTransportFactory.getDefaultHttpTransport();
    if (tlsConfig != null) {
      httpTransport = new TLSHttpsTransport(tlsConfig);
    }
    httpTransport.addHeaders(extraGlobalHeaders);

    this.httpClient = new ServiceCenterRawClient.Builder()
        .setHost(host)
        .setPort(port)
        .setProjectName(projectName)
        .setTenantName(tenantName)
        .setHttpTransport(httpTransport).build();
  }

  public ServiceCenterClient(ServiceCenterRawClient serviceCenterRawClient) {
    this.httpClient = serviceCenterRawClient;
  }

  @Override
  public MicroserviceInstancesResponse getServiceCenterInstances() {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/health", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(response.getContent(), MicroserviceInstancesResponse.class);
      } else {
        throw new OperationException(
            "get service-center instances fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service-center instances fails", e);
    }
  }

  @Override
  public RegisteredMicroserviceResponse registerMicroservice(Microservice microservice) {
    try {
      CreateMicroserviceRequest request = new CreateMicroserviceRequest();
      request.setService(microservice);
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices", null, mapper.writeValueAsString(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(response.getContent(), RegisteredMicroserviceResponse.class);
      } else {
        throw new OperationException(
            "register service fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "register service fails", e);
    }
  }

  @Override
  public MicroservicesResponse getMicroserviceList() {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/microservices", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(response.getContent(), MicroservicesResponse.class);
      } else {
        throw new OperationException(
            "get service List fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service List fails", e);
    }
  }

  @Override
  public RegisteredMicroserviceResponse queryServiceId(Microservice microservice) {
    try {
      URIBuilder uriBuilder = new URIBuilder("/registry/existence");
      uriBuilder.setParameter("type", "microservice");
      uriBuilder.setParameter("appId", microservice.getAppId());
      uriBuilder.setParameter("serviceName", microservice.getServiceName());
      uriBuilder.setParameter("version", microservice.getVersion());
      uriBuilder.setParameter("env", microservice.getEnvironment());

      HttpResponse response = httpClient.getHttpRequest(uriBuilder.build().toString(), null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(response.getContent(), RegisteredMicroserviceResponse.class);
      } else {
        LOGGER.info("Query serviceId fails, statusCode = " + response.getStatusCode() + "; message = " + response
            .getMessage()
            + "; content = " + response.getContent());
        return null;
      }
    } catch (IOException e) {
      throw new OperationException(
          "query serviceId fails", e);
    } catch (URISyntaxException e) {
      throw new OperationException(
          "build url failed.", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Microservice getMicroserviceByServiceId(String serviceId) {
    try {
      HttpResponse response = httpClient.getHttpRequest("/registry/microservices/" + serviceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return mapper.readValue(jsonNode.get("service").toString(), Microservice.class);
      } else {
        throw new OperationException(
            "get service message fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service message fails", e);
    }
  }

  @Override
  public RegisteredMicroserviceInstanceResponse registerMicroserviceInstance(MicroserviceInstance instance) {
    try {
      CreateMicroserviceInstanceRequest request = new CreateMicroserviceInstanceRequest();
      request.setInstance(instance);
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices/" + instance.getServiceId() + "/instances", null,
              mapper.writeValueAsString(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(response.getContent(), RegisteredMicroserviceInstanceResponse.class);
      } else {
        throw new OperationException(
            "register service instance fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "register service instance fails", e);
    }
  }

  @Override
  public MicroserviceInstancesResponse getMicroserviceInstanceList(String serviceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/instances", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(response.getContent(), MicroserviceInstancesResponse.class);
      } else {
        throw new OperationException(
            "get service instances list fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service instances list fails", e);
    }
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return mapper.readValue(jsonNode.get("instance").toString(), MicroserviceInstance.class);
      } else {
        throw new OperationException(
            "get service instance message fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service instance message fails", e);
    }
  }

  /**
   * Delete a microservice instance
   *
   * @param serviceId
   * @param instanceId
   * @return
   * @throws OperationException
   */
  public void deleteMicroserviceInstance(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .deleteHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("DELETE SERVICE INSTANCE OK");
      } else {
        throw new OperationException(
            "delete service instance fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "delete service instance fails", e);
    }
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(String serviceId, String instanceId,
      MicroserviceInstanceStatus status) {
    try {
      HttpResponse response = httpClient.putHttpRequest(
          "/registry/microservices/" + serviceId + "/instances/" + instanceId + "/status?value=" + status, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("UPDATE STATUS OK");
        return true;
      } else {
        throw new OperationException(
            "update service instance status fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "update service instance status fails", e);
    }
  }

  /**
   * Batch send heartbeats to service-center
   *
   * @param heartbeatsRequest
   * @return
   * @throws OperationException
   */
  public void sendHeartBeats(HeartbeatsRequest heartbeatsRequest) {
    try {
      HttpResponse response = httpClient
          .putHttpRequest("/registry/heartbeats", null, mapper.writeValueAsString(heartbeatsRequest));

      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("HEARTBEATS SUCCESS");
      } else {
        throw new OperationException(
            "heartbeats fails, statusCode = " + response.getStatusCode() + "; message = " + response.getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "heartbeats fails ", e);
    }
  }

  @Override
  public boolean sendHeartBeat(String serviceId, String instanceId) {
    try {
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/instances/" + instanceId + "/heartbeat",
              null, null);

      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      } else {
        throw new OperationException(
            "heartbeats fails, statusCode = " + response.getStatusCode() + "; message = " + response.getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "heartbeats fails ", e);
    }
  }

  /**
   * Get schemas list of service
   *
   * @param serviceId
   * @return
   * @throws OperationException
   */
  public List<SchemaInfo> getServiceSchemasList(String serviceId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/schemas", null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return mapper.readValue(jsonNode.get("schemas").toString(), new TypeReference<List<SchemaInfo>>() {
        });
      } else {
        throw new OperationException(
            "get service schemas list fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service schemas list fails", e);
    }
  }

  /**
   * Get one schema context of service
   *
   * @param serviceId
   * @param schemaId
   * @return
   * @throws OperationException
   */
  public String getServiceSchemaContext(String serviceId, String schemaId) {
    try {
      HttpResponse response = httpClient
          .getHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaId, null, null);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        JsonNode jsonNode = mapper.readTree(response.getContent());
        return jsonNode.get("schema").textValue();
      } else {
        throw new OperationException(
            "get service schema context fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "get service schemas context fails", e);
    }
  }

  @Override
  public boolean registerSchema(String serviceId, String schemaId, CreateSchemaRequest schema) {
    try {
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaId, null,
              mapper.writeValueAsString(schema));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      } else {
        throw new OperationException(
            "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }

  @Override
  public boolean updateServiceSchemaContext(String serviceId, SchemaInfo schemaInfo) {
    try {
      CreateSchemaRequest request = new CreateSchemaRequest();
      request.setSchema(schemaInfo.getSchema());
      request.setSummary(schemaInfo.getSummary());
      HttpResponse response = httpClient
          .putHttpRequest("/registry/microservices/" + serviceId + "/schemas/" + schemaInfo.getSchemaId(), null,
              mapper.writeValueAsString(request));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      } else {
        throw new OperationException(
            "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }

  @Override
  public boolean batchUpdateServiceSchemaContext(String serviceId, ModifySchemasRequest modifySchemasRequest) {
    try {
      HttpResponse response = httpClient
          .postHttpRequest("/registry/microservices/" + serviceId + "/schemas", null,
              mapper.writeValueAsString(modifySchemasRequest));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("UPDATE SCHEMA OK");
        return true;
      } else {
        throw new OperationException(
            "update service schema fails, statusCode = " + response.getStatusCode() + "; message = " + response
                .getMessage()
                + "; content = " + response.getContent());
      }
    } catch (IOException e) {
      throw new OperationException(
          "update service schema fails", e);
    }
  }
}
