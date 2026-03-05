package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.event.AllPersonsDeletedEvent;
import com.persons.finder.domain.event.PersonLocationUpdatedEvent;
import com.persons.finder.domain.model.Person;
import com.persons.finder.utils.GeoShardingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSyncListener {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Only update Redis after the database transaction is successfully Commited
     * Addresses "index drift" risk
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLocationUpdate(PersonLocationUpdatedEvent event) {
        String personId = event.personId().toString();

        // Step 1: Clean up old shards
        if (event.oldLocation() != null) {
            String oldShard = GeoShardingUtil.getShardKey(
                    event.oldLocation().getX(),
                    event.oldLocation().getY()
            );
            redisTemplate.opsForGeo().remove(oldShard, personId);
        }

        // Step 2: Write new shards
        if (event.newLocation() != null) {
            String newShard = GeoShardingUtil.getShardKey(
                    event.newLocation().getX(),
                    event.newLocation().getY()
            );
            redisTemplate.opsForGeo().add(
                    newShard,
                    event.newLocation(),
                    personId
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAllDeleted(AllPersonsDeletedEvent event) {
        Set<String> keys = redisTemplate.keys("person:geo:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}