package com.hcbs.repository;
import com.hcbs.entity.Screen;
import com.hcbs.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByCinema(Cinema cinema);
}
