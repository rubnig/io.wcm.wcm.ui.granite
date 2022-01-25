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
package io.wcm.wcm.ui.granite.testcontext;

import java.util.Locale;

import javax.servlet.jsp.PageContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;

import com.adobe.granite.ui.components.ExpressionResolver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.sling.commons.resource.ImmutableValueMap;

public class MockExpressionResolver implements ExpressionResolver {

  @Override
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public <T> T resolve(String expression, Locale locale, Class<T> expectedType, PageContext pageContext) {
    ValueMap valueMap = ImmutableValueMap.of("value", expression);
    return valueMap.get("value", expectedType);
  }

  @Override
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public <T> T resolve(String expression, Locale locale, Class<T> expectedType, SlingHttpServletRequest request) {
    ValueMap valueMap = ImmutableValueMap.of("value", expression);
    return valueMap.get("value", expectedType);
  }

}
