package com.hcbs.views;

import com.hcbs.entity.*;
import com.hcbs.repository.*;
import com.hcbs.service.CinemaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalTime;
import java.util.Collections;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin Dashboard | Horizon Cinemas")
@RolesAllowed({"ADMIN", "MANAGER"})
public class AdminView extends VerticalLayout {

    private final FilmRepository filmRepository;
    private final CinemaService cinemaService;
    private final CinemaRepository cinemaRepository;
    private final ShowingRepository showingRepository;
    private final ScreenRepository screenRepository;

    private VerticalLayout content = new VerticalLayout();
    private Grid<Film> filmGrid = new Grid<>(Film.class, false);
    private Binder<Film> filmBinder = new Binder<>(Film.class);
    private Grid<Showing> showingGrid = new Grid<>(Showing.class, false);

    public AdminView(FilmRepository filmRepository, CinemaService cinemaService, 
                     CinemaRepository cinemaRepository, ShowingRepository showingRepository, 
                     ScreenRepository screenRepository) {
        this.filmRepository = filmRepository;
        this.cinemaService = cinemaService;
        this.cinemaRepository = cinemaRepository;
        this.showingRepository = showingRepository;
        this.screenRepository = screenRepository;

        add(new H2("Admin Dashboard"));

        Tabs tabs = new Tabs(
            new Tab("Manage Films"),
            new Tab("Manage Showings"),
            new Tab("Reports")
        );
        tabs.addSelectedChangeListener(e -> {
            updateContent(tabs.getSelectedIndex());
        });

        configureFilmGrid();
        add(tabs, content);
        updateContent(0); // Default to Manage Films
    }

    private void configureFilmGrid() {
        filmGrid.addColumn(Film::getTitle).setHeader("Title").setSortable(true);
        filmGrid.addColumn(Film::getGenre).setHeader("Genre");
        filmGrid.addColumn(Film::getAgeRating).setHeader("Age Rating");
        filmGrid.addColumn(Film::getDurationMinutes).setHeader("Duration (mins)");
        filmGrid.asSingleSelect().addValueChangeListener(e -> editFilm(e.getValue()));
    }

    private void updateContent(int index) {
        content.removeAll();
        if (index == 0) {
            renderFilmManagement();
        } else if (index == 1) {
            renderShowingManagement();
        } else {
            renderReports();
        }
    }

    private void renderFilmManagement() {
        content.add(new H3("Film List"));
        
        filmGrid.setItems(filmRepository.findAll());

        Button addNew = new Button("Add New Film", e -> editFilm(new Film()));
        
        content.add(addNew, filmGrid);
    }

    private void editFilm(Film film) {
        if (film == null) return;

        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();
        
        TextField title = new TextField("Title");
        TextArea desc = new TextArea("Description");
        TextField genre = new TextField("Genre");
        TextField actors = new TextField("Actors");
        TextField director = new TextField("Director");
        TextField posterUrl = new TextField("Poster URL");
        NumberField rating = new NumberField("Rating");
        TextField age = new TextField("Age Rating");
        IntegerField duration = new IntegerField("Duration (mins)");

        filmBinder.forField(title).bind(Film::getTitle, Film::setTitle);
        filmBinder.forField(desc).bind(Film::getDescription, Film::setDescription);
        filmBinder.forField(genre).bind(Film::getGenre, Film::setGenre);
        filmBinder.forField(actors).bind(Film::getActors, Film::setActors);
        filmBinder.forField(director).bind(Film::getDirector, Film::setDirector);
        filmBinder.forField(posterUrl).bind(Film::getPosterUrl, Film::setPosterUrl);
        filmBinder.forField(rating).bind(Film::getRating, Film::setRating);
        filmBinder.forField(age).bind(Film::getAgeRating, Film::setAgeRating);
        filmBinder.forField(duration).bind(Film::getDurationMinutes, Film::setDurationMinutes);

        filmBinder.setBean(film);

        Button save = new Button("Save", e -> {
            filmRepository.save(filmBinder.getBean());
            filmGrid.setItems(filmRepository.findAll());
            dialog.close();
            Notification.show("Film saved!");
        });
        
        Button delete = new Button("Delete", e -> {
            filmRepository.delete(filmBinder.getBean());
            filmGrid.setItems(filmRepository.findAll());
            dialog.close();
            Notification.show("Film removed!");
        });
        delete.getStyle().set("color", "red");

        form.add(title, desc, genre, actors, director, posterUrl, rating, age, duration);
        dialog.add(new H3(film.getId() == null ? "Add Film" : "Edit Film"), form, new HorizontalLayout(save, delete));
        dialog.open();
    }

    private void renderShowingManagement() {
        content.add(new H3("Showings Schedule (放映排片管理)"));
        
        Button addShowing = new Button("Schedule New Showing (添加放映排片)", e -> openAddShowingDialog());
        addShowing.getStyle().set("background-color", "#10b981").set("color", "white");
        
        configureShowingGrid();
        showingGrid.setItems(showingRepository.findAll());
        
        content.add(addShowing, showingGrid);
    }

    private void configureShowingGrid() {
        showingGrid.removeAllColumns();
        showingGrid.addColumn(s -> s.getFilm() != null ? s.getFilm().getTitle() : "").setHeader("Film").setSortable(true);
        showingGrid.addColumn(s -> s.getScreen() != null && s.getScreen().getCinema() != null ? s.getScreen().getCinema().getName() : "").setHeader("Cinema");
        showingGrid.addColumn(s -> s.getScreen() != null ? "Screen " + s.getScreen().getScreenNumber() : "").setHeader("Screen");
        showingGrid.addColumn(Showing::getDate).setHeader("Date").setSortable(true);
        showingGrid.addColumn(Showing::getStartTime).setHeader("Time");
        showingGrid.addColumn(s -> "£" + s.getPriceLowerHall()).setHeader("Lower Hall Price");
        showingGrid.addColumn(s -> "£" + s.getPriceGallery()).setHeader("Gallery Price");
        showingGrid.addColumn(s -> s.getRemainingSeats() + " / " + (s.getScreen() != null ? s.getScreen().getCapacity() : 0)).setHeader("Seats");
        
        showingGrid.addComponentColumn(s -> {
            Button deleteBtn = new Button("Delete", clickEvent -> {
                showingRepository.delete(s);
                showingGrid.setItems(showingRepository.findAll());
                Notification.show("Showing deleted!");
            });
            deleteBtn.getStyle().set("color", "red");
            return deleteBtn;
        }).setHeader("Actions");
    }

    private void openAddShowingDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Schedule New Showing");

        FormLayout form = new FormLayout();
        
        ComboBox<Film> filmSelect = new ComboBox<>("Select Film");
        filmSelect.setItems(filmRepository.findAll());
        filmSelect.setItemLabelGenerator(Film::getTitle);
        
        ComboBox<Cinema> cinemaSelect = new ComboBox<>("Select Cinema");
        cinemaSelect.setItems(cinemaRepository.findAll());
        cinemaSelect.setItemLabelGenerator(Cinema::getName);
        
        ComboBox<Screen> screenSelect = new ComboBox<>("Select Screen");
        screenSelect.setItemLabelGenerator(s -> "Screen " + s.getScreenNumber() + " (Capacity: " + s.getCapacity() + ")");
        
        cinemaSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                screenSelect.setItems(screenRepository.findByCinema(e.getValue()));
            } else {
                screenSelect.setItems(Collections.emptyList());
            }
            screenSelect.clear();
        });

        DatePicker datePicker = new DatePicker("Show Date");
        datePicker.setMin(java.time.LocalDate.now());
        
        TimePicker timePicker = new TimePicker("Start Time");
        timePicker.setStep(java.time.Duration.ofMinutes(15));
        
        NumberField priceLower = new NumberField("Price Lower Hall (£)");
        priceLower.setValue(6.0);
        priceLower.setMin(0.0);
        
        NumberField priceGallery = new NumberField("Price Gallery (£)");
        priceGallery.setValue(8.0);
        priceGallery.setMin(0.0);

        form.add(filmSelect, cinemaSelect, screenSelect, datePicker, timePicker, priceLower, priceGallery);
        
        Button save = new Button("Schedule", clickEvent -> {
            if (filmSelect.getValue() == null || screenSelect.getValue() == null || 
                datePicker.getValue() == null || timePicker.getValue() == null || 
                priceLower.getValue() == null || priceGallery.getValue() == null) {
                Notification.show("Please fill in all fields!");
                return;
            }
            
            Showing showing = Showing.builder()
                    .film(filmSelect.getValue())
                    .screen(screenSelect.getValue())
                    .date(datePicker.getValue())
                    .startTime(timePicker.getValue())
                    .priceLowerHall(priceLower.getValue())
                    .priceGallery(priceGallery.getValue())
                    .remainingSeats(screenSelect.getValue().getCapacity())
                    .build();
                    
            showingRepository.save(showing);
            showingGrid.setItems(showingRepository.findAll());
            dialog.close();
            Notification.show("New showing scheduled successfully!");
        });
        
        Button cancel = new Button("Cancel", clickEvent -> dialog.close());
        
        dialog.add(form);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void renderReports() {
        content.add(new H3("Business Reports"));

        // 1. Top Film
        Film topFilm = cinemaService.getTopRevenueFilm();
        if (topFilm != null) {
            content.add(new Div(new Span("Top Revenue Film: "), new Span(topFilm.getTitle())));
        }

        // 2. Revenue per Cinema
        content.add(new H4("Revenue per Cinema"));
        Grid<Cinema> cinemaRevenueGrid = new Grid<>(Cinema.class, false);
        cinemaRevenueGrid.setItems(cinemaRepository.findAll());
        cinemaRevenueGrid.addColumn(Cinema::getName).setHeader("Cinema Name");
        cinemaRevenueGrid.addColumn(c -> "£" + cinemaService.getTotalRevenueForCinema(c)).setHeader("Total Revenue");
        content.add(cinemaRevenueGrid);

        // 3. Staff Performance
        content.add(new H4("Staff Performance (Bookings this Month)"));
        Grid<User> staffGrid = new Grid<>(User.class, false);
        staffGrid.setItems(cinemaService.getStaffPerformance());
        staffGrid.addColumn(User::getUsername).setHeader("Username");
        staffGrid.addColumn(u -> cinemaService.getBookingCountForUser(u)).setHeader("Bookings Count");
        content.add(staffGrid);
    }
}
