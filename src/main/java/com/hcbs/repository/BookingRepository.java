package com.hcbs.repository;
import com.hcbs.entity.Booking;
import com.hcbs.entity.Showing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByReference(String reference);
    List<Booking> findByShowing(Showing showing);
}
