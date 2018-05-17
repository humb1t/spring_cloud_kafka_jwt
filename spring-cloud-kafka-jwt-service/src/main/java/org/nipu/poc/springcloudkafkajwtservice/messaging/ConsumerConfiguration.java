package org.nipu.poc.springcloudkafkajwtservice.messaging;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.nipu.poc.springcloudkafkajwtservice.SecurityCheckController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class ConsumerConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Autowired
    private DefaultTokenServices tokenServices;
    private String resourceId = null;
    @Autowired
    private SecurityCheckController securityCheckController;


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        return props;
    }

    @Bean
    public ConsumerFactory<String, MessageContainer> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
                new JsonDeserializer<>(MessageContainer.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageContainer> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MessageContainer> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        return factory;
    }

    @Bean
    public Receiver receiver() {
        return new Receiver(new KafkaAuthenticationManager(), securityCheckController);
    }

    public class KafkaAuthenticationManager implements AuthenticationManager {
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if (authentication == null) {
                throw new InvalidTokenException("Invalid token (token not found)");
            }
            String token = (String) authentication.getPrincipal();
            OAuth2Authentication auth = tokenServices.loadAuthentication(token);
            if (auth == null) {
                throw new InvalidTokenException("Invalid token: " + token);
            }
            Collection<String> resourceIds = auth.getOAuth2Request().getResourceIds();
            if (resourceId != null && resourceIds != null && !resourceIds.isEmpty() && !resourceIds.contains(resourceId)) {
                throw new OAuth2AccessDeniedException("Invalid token does not contain resource id (" + resourceId + ")");
            }
            checkClientDetails(auth);
            if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
                OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
                // Guard against a cached copy of the same details
                if (!details.equals(auth.getDetails())) {
                    // Preserve the authentication details from the one loaded by token services
                    details.setDecodedDetails(auth.getDetails());
                }
            }
            auth.setDetails(authentication.getDetails());
            auth.setAuthenticated(true);
            return auth;
        }
    }


    private void checkClientDetails(OAuth2Authentication auth) {

    }
}
