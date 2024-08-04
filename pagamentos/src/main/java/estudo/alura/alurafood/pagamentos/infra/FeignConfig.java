package estudo.alura.alurafood.pagamentos.infra;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public class CustomErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            System.out.println("Feign ErrorDecoder invoked for response: " + response.status());
            switch (response.status()) {
                case 503:
                    return new ServiceUnavailableException("Service is unavailable");
                default:
                    return new Exception("Generic error");
            }
        }
    }

    public class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}