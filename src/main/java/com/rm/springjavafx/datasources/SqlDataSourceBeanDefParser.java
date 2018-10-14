package com.rm.springjavafx.datasources;

import java.util.List;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 *
 * @author rmarquez
 */
public class SqlDataSourceBeanDefParser extends AbstractBeanDefinitionParser {
  
  /**
   * 
   * @param elmnt
   * @param pc
   * @return 
   */
  @Override
  protected AbstractBeanDefinition parseInternal(Element elmnt, ParserContext pc) {
    BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(SqlDataSourceFactory.class);
    String id = elmnt.getAttribute("id");
    pc.getRegistry().registerBeanDefinition(id, result.getBeanDefinition());
    
    result.addPropertyValue("dbConnectionRef", elmnt.getAttribute("dbConnectionRef"));
    List<Element> els = DomUtils.getChildElements(elmnt);
    els.stream().forEach((el) -> {
      String tagName = el.getTagName();
      if (tagName.endsWith("query-params")) {
        BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(QueryParameters.class);
        new QueryParametersBeanDefParser().doParse(el, beanDefBuilder);
        result.addPropertyValue("queryParams", beanDefBuilder.getBeanDefinition()); 
      } else if (tagName.endsWith("query-file")) {
        String val = el.getAttribute("value"); 
        result.addPropertyValue("queryFile", val); 
      } 
    });
    return result.getBeanDefinition();
  }
}
