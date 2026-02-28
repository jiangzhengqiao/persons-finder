package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.event.AllPersonsDeletedEvent;
import com.persons.finder.domain.event.PersonDeletedEvent;
import com.persons.finder.domain.event.PersonLocationUpdatedEvent;
import com.persons.finder.domain.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RedisSyncListener {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String GEO_KEY = "person_locations";

    /**
     * Only update Redis after the database transaction is successfully Commited
     * Addresses "index drift" risk
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLocationUpdate(PersonLocationUpdatedEvent event) {
        Person p = event.person();
        if (null != p.getLocation()) {
            redisTemplate.opsForGeo().add(
                    GEO_KEY,
                    new Point(p.getLocation().getLongitude(), p.getLocation().getLatitude()),
                    p.getId().toString()
            );
        }
    }

    /**
     * Only removed from Redis after the database transaction is successfully Commited.
     */
    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePersonDeletion(PersonDeletedEvent event) {
        if (null != event) {
            redisTemplate.opsForGeo().remove(GEO_KEY, event.personId().toString());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAllDeleted(AllPersonsDeletedEvent event) {
        redisTemplate.delete(GEO_KEY);
    }
}