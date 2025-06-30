package tw.fengqing.spring.springbucks.config;

import tw.fengqing.spring.springbucks.converter.BytesToMoneyConverter;
import tw.fengqing.spring.springbucks.converter.MoneyToBytesConverter;
import io.lettuce.core.ReadFrom;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.Arrays;

@Configuration
@EnableRedisRepositories(basePackages = "tw.fengqing.spring.springbucks.repository")
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer customizer() {
        return builder -> builder.readFrom(ReadFrom.MASTER_PREFERRED);
    }

    @Bean
    public RedisCustomConversions redisCustomConversions() {
        return new RedisCustomConversions(
                Arrays.asList(new MoneyToBytesConverter(), new BytesToMoneyConverter()));
    }
} 