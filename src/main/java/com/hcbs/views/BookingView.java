package com.hcbs.views;

import com.hcbs.entity.Booking;
import com.hcbs.entity.Showing;
import com.hcbs.entity.User;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import com.hcbs.service.CinemaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collections;

@Route(value = "booking", layout = MainLayout.class)
@PageTitle("Book Tickets | Horizon Cinemas")
@PermitAll
public class BookingView extends VerticalLayout {

    private final CinemaService cinemaService;
    private final ShowingRepository showingRepository;
    private final UserRepository userRepository;
    private final AuthenticationContext authContext;
    private DatePicker datePicker = new DatePicker("Select Date");
    private ComboBox<com.hcbs.entity.Film> filmSelect = new ComboBox<>("Select Film");
    private ComboBox<com.hcbs.entity.Cinema> cinemaSelect = new ComboBox<>("Select Cinema");
    private ComboBox<Showing> showingSelect = new ComboBox<>("Select Showing (Time & Hall)");

    private IntegerField ticketCount = new IntegerField("Number of Tickets");
    private SeatSelector seatSelector;
    private Button bookButton = new Button("Book Now");
    private Span capacityInfo = new Span();
    
    private java.util.List<Showing> showingsForDate = Collections.emptyList();

    public BookingView(CinemaService cinemaService, ShowingRepository showingRepository, 
                       UserRepository userRepository, AuthenticationContext authContext) {
        this.cinemaService = cinemaService;
        this.showingRepository = showingRepository;
        this.userRepository = userRepository;
        this.authContext = authContext;

        add(new H2("Ticket Booking"));

        datePicker.setValue(LocalDate.now());
        datePicker.addValueChangeListener(e -> loadShowingsForDate());

        filmSelect.setItemLabelGenerator(com.hcbs.entity.Film::getTitle);
        filmSelect.addValueChangeListener(e -> updateCinemaOptions());
        
        cinemaSelect.setItemLabelGenerator(com.hcbs.entity.Cinema::getName);
        cinemaSelect.addValueChangeListener(e -> updateShowingOptions());

        showingSelect.setItemLabelGenerator(s -> 
            s.getStartTime() + " - Screen " + s.getScreen().getScreenNumber());
        showingSelect.setWidth("400px");
        showingSelect.addValueChangeListener(e -> updateCapacityInfo());

        ticketCount.setReadOnly(true);

        seatSelector = new SeatSelector(0, 0, null, selected -> {
            ticketCount.setValue(selected.size());
            bookButton.setEnabled(!selected.isEmpty());
        });

        loadShowingsForDate();

        bookButton.addClickListener(e -> handleBooking());
        bookButton.setEnabled(false);

        add(datePicker, filmSelect, cinemaSelect, showingSelect, capacityInfo, ticketCount, seatSelector, bookButton);
    }

    private void loadShowingsForDate() {
        if (datePicker.getValue() != null) {
            showingsForDate = showingRepository.findByDate(datePicker.getValue());
        } else {
            showingsForDate = Collections.emptyList();
        }
        updateFilmOptions();
    }

    private void updateFilmOptions() {
        java.util.List<com.hcbs.entity.Film> films = showingsForDate.stream()
            .map(Showing::getFilm)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        filmSelect.setItems(films);
        filmSelect.clear();
        cinemaSelect.clear();
        showingSelect.clear();
    }
    
    private void updateCinemaOptions() {
        com.hcbs.entity.Film selectedFilm = filmSelect.getValue();
        if (selectedFilm != null) {
            java.util.List<com.hcbs.entity.Cinema> cinemas = showingsForDate.stream()
                .filter(s -> s.getFilm().equals(selectedFilm))
                .map(s -> s.getScreen().getCinema())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            cinemaSelect.setItems(cinemas);
        } else {
            cinemaSelect.setItems(Collections.emptyList());
        }
        cinemaSelect.clear();
        showingSelect.clear();
    }

    private void updateShowingOptions() {
        com.hcbs.entity.Film selectedFilm = filmSelect.getValue();
        com.hcbs.entity.Cinema selectedCinema = cinemaSelect.getValue();
        
        if (selectedFilm != null && selectedCinema != null) {
            java.util.List<Showing> filteredShowings = showingsForDate.stream()
                .filter(s -> s.getFilm().equals(selectedFilm))
                .filter(s -> s.getScreen().getCinema().equals(selectedCinema))
                .collect(java.util.stream.Collectors.toList());
            showingSelect.setItems(filteredShowings);
        } else {
            showingSelect.setItems(Collections.emptyList());
        }
        showingSelect.clear();
    }

    private void updateCapacityInfo() {
        Showing s = showingSelect.getValue();
        if (s != null) {
            int available = s.getRemainingSeats();
            capacityInfo.setText("Available Seats: " + available + " / " + s.getScreen().getCapacity());
            capacityInfo.getStyle().set("color", available > 0 ? "green" : "red");
            capacityInfo.getStyle().set("font-weight", "bold");
            
            // Update seat selector
            java.util.Set<String> reserved = cinemaService.getReservedSeatsForShowing(s);
            seatSelector.setDimensions(s.getScreen().getRows(), s.getScreen().getColumns());
            seatSelector.setReservedSeats(reserved);
        } else {
            capacityInfo.setText("");
            seatSelector.setDimensions(0, 0);
        }
    }

    private void handleBooking() {
        if (showingSelect.getValue() == null || seatSelector.getSelectedSeats().isEmpty()) {
            Notification.show("Please select a showing and at least one seat");
            return;
        }

        UserDetails userDetails = authContext.getAuthenticatedUser(UserDetails.class).get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();

        String selectedSeatsStr = String.join(", ", seatSelector.getSelectedSeats());

        Booking booking = cinemaService.createBooking(
                showingSelect.getValue(),
                seatSelector.getSelectedSeats().size(),
                selectedSeatsStr,
                user
        );

        showReceipt(booking);
        updateCapacityInfo(); // Refresh seat map after booking
    }

    private void showReceipt(Booking b) {
        //创建了一个新的、空的对话框（弹窗）实例
        Dialog dialog = new Dialog();
        dialog.add(new H2("Booking Receipt"));
        dialog.add(new Paragraph("Reference: " + b.getReference()));
        dialog.add(new Paragraph("Film: " + b.getShowing().getFilm().getTitle()));
        dialog.add(new Paragraph("Date: " + b.getShowing().getDate()));
        dialog.add(new Paragraph("Time: " + b.getShowing().getStartTime()));
        dialog.add(new Paragraph("Screen: " + b.getShowing().getScreen().getScreenNumber()));
        dialog.add(new Paragraph("Tickets: " + b.getNumTickets()));
        dialog.add(new Paragraph("Seats: " + b.getSeatNumbers()));
        dialog.add(new Paragraph("Total Cost: £" + b.getTotalCost()));
        
        Button close = new Button("Close", e -> dialog.close());
        dialog.add(close);
        dialog.open();
    }
}
