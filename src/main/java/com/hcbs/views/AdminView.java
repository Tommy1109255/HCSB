package com.hcbs.views;

import com.hcbs.entity.Cinema;
import com.hcbs.entity.Film;
import com.hcbs.entity.User;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.FilmRepository;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin Dashboard | Horizon Cinemas")
@RolesAllowed({"ADMIN", "MANAGER"})
public class AdminView extends VerticalLayout {

    private final FilmRepository filmRepository;
    private final CinemaService cinemaService;
    private final CinemaRepository cinemaRepository;

    private VerticalLayout content = new VerticalLayout();
    private Grid<Film> filmGrid = new Grid<>(Film.class, false);
    private Binder<Film> filmBinder = new Binder<>(Film.class);

    public AdminView(FilmRepository filmRepository, CinemaService cinemaService, CinemaRepository cinemaRepository) {
        this.filmRepository = filmRepository;
        this.cinemaService = cinemaService;
        this.cinemaRepository = cinemaRepository;

        add(new H2("Admin Dashboard"));

        Tabs tabs = new Tabs(
            new Tab("Manage Films"),
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
