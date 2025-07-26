/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.java.generator;

import io.fabric8.java.generator.nodes.GeneratorResult;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.util.Map;
import org.approvaltests.Approvals;
import org.approvaltests.namer.NamedEnvironment;
import org.approvaltests.namer.NamerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.fabric8.java.generator.CRGeneratorRunner.groupToPackage;

class ApprovalTest {

  private java.util.List<io.fabric8.kubernetes.api.model.EnvVar> env = io.fabric8.kubernetes.client.utils.Serialization.unmarshal("[{\"name\":\"METADATA_NAMESPACE\",\"valueFrom\":{\"fieldRef\":{\"fieldPath\":\"metadata.namespace\"}}},{\"name\":\"METADATA_NAME\",\"valueFrom\":{\"fieldRef\":{\"fieldPath\":\"metadata.name\"}}}]", new com.fasterxml.jackson.core.type.TypeReference<java.util.List<io.fabric8.kubernetes.api.model.EnvVar>>() {});

  @Test
  void testEnv() {
    assertThat(env).hasSize(2);
    assertThat(env.get(0).getName()).isEqualTo("METADATA_NAMESPACE");
    assertThat(env.get(0).getValueFrom().getFieldRef().getFieldPath()).isEqualTo("metadata.namespace");
    assertThat(env.get(1).getName()).isEqualTo("METADATA_NAME");
    assertThat(env.get(1).getValueFrom().getFieldRef().getFieldPath()).isEqualTo("metadata.name");
  }

//  @Test
//  void config1() {
//    Map<String, String> existingJavaTypes = Map.of("a", "b");
//    Config config1 = Config.builder().existingJavaTypes(existingJavaTypes).build();
//    Config config2 = Config.builder().existingJavaTypes(existingJavaTypes).build();
//    assertThat(config1.getExistingJavaTypes()).isNotSameInstanceAs(config2.getExistingJavaTypes());
//  }
  @Test
  void config4() {
    Map<String, String> existingJavaTypes = Collections.emptyMap();
    Config config1 = Config.builder().existingJavaTypes(existingJavaTypes).build();
    Config config2 = Config.builder().existingJavaTypes(existingJavaTypes).build();
    assertThat(config1.getExistingJavaTypes()).isNotSameInstanceAs(config2.getExistingJavaTypes());
  }
  @Test
  void config2() {
    Config.ConfigBuilder builder = Config.builder();
//    builder.existingJavaTypes(Collections.emptyMap());
    Config config3 = builder.build();
    Config config4 = builder.build();
    assertThat(config3.getExistingJavaTypes()).isNotSameInstanceAs(config4.getExistingJavaTypes());
  }
  @Test
  void config5() {
    Config.ConfigBuilder builder = Config.builder();
    builder.existingJavaTypes(Collections.emptyMap());
    Config config3 = builder.build();
    Config config4 = builder.build();
    assertThat(config3.getExistingJavaTypes()).isNotSameInstanceAs(config4.getExistingJavaTypes());
  }
//  @Test
//  void config3() {
//    Config.ConfigBuilder builder = Config.builder();
//    builder.existingJavaTypes(Map.of("a", "b"));
//    Config config3 = builder.build();
//    Config config4 = builder.build();
//    assertThat(config3.getExistingJavaTypes()).isNotSameInstanceAs(config4.getExistingJavaTypes());
//  }
  @Test
  void portForward() {
    KubernetesClient kubernetesClient = new io.fabric8.kubernetes.client.DefaultKubernetesClient(new ConfigBuilder().build());
    kubernetesClient.services().inNamespace("foo").withName("bar").portForward(8080, 80);
  }

  private static Stream<Arguments> getCRDGenerationInputData() {
    return Stream.of(
//        Arguments.of("testCrontabCrd", "crontab-crd.yml", "CronTab", "CrontabJavaCr", new Config()),
//        Arguments.of("testCrontabExtraAnnotationsCrd", "crontab-crd.yml", "CronTab", "CrontabJavaExtraAnnotationsCr",
//            new Config(null, true, null, new HashMap<>())),
//        Arguments.of("testKeycloakCrd", "keycloak-crd.yml", "Keycloak", "KeycloakJavaCr", new Config()),
//        Arguments.of("testJokeCrd", "jokerequests-crd.yml", "JokeRequest", "JokeRequestJavaCr", new Config()),
//        Arguments.of("testAkkaMicroservicesCrd", "akka-microservices-crd.yml", "AkkaMicroservice", "AkkaMicroserviceJavaCr",
//            new Config()),
//        Arguments.of("testCalicoIPPoolCrd", "calico-ippool-crd.yml", "IPPool", "CalicoIPPoolCr", new Config()),
        Arguments.of("testExistingJavaType", "existing-java-type-crd.yml", "ExistingJavaType", "ExistingJavaTypeCr",
            Config.builder().existingJavaTypes(Map.of(
              "org.test.v1.existingjavatypespec.Affinity", "io.fabric8.kubernetes.api.model.Affinity",
              "org.test.v1.existingjavatypespec.Env", "io.fabric8.kubernetes.api.model.EnvVar")).build()));
//        Arguments.of("testRequireSpecAndStatusCrd", "require-spec-and-status-crd.yml", "RequireSpecAndStatus",
//            "RequireSpecAndStatusJavaCr", new Config()));
  }

  @ParameterizedTest
  @MethodSource("getCRDGenerationInputData")
  void generate_withValidCrd_shouldGeneratePojos(String parameter, String crdYaml, String customResourceName,
      String approvalLabel, Config config) {
    try (NamedEnvironment en = NamerFactory.withParameters(parameter)) {
      // Arrange
      CRGeneratorRunner runner = new CRGeneratorRunner(config);
      CustomResourceDefinition crd = getCRD(crdYaml);

      // Act
      List<WritableCRCompilationUnit> writables = runner.generate(crd, groupToPackage("test.org"));

      // Assert
      assertThat(writables).hasSize(1);

      WritableCRCompilationUnit writable = writables.get(0);

      List<String> underTest = new ArrayList<>();
      List<GeneratorResult.ClassResult> crl = writable.getClassResults();
      underTest.add(getJavaClass(crl, customResourceName));
      underTest.add(getJavaClass(crl, customResourceName + "Spec"));
      // not all the tested CRDs have a status definition, e.g. see calico-ippool-crd.yml
      final String statusCrlName = customResourceName + "Status";
      if (crl.stream().anyMatch(c -> c.getName().equals(statusCrlName))) {
        underTest.add(getJavaClass(crl, statusCrlName));
      }
      Approvals.verifyAll(approvalLabel, underTest);
    }
  }

  private CustomResourceDefinition getCRD(String name) {
    return Serialization.unmarshal(
        this.getClass().getClassLoader().getResourceAsStream(name),
        CustomResourceDefinition.class);
  }

  private String getJavaClass(List<GeneratorResult.ClassResult> classResults, String name) {
    GeneratorResult.ClassResult cr = classResults.stream().filter(c -> c.getName().equals(name)).findFirst().get();
    return cr.getJavaSource();
  }
}
