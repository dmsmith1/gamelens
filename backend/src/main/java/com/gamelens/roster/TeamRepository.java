package com.gamelens.roster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TeamRepository extends JpaRepository<Team,UUID> {
 List<Team> findAllByOrderByNameAscSeasonAsc();
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select t from Team t where t.id = :id")
 Optional<Team> lockById(@org.springframework.data.repository.query.Param("id") UUID id);
}
