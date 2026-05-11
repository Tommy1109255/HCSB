package com.hcbs.repository;
import com.hcbs.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FilmRepository extends JpaRepository<Film, Long> {}
