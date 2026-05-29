package com.hcbs.views;

import com.hcbs.entity.Film;
import com.hcbs.repository.FilmRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@PageTitle("Now Showing | Horizon Cinemas")
@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class FilmListingView extends VerticalLayout {

    public FilmListingView(FilmRepository filmRepository) {
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Now Showing");
        title.getStyle().set("margin-bottom", "20px");
        add(title);

        FlexLayout container = new FlexLayout();
        container.setWidthFull();
        container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        container.setJustifyContentMode(JustifyContentMode.CENTER);
        container.getStyle().set("gap", "20px");

        List<Film> films = filmRepository.findAll();
        for (Film film : films) {
            container.add(createFilmCard(film));
        }

        add(container);
    }
    //卡片
    private VerticalLayout createFilmCard(Film film) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "15px")
                .set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1)")
                .set("background", "white")
                .set("cursor", "pointer");

        // Poster Image
        Image poster = new Image(film.getPosterUrl() != null ? film.getPosterUrl() : "https://via.placeholder.com/300x450?text=No+Poster", film.getTitle());
        poster.setWidthFull();
        poster.setHeight("400px");
        poster.getStyle().set("border-radius", "10px").set("object-fit", "cover");
        
        // Content
        H3 title = new H3(film.getTitle());
        title.getStyle().set("margin", "10px 0 5px 0");

        Span rating = new Span("⭐ " + (film.getRating() != null ? film.getRating() : "N/A"));
        rating.getStyle().set("color", "#f59e0b").set("font-weight", "bold");

        Span info = new Span(film.getGenre() + " | " + film.getDurationMinutes() + " mins");
        info.getStyle().set("color", "#64748b").set("font-size", "0.9em");

        Paragraph desc = new Paragraph(film.getDescription());
        desc.getStyle()
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical")
                .set("overflow", "hidden")
                .set("color", "#475569")
                .set("font-size", "0.85em");

        Button bookBtn = new Button("Book Now", e -> getUI().ifPresent(ui -> ui.navigate("booking")));
        bookBtn.setWidthFull();
        bookBtn.getStyle().set("background-color", "#2563eb").set("color", "white").set("border-radius", "8px");

        card.add(poster, title, new HorizontalLayout(rating, info), desc, bookBtn);
        return card;
    }
}
