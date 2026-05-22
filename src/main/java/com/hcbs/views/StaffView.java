package com.hcbs.views;

import com.hcbs.entity.Booking;
import com.hcbs.entity.Showing;
import com.hcbs.entity.User;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import com.hcbs.service.CinemaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;

@Route(value = "staff", layout = MainLayout.class)
@PageTitle("Staff Booking Management | Horizon Cinemas")
@RolesAllowed({"STAFF", "ADMIN", "MANAGER"})
public class StaffView extends VerticalLayout {

    private final BookingRepository bookingRepository;
    private final ShowingRepository showingRepository;
    private final UserRepository userRepository;
    private final CinemaService cinemaService;

    private Grid<Booking> grid = new Grid<>(Booking.class, false);

    public StaffView(BookingRepository bookingRepository, ShowingRepository showingRepository, 
                     UserRepository userRepository, CinemaService cinemaService) {
        this.bookingRepository = bookingRepository;
        this.showingRepository = showingRepository;
        this.userRepository = userRepository;
        this.cinemaService = cinemaService;

        add(new H2("Staff Ticket Management"));

        configureGrid();
        
        Button addBooking = new Button("New Booking (Help Customer)", e -> openBookingDialog());
        addBooking.getStyle().set("background-color", "#2563eb").set("color", "white");

        add(addBooking, grid);
        updateGrid();
    }

    private void configureGrid() {
        grid.addColumn(Booking::getReference).setHeader("Reference").setSortable(true);
        grid.addColumn(b -> b.getBookedBy() != null ? b.getBookedBy().getUsername() : "Guest").setHeader("Customer");
        grid.addColumn(b -> b.getFilm() != null ? b.getFilm().getTitle() : "").setHeader("Film");
        grid.addColumn(b -> "Screen " + (b.getScreen() != null ? b.getScreen().getScreenNumber() : "")).setHeader("Hall");
        grid.addColumn(b -> b.getShowing().getDate() + " " + b.getShowing().getStartTime()).setHeader("Showtime");
        grid.addColumn(Booking::getNumTickets).setHeader("Tickets");
        grid.addColumn(b -> "£" + b.getTotalCost()).setHeader("Total");
        grid.addColumn(b -> b.isCancelled() ? "Cancelled" : "Active").setHeader("Status");

        grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("200px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(Booking booking) {
        Button cancel = new Button("Cancel", e -> {
            try {
                String result = cinemaService.cancelBooking(booking.getReference());
                Notification.show(result);
                updateGrid();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });
        cancel.setEnabled(!booking.isCancelled());
        cancel.getStyle().set("color", "orange");

        Button delete = new Button("Delete", e -> {
            bookingRepository.delete(booking);
            Notification.show("Booking deleted");
            updateGrid();
        });
        delete.getStyle().set("color", "red");

        return new HorizontalLayout(cancel, delete);
    }

    private void updateGrid() {
        grid.setItems(bookingRepository.findAll());
    }

    private void openBookingDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Booking for Customer");

        VerticalLayout dialogLayout = new VerticalLayout();
        
        ComboBox<User> customerSelect = new ComboBox<>("Select Customer");
        customerSelect.setItems(userRepository.findAll());
        customerSelect.setItemLabelGenerator(User::getUsername);

        DatePicker datePicker = new DatePicker("Select Date");
        ComboBox<com.hcbs.entity.Film> filmSelect = new ComboBox<>("Select Film");
        ComboBox<com.hcbs.entity.Cinema> cinemaSelect = new ComboBox<>("Select Cinema");
        ComboBox<Showing> showingSelect = new ComboBox<>("Select Showing (Time & Hall)");

        datePicker.setValue(LocalDate.now());

        Runnable updateFilmOptions = () -> {
            if (datePicker.getValue() == null) {
                filmSelect.setItems(java.util.Collections.emptyList());
                return;
            }
            java.util.List<Showing> shows = showingRepository.findByDate(datePicker.getValue());
            java.util.List<com.hcbs.entity.Film> films = shows.stream().map(Showing::getFilm).distinct().collect(java.util.stream.Collectors.toList());
            filmSelect.setItems(films);
            filmSelect.clear();
            cinemaSelect.clear();
            showingSelect.clear();
        };

        datePicker.addValueChangeListener(e -> updateFilmOptions.run());
        filmSelect.setItemLabelGenerator(com.hcbs.entity.Film::getTitle);

        filmSelect.addValueChangeListener(e -> {
            com.hcbs.entity.Film selectedFilm = e.getValue();
            if (selectedFilm != null && datePicker.getValue() != null) {
                java.util.List<Showing> shows = showingRepository.findByDate(datePicker.getValue());
                java.util.List<com.hcbs.entity.Cinema> cinemas = shows.stream()
                        .filter(s -> s.getFilm().equals(selectedFilm))
                        .map(s -> s.getScreen().getCinema())
                        .distinct().collect(java.util.stream.Collectors.toList());
                cinemaSelect.setItems(cinemas);
            } else {
                cinemaSelect.setItems(java.util.Collections.emptyList());
            }
            cinemaSelect.clear();
            showingSelect.clear();
        });

        cinemaSelect.setItemLabelGenerator(com.hcbs.entity.Cinema::getName);
        cinemaSelect.addValueChangeListener(e -> {
            com.hcbs.entity.Film selectedFilm = filmSelect.getValue();
            com.hcbs.entity.Cinema selectedCinema = e.getValue();
            if (selectedFilm != null && selectedCinema != null && datePicker.getValue() != null) {
                java.util.List<Showing> shows = showingRepository.findByDate(datePicker.getValue());
                java.util.List<Showing> filtered = shows.stream()
                        .filter(s -> s.getFilm().equals(selectedFilm) && s.getScreen().getCinema().equals(selectedCinema))
                        .collect(java.util.stream.Collectors.toList());
                showingSelect.setItems(filtered);
            } else {
                showingSelect.setItems(java.util.Collections.emptyList());
            }
            showingSelect.clear();
        });

        showingSelect.setItemLabelGenerator(s -> s.getStartTime() + " - Screen " + s.getScreen().getScreenNumber());

        updateFilmOptions.run();

        IntegerField tickets = new IntegerField("Number of Tickets");
        tickets.setMin(1);
        tickets.setValue(1);

        TextField seats = new TextField("Seat Numbers (e.g. A1, A2)");

        Button save = new Button("Create Booking", e -> {
            if (customerSelect.getValue() == null || showingSelect.getValue() == null) {
                Notification.show("Please select customer and showing");
                return;
            }
            try {
                cinemaService.createBooking(showingSelect.getValue(), tickets.getValue(), seats.getValue(), customerSelect.getValue());
                Notification.show("Booking created successfully!");
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        dialogLayout.add(customerSelect, datePicker, filmSelect, cinemaSelect, showingSelect, tickets, seats);
        dialog.add(dialogLayout);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }
}
