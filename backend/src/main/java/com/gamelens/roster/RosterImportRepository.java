package com.gamelens.roster;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RosterImportRepository extends JpaRepository<RosterImport,UUID> {}
