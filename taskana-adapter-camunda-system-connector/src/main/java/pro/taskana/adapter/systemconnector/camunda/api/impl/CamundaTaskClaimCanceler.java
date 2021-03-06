package pro.taskana.adapter.systemconnector.camunda.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import pro.taskana.adapter.systemconnector.api.ReferencedTask;
import pro.taskana.adapter.systemconnector.api.SystemResponse;
import pro.taskana.adapter.systemconnector.camunda.config.CamundaSystemUrls;
import pro.taskana.common.api.exceptions.SystemException;

/**
 * Cancels claims of tasks in camunda through the camunda REST-API that have a canceled claim in
 * TASKANA.
 */
@Component
public class CamundaTaskClaimCanceler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CamundaTaskClaimCanceler.class);

  @Autowired private RestTemplate restTemplate;

  public SystemResponse cancelClaimOfCamundaTask(
      CamundaSystemUrls.SystemUrlInfo camundaSystemUrlInfo, ReferencedTask referencedTask) {

    StringBuilder requestUrlBuilder = new StringBuilder();

    requestUrlBuilder
        .append(camundaSystemUrlInfo.getSystemRestUrl())
        .append(CamundaSystemConnectorImpl.URL_GET_CAMUNDA_TASKS)
        .append(referencedTask.getId())
        .append(CamundaSystemConnectorImpl.UNCLAIM_TASK);

    String requestBody = "{}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity(requestUrlBuilder.toString(), requestEntity, String.class);
      LOGGER.debug(
          "cancel claimed camunda task {}. Status code = {}",
          referencedTask.getId(),
          responseEntity.getStatusCode());

      return new SystemResponse(responseEntity.getStatusCode(), null);

    } catch (HttpStatusCodeException e) {

      LOGGER.info(
          "tried to cancel claim camunda task {} and caught Status code {}",
          referencedTask.getId(),
          e.getStatusCode());
      throw new SystemException(
          "caught HttpStatusCodeException "
              + e.getStatusCode()
              + " on the attempt to cancel claim Camunda Task "
              + referencedTask.getId(),
          e.getMostSpecificCause());
    }
  }
}
