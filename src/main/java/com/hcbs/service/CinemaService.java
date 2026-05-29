package com.hcbs.service;

import com.hcbs.entity.*;
import com.hcbs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CinemaService {
    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final FilmRepository filmRepository;
    private final ShowingRepository showingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FilePersistenceService filePersistenceService;

//    public double calculatePrice(String cityName, LocalTime time) {
//        int hour = time.getHour();
//        if (cityName.equalsIgnoreCase("Birmingham")) {
//            if (hour < 12) return 5.0;
//            if (hour < 17) return 6.0;
//            return 7.0;
//        } else if (cityName.equalsIgnoreCase("Bristol")) {
//            if (hour < 12) return 6.0;
//            if (hour < 17) return 7.0;
//            return 8.0;
//        } else if (cityName.equalsIgnoreCase("Cardiff")) {
//            if (hour < 12) return 5.0;
//            if (hour < 17) return 6.0;
//            return 7.0;
//        } else if (cityName.equalsIgnoreCase("London")) {
//            if (hour < 12) return 10.0;
//            if (hour < 17) return 11.0;
//            return 12.0;
//        }
//        return 7.0; // Default
//    }
     //创建预订
    @Transactional
    public Booking createBooking(Showing showing, int numTickets, String seatNumbers, User user) {
        if (showing.getRemainingSeats() < numTickets) {
            throw new RuntimeException("Not enough tickets available! Only " + showing.getRemainingSeats() + " left.");
        }

        String reference = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double totalCost = showing.getPriceLowerHall() * numTickets; 
        
        // Update showing capacity
        showing.setRemainingSeats(showing.getRemainingSeats() - numTickets);
        showingRepository.save(showing);

        Booking booking = Booking.builder()
                .reference(reference)
                .showing(showing)
                .numTickets(numTickets)
                .film(showing.getFilm())
                .screen(showing.getScreen())
                .seatNumbers(seatNumbers)
                .totalCost(totalCost)
                .bookingDate(LocalDateTime.now())
                .bookedBy(user)
                .cancelled(false)
                .build();
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Log to file
        filePersistenceService.saveToFile("New Booking: Ref=" + savedBooking.getReference() + 
            ", User=" + user.getUsername() + ", Tickets=" + numTickets);
            
        return savedBooking;
    }

    //取消票 逻辑
    @Transactional
    public String cancelBooking(String reference) {
        Booking booking = bookingRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.isCancelled()) {
            return "Already cancelled";
        }

        LocalDate showDate = booking.getShowing().getDate();
        LocalDate today = LocalDate.now();

        if (today.isBefore(showDate)) {
            booking.setCancelled(true);
            booking.setCancellationDate(LocalDateTime.now());
            bookingRepository.save(booking);
            return "Cancelled successfully. 50% refund applied (conceptually).";
        } else {
            return "Cancellation not allowed on the day of the show or after.";
        }
    }

    public List<Showing> getShowingsByDate(LocalDate date) {
        return showingRepository.findByDate(date);
    }

    public List<Film> getAllFilms() {
        return filmRepository.findAll();
    }

    // Report Methods
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public double getTotalRevenueForCinema(Cinema cinema) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getShowing().getScreen().getCinema().equals(cinema))
                .filter(b -> !b.isCancelled())
                .mapToDouble(Booking::getTotalCost)
                .sum();
    }

    public Film getTopRevenueFilm() {
        return filmRepository.findAll().stream()
                .max((f1, f2) -> Double.compare(getRevenueForFilm(f1), getRevenueForFilm(f2)))
                .orElse(null);
    }

    private double getRevenueForFilm(Film film) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getShowing().getFilm().equals(film))
                .filter(b -> !b.isCancelled())
                .mapToDouble(Booking::getTotalCost)
                .sum();
    }

    public List<User> getStaffPerformance() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STAFF"))
                .sorted((u1, u2) -> Integer.compare(getBookingCountForUser(u2), getBookingCountForUser(u1)))
                .collect(java.util.stream.Collectors.toList());
    }

    public int getBookingCountForUser(User user) {
        return (int) bookingRepository.findAll().stream()
                .filter(b -> b.getBookedBy() != null && b.getBookedBy().equals(user))
                .count();
    }

//    public int getBookingCountForShowing(Showing showing) {
//        return bookingRepository.findByShowing(showing).stream()
//                .filter(b -> !b.isCancelled())
//                .mapToInt(Booking::getNumTickets)
//                .sum();
//    }

    public java.util.Set<String> getReservedSeatsForShowing(Showing showing) {
        return bookingRepository.findByShowing(showing).stream()
                .filter(b -> !b.isCancelled())
                .filter(b -> b.getSeatNumbers() != null && !b.getSeatNumbers().isEmpty())
                .flatMap(b -> java.util.Arrays.stream(b.getSeatNumbers().split(",\\s*")))
                .collect(java.util.stream.Collectors.toSet());
    }
}
