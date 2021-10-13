/*******************************************************************************
 * Copyright © [24]7 Customer, Inc.
 * All Rights Reserved.
 *******************************************************************************/
package com.tfs.learningsystems.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.tfs.learningsystems.ui.model.PatchDocument;
import com.tfs.learningsystems.ui.model.PatchRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Convert {@link PatchRequest} containing JSON Patch to Java Object.
 *
 * @author manish.marathe
 */
@Component
@Slf4j
public class JsonConverter {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public ArrayNode convertToJsonNode(PatchRequest patchRequest) {
    JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
    ArrayNode patchNode = nodeFactory.arrayNode();
    for (PatchDocument document : patchRequest) {
      ObjectNode opNode = nodeFactory.objectNode();
      opNode.set("op", nodeFactory.textNode(document.getOp().toString()));
      opNode.set("path", nodeFactory.textNode(document.getPath()));
      String fromValue = document.getFrom();
      if (fromValue != null && !"".equals(fromValue)) {
        opNode.set("from", nodeFactory.textNode(fromValue));
      }
      Object toValue = document.getValue();

      if (toValue != null && !"".equals(toValue)) {
        opNode.set("value", objectMapper.valueToTree(toValue));
      }
      patchNode.add(opNode);
    }
    return patchNode;
  }

  public <T> T patch(PatchRequest patchRequest, T original,
      Class<T> classType) {
    ArrayNode patchNode = null;
    Object object = null;
    JsonNode serverState = null;
    JsonPatch patch = null;

    JsonNode result;

    try {
      patchNode = this.convertToJsonNode(patchRequest);
      serverState = objectMapper.convertValue(original, JsonNode.class);
      patch = JsonPatch.fromJson(patchNode);
      result = patch.apply(serverState);
      objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      object = objectMapper.treeToValue(result, original.getClass());
    } catch (JsonProcessingException jpre) {
      log.error("Failed to convert patched JsonNode to original object", jpre);
    } catch (IOException e) {
      log.error("Failed creating patch from the PatchNode: {}", patchNode, e);
    } catch (JsonPatchException jpe) {
      log.error("Failed to apply patch: {} to the original object: {}",
          patch, serverState, jpe);
    } catch (Exception ex) {
      log.error("Failed while creating a patch document: {}",
          patch, serverState, ex);
    }

    return classType.cast(object);
  }


}
