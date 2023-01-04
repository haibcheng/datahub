package com.linkedin.gms.factory.auth;

import com.datahub.cisco.wap.CITokenInfo;
import com.datahub.cisco.wap.CITokenService;
import com.datahub.cisco.wap.CITokenServiceImpl;
import com.linkedin.gms.factory.spring.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nonnull;

@Configuration
@ConfigurationProperties(prefix = "cisco.ci")
@PropertySource(value = "classpath:/application.yml", factory = YamlPropertySourceFactory.class)
@Data
public class CITokenServiceFactory {

  private CITokenInfo wapDataHub;

  @Bean(name = "wapDataHub")
  @Scope("singleton")
  @Nonnull
  protected CITokenInfo getWapDataHub() {
    return wapDataHub;
  }

  @Bean(name = "ciTokenService")
  @Scope("singleton")
  @Nonnull
  protected CITokenService getInstance() {
    return new CITokenServiceImpl();
  }

}