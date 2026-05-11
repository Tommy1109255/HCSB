package com.hcbs.views;

import com.hcbs.entity.Cinema;
import com.hcbs.entity.City;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.CityRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "manager", layout = MainLayout.class)
@PageTitle("Manager View | Horizon Cinemas")
@RolesAllowed("MANAGER")
public class ManagerView extends VerticalLayout {

    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;

    private Grid<City> cityGrid = new Grid<>(City.class, false);
    private Grid<Cinema> cinemaGrid = new Grid<>(Cinema.class, false);

    public ManagerView(CityRepository cityRepository, CinemaRepository cinemaRepository) {
        this.cityRepository = cityRepository;
        this.cinemaRepository = cinemaRepository;

        add(new H2("Manager View - Strategic Management"));

        renderCitySection();
        renderCinemaSection();
    }

    private void renderCitySection() {
        add(new H3("Manage Cities"));
        TextField cityName = new TextField("New City Name");
        Button addCity = new Button("Add City", e -> {
            if (!cityName.isEmpty()) {
                cityRepository.save(City.builder().name(cityName.getValue()).build());
                cityName.clear();
                updateGrids();
                Notification.show("City added!");
            }
        });

        HorizontalLayout form = new HorizontalLayout(cityName, addCity);
        form.setVerticalComponentAlignment(Alignment.BASELINE, addCity);
        
        cityGrid.setItems(cityRepository.findAll());
        cityGrid.addColumn(City::getId).setHeader("ID");
        cityGrid.addColumn(City::getName).setHeader("Name");
        
        add(form, cityGrid);
    }

    private void renderCinemaSection() {
        add(new H3("Manage Cinemas"));
        
        TextField cinemaName = new TextField("Cinema Name");
        ComboBox<City> citySelect = new ComboBox<>("City");
        citySelect.setItems(cityRepository.findAll());
        citySelect.setItemLabelGenerator(City::getName);

        Button addCinema = new Button("Add Cinema", e -> {
            if (!cinemaName.isEmpty() && citySelect.getValue() != null) {
                cinemaRepository.save(Cinema.builder()
                        .name(cinemaName.getValue())
                        .city(citySelect.getValue())
                        .build());
                cinemaName.clear();
                updateGrids();
                Notification.show("Cinema added!");
            }
        });

        HorizontalLayout form = new HorizontalLayout(cinemaName, citySelect, addCinema);
        form.setVerticalComponentAlignment(Alignment.BASELINE, addCinema);

        cinemaGrid.setItems(cinemaRepository.findAll());
        cinemaGrid.addColumn(Cinema::getId).setHeader("ID");
        cinemaGrid.addColumn(Cinema::getName).setHeader("Name");
        cinemaGrid.addColumn(c -> c.getCity().getName()).setHeader("City");

        add(form, cinemaGrid);
    }

    private void updateGrids() {
        cityGrid.setItems(cityRepository.findAll());
        cinemaGrid.setItems(cinemaRepository.findAll());
    }
}
