/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.wcm.ui.granite.pathfield.impl;

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_FILE;
import static com.day.cq.commons.jcr.JcrConstants.NT_HIERARCHYNODE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static io.wcm.wcm.ui.granite.pathfield.impl.DummyPredicateProvider.PREDICATE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.ds.DataSource;
import com.day.cq.commons.predicate.PredicateProvider;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.ui.granite.testcontext.MockExpressionResolver;

@ExtendWith(AemContextExtension.class)
class PathFieldChildrenDatasourceServletTest {

  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  private PathFieldChildrenDatasourceServlet underTest;

  @BeforeEach
  void setUp() {
    context.registerService(ExpressionResolver.class, new MockExpressionResolver());
    context.registerService(PredicateProvider.class, new DummyPredicateProvider(context));

    context.registerService(Predicate.class, new com.day.cq.commons.predicate.IsFolderPredicate(),
        PREDICATE_NAME, "folder");
    context.registerService(Predicate.class, new com.day.cq.commons.predicate.IsHierarchyNodePredicate(),
        PREDICATE_NAME, "hierarchy");
    context.registerService(Predicate.class, new com.day.cq.commons.predicate.HierarchyNotFilePredicate(),
        PREDICATE_NAME, "hierarchyNotFile");
    context.registerService(Predicate.class, new com.day.cq.commons.predicate.IsNoSystemNodePredicate(),
        PREDICATE_NAME, "nosystem");

    underTest = context.registerInjectActivateService(new PathFieldChildrenDatasourceServlet());

    context.currentResource(context.create().resource("/apps/picker",
        "sling:resourceType", PathFieldChildrenDatasourceServlet.RESOURCE_TYPE));

    context.build().resource("/content/l1", JCR_PRIMARYTYPE, NT_HIERARCHYNODE)
        .siblingsMode()
        .resource("l1b", JCR_PRIMARYTYPE, NT_HIERARCHYNODE)
        .resource("l1a", JCR_PRIMARYTYPE, NT_HIERARCHYNODE)
        .resource("file", JCR_PRIMARYTYPE, NT_FILE);

  }

  @Test
  void testHierarchyNotFilePredicate() {
    Map<String, Object> props = Map.of(
        "path", "/content/l1",
        "filter", "hierarchyNotFile");
    assertResultPaths(props,
        "/content/l1/l1a",
        "/content/l1/l1b");
  }

  @Test
  void testNoSystemPredicate() {
    Map<String, Object> props = Map.of(
        "path", "/content/l1",
        "filter", "nosystem");
    assertResultPaths(props,
        "/content/l1/file",
        "/content/l1/l1a",
        "/content/l1/l1b");
  }

  @Test
  void testQuery() {
    Map<String, Object> props = Map.of(
        "query", "fi",
        "rootPath", "/content/l1",
        "filter", "nosystem");
    assertResultPaths(props,
        "/content/l1/file");
  }

  @Test
  void testHierarchyNotFilePredicate_OrderedChildNodes() {
    context.build().resource("/content/l2", JCR_PRIMARYTYPE, NT_UNSTRUCTURED)
        .siblingsMode()
        .resource("l2b", JCR_PRIMARYTYPE, NT_HIERARCHYNODE)
        .resource("l2a", JCR_PRIMARYTYPE, NT_HIERARCHYNODE)
        .resource("file", JCR_PRIMARYTYPE, NT_FILE);

    Map<String, Object> props = Map.of(
        "path", "/content/l2",
        "filter", "hierarchyNotFile");
    assertResultPaths(props,
        "/content/l2/l2b",
        "/content/l2/l2a");
  }

  private void assertResultPaths(Map<String, Object> props, String... paths) {
    context.create().resource("/apps/picker/datasource", props);
    try {
      underTest.doGet(context.request(), context.response());
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    DataSource ds = (DataSource)context.request().getAttribute(DataSource.class.getName());
    List<String> actualPaths = IteratorUtils.toList(ds.iterator()).stream().map(Resource::getPath).collect(Collectors.toList());
    List<String> expectedPaths = List.of(paths);
    assertEquals(expectedPaths, actualPaths);
  }

}
