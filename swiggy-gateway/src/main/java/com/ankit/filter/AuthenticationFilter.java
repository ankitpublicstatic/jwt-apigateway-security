package com.ankit.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import com.ankit.util.JwtUtil;

@Component
public class AuthenticationFilter
    extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  @Autowired
  private RouteValidator validator;

  // @Autowired
  // private RestTemplate template;
  @Autowired
  private JwtUtil jwtUtil;

  public AuthenticationFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest serverHttpRequest = null;
      if (validator.isSecured.test(exchange.getRequest())) {
        // header contains token or not
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
          throw new RuntimeException("missing authorization header");
        }

        String token = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        if (token != null && token.startsWith("Bearer ")) {
          token = token.substring(7);
        }
        try {
          // //REST call to AUTH service
          // template.getForObject("http://IDENTITY-SERVICE//validate?token" + authHeader,
          // String.class);
          jwtUtil.validateToken(token);

          // Passing logged user name to caller microservice
          serverHttpRequest = exchange.getRequest().mutate()
              .header("loggedUser", jwtUtil.extractUserName(token)).build();

        } catch (Exception e) {
          System.out.println("invalid access...!");
          throw new RuntimeException("unauthorized access to application");
        }
      }
      return chain.filter(exchange.mutate().request(serverHttpRequest).build());
    };
  }

  public static class Config {

  }
}
