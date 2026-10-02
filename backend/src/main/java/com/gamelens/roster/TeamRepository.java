package com.gamelens.roster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TeamRepository extends JpaRepository<Team,UUID> {
 List<Team> findAllByOrderByNameAscSeasonAsc();
}
