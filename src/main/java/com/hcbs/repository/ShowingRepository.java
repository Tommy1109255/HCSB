package com.hcbs.repository;
import com.hcbs.entity.Showing;
import com.hcbs.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface ShowingRepository extends JpaRepository<Showing, Long> {
    List<Showing> findByDate(LocalDate date);
    List<Showing> findByScreenAndDate(Screen screen, LocalDate date);
}
