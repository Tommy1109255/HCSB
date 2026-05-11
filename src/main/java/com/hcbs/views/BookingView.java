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
import com.vaadin.flow.component.textfield.TextField;
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

    private ComboBox<Showing> showingSelect = new ComboBox<>("Select Showing");
    private DatePicker datePicker = new DatePicker("Select Date");
    private IntegerField ticketCount = new IntegerField("Number of Tickets");
    private TextField seats = new TextField("Seat Numbers (e.g. A1, A2)");
    private Button bookButton = new Button("Book Now");
    private Span capacityInfo = new Span();

    public BookingView(CinemaService cinemaService, ShowingRepository showingRepository, 
                       UserRepository userRepository, AuthenticationContext authContext) {
        this.cinemaService = cinemaService;
        this.showingRepository = showingRepository;
        this.userRepository = userRepository;
        this.authContext = authContext;

        add(new H2("Ticket Booking"));

        datePicker.setValue(LocalDate.now());
        datePicker.addValueChangeListener(e -> updateShowings());

        showingSelect.setItemLabelGenerator(s -> 
            s.getFilm().getTitle() + " - " + s.getStartTime() + " (" + s.getScreen().getCinema().getName() + ")");
        showingSelect.setWidth("400px");
        showingSelect.addValueChangeListener(e -> updateCapacityInfo());

        ticketCount.setMin(1);
        ticketCount.setMax(10);
        ticketCount.setValue(1);

        updateShowings();

        bookButton.addClickListener(e -> handleBooking());

        add(datePicker, showingSelect, capacityInfo, ticketCount, seats, bookButton);
    }

    private void updateShowings() {
        if (datePicker.getValue() != null) {
            showingSelect.setItems(showingRepository.findByDate(datePicker.getValue()));
        } else {
            showingSelect.setItems(Collections.emptyList());
        }
        updateCapacityInfo();
    }

    private void updateCapacityInfo() {
        Showing s = showingSelect.getValue();
        if (s != null) {
            int booked = cinemaService.getBookingCountForShowing(s);
            int available = s.getScreen().getCapacity() - booked;
            capacityInfo.setText("Available Seats: " + available + " / " + s.getScreen().getCapacity());
            capacityInfo.getStyle().set("color", available > 0 ? "green" : "red");
            capacityInfo.getStyle().set("font-weight", "bold");
        } else {
            capacityInfo.setText("");
        }
    }

    private void handleBooking() {
        if (showingSelect.getValue() == null || seats.isEmpty()) {
            Notification.show("Please fill all fields");
            return;
        }

        UserDetails userDetails = authContext.getAuthenticatedUser(UserDetails.class).get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();

        Booking booking = cinemaService.createBooking(
                showingSelect.getValue(),
                ticketCount.getValue(),
                seats.getValue(),
                user
        );

        showReceipt(booking);
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
