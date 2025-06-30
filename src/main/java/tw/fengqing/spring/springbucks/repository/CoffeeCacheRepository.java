package tw.fengqing.spring.springbucks.repository;

import tw.fengqing.spring.springbucks.model.CoffeeCache;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CoffeeCacheRepository extends CrudRepository<CoffeeCache, Long> {
    Optional<CoffeeCache> findOneByName(String name);
}
