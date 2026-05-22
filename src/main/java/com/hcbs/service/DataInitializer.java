package com.hcbs.service;

import com.hcbs.entity.*;
import com.hcbs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final FilmRepository filmRepository;
    private final ShowingRepository showingRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing data...");
        
        // 1. Create Users
        if (userRepository.count() == 0) {
            userRepository.save(User.builder().username("admin").password(passwordEncoder.encode("admin")).roles(new HashSet<>(Arrays.asList("ADMIN", "STAFF"))).build());
            userRepository.save(User.builder().username("manager").password(passwordEncoder.encode("manager")).roles(new HashSet<>(Arrays.asList("MANAGER"))).build());
            userRepository.save(User.builder().username("staff").password(passwordEncoder.encode("staff")).roles(new HashSet<>(Arrays.asList("STAFF"))).build());
            userRepository.save(User.builder().username("guo").password(passwordEncoder.encode("123456")).roles(new HashSet<>(Arrays.asList("CUSTOMER"))).build());
        }

        // 2. Create Cities and Cinemas
        if (cityRepository.count() == 0) {
            List<String> cityNames = Arrays.asList("Birmingham", "Bristol", "Cardiff", "London");
            for (String cityName : cityNames) {
                City city = cityRepository.save(City.builder().name(cityName).build());
                
                for (int i = 1; i <= 2; i++) {
                    Cinema cinema = cinemaRepository.save(Cinema.builder().name(cityName + " Cinema " + i).city(city).build());
                    
                    for (int s = 1; s <= 6; s++) {
                        int cap = 50 + (s * 10);
                        int rows = 8;
                        int cols = cap / rows;
                        screenRepository.save(Screen.builder()
                                .screenNumber(s)
                                .capacity(cap)
                                .rows(rows)
                                .columns(cols)
                                .cinema(cinema)
                                .build());
                    }
                }
            }
        }

        // 3. Create Films
        if (filmRepository.count() == 0) {
            filmRepository.save(Film.builder()
                    .title("Oppenheimer")
                    .description("The story of J. Robert Oppenheimer and his role in the development of the atomic bomb.")
                    .genre("Biography/Drama")
                    .director("Christopher Nolan")
                    .actors("Cillian Murphy, Emily Blunt")
                    .ageRating("15")
                    .durationMinutes(180)
                    .posterUrl("https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&h=600&fit=crop")
                    .releaseDate(LocalDate.of(2023, 7, 21))
                    .rating(8.4)
                    .build());
            
            filmRepository.save(Film.builder()
                    .title("Barbie")
                    .description("Barbie suffers a crisis that leads her to question her world and her existence.")
                    .genre("Adventure/Comedy")
                    .director("Greta Gerwig")
                    .actors("Margot Robbie, Ryan Gosling")
                    .ageRating("12A")
                    .durationMinutes(114)
                    .posterUrl("https://images.unsplash.com/photo-1601513445506-2ab0d4fb4229?w=400&h=600&fit=crop")
                    .releaseDate(LocalDate.of(2023, 7, 21))
                    .rating(7.0)
                    .build());

            filmRepository.save(Film.builder()
                    .title("Dune: Part Two")
                    .description("Paul Atreides unites with Chani and the Fremen while on a warpath of revenge.")
                    .genre("Sci-Fi/Action")
                    .director("Denis Villeneuve")
                    .actors("Timothée Chalamet, Zendaya")
                    .ageRating("12A")
                    .durationMinutes(166)
                    .posterUrl("https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=400&h=600&fit=crop")
                    .releaseDate(LocalDate.of(2024, 3, 1))
                    .rating(8.6)
                    .build());
        }

        // 4. Create Showings for today and next week
        LocalDate today = LocalDate.now();
        boolean hasFutureShowings = showingRepository.findAll().stream()
                .anyMatch(s -> !s.getDate().isBefore(today));
        
        if (showingRepository.count() == 0 || !hasFutureShowings) {
            System.out.println("Re-initializing showing dates to be current (starting from " + today + ")...");
            
            // Delete bookings first due to foreign keys referencing showing
            bookingRepository.deleteAll();
            showingRepository.deleteAll();
            
            List<Screen> screens = screenRepository.findAll();
            List<Film> films = filmRepository.findAll();
            
            if (!screens.isEmpty() && !films.isEmpty()) {
                Film f1 = films.get(0);
                Film f2 = films.size() > 1 ? films.get(1) : f1;
                Film f3 = films.size() > 2 ? films.get(2) : f1;
                
                for (int day = 0; day < 7; day++) {
                    LocalDate date = today.plusDays(day);
                    for (int i = 0; i < 5; i++) { // Sample 5 showings per day
                        Screen screen = screens.get(i % screens.size());
                        LocalTime time = LocalTime.of(10 + (i * 3), 0);
                        Film film = (i % 3 == 0) ? f1 : (i % 3 == 1) ? f2 : f3;
                        
                        double price = calculatePrice(screen.getCinema().getCity().getName(), time);
                        
                        showingRepository.save(Showing.builder()
                                .film(film)
                                .screen(screen)
                                .date(date)
                                .startTime(time)
                                .priceLowerHall(price)
                                .priceGallery(price + 2.0)
                                .remainingSeats(screen.getCapacity())
                                .build());
                    }
                }
            }
        }
    }

    private double calculatePrice(String cityName, LocalTime time) {
        int hour = time.getHour();
        if (cityName.equalsIgnoreCase("Birmingham") || cityName.equalsIgnoreCase("Cardiff")) {
            if (hour < 12) return 5.0;
            if (hour < 17) return 6.0;
            return 7.0;
        } else if (cityName.equalsIgnoreCase("Bristol")) {
            if (hour < 12) return 6.0;
            if (hour < 17) return 7.0;
            return 8.0;
        } else if (cityName.equalsIgnoreCase("London")) {
            if (hour < 12) return 10.0;
            if (hour < 17) return 11.0;
            return 12.0;
        }
        return 7.0;
    }
}
