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
            // 1. 火遮眼（2026 最热华语动作片）
            filmRepository.save(Film.builder()
                    .title("火遮眼")
                    .description("失语维修工王伟为营救被绑架的女儿，独自踏上血腥复仇之路，拳拳到肉的硬核动作场面。")
                    .genre("动作/犯罪")
                    .director("谷垣健治")
                    .actors("谢苗、杨恩又、雅彦·鲁伊安")
                    .ageRating("18+")
                    .durationMinutes(108)
                    .posterUrl("https://q3.itc.cn/q_70/images03/20260527/d7c30e51a5bb4153a3a49d84fbfe4d29.jpeg")
                    .releaseDate(LocalDate.of(2026, 6, 11))
                    .rating(8.9)
                    .build());

            // 2. 挽救计划（2026 好莱坞科幻爆款）
            filmRepository.save(Film.builder()
                    .title("挽救计划")
                    .description("太阳即将毁灭，一名宇航员与外星伙伴联手展开跨物种拯救地球的硬核科幻冒险。")
                    .genre("科幻/冒险")
                    .director("菲尔·罗德、克里斯托弗·米勒")
                    .actors("瑞恩·高斯林、桑德拉·惠勒")
                    .ageRating("12A")
                    .durationMinutes(130)
                    .posterUrl("https://img0.baidu.com/it/u=1725255890,1125476600&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=696")
                    .releaseDate(LocalDate.of(2026, 3, 20))
                    .rating(7.8)
                    .build());

            // 3. 给阿嬷的情书（2026 华语口碑冠军）
            filmRepository.save(Film.builder()
                    .title("给阿嬷的情书")
                    .description("一封跨越时光的家书，讲述祖孙之间最纯粹动人的亲情，治愈又催泪。")
                    .genre("剧情/家庭")
                    .director("蓝鸿春")
                    .actors("李思潼、王彦桐、郑润奇")
                    .ageRating("PG")
                    .durationMinutes(115)
                    .posterUrl("https://q4.itc.cn/q_70/images03/20260425/5503f8c5f0854728bbd31d26552dc4e5.jpeg")
                    .releaseDate(LocalDate.of(2026, 4, 30))
                    .rating(9.2)
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
