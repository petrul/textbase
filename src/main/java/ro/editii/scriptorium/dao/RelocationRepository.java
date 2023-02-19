package ro.editii.scriptorium.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.editii.scriptorium.model.Relocation;

public interface RelocationRepository extends JpaRepository<Relocation, String> {}
