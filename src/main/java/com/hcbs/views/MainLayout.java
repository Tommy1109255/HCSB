package com.hcbs.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Horizon Cinemas");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(logo);
        
        authContext.getAuthenticatedUser(UserDetails.class).ifPresentOrElse(user -> {
            Button logout = new Button("Logout (" + user.getUsername() + ")", e -> authContext.logout());
            header.add(logout);
        }, () -> {
            Button login = new Button("Login", e -> getUI().ifPresent(ui -> ui.navigate("login")));
            header.add(login);
        });

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(new DrawerToggle(), header);
        setPrimarySection(Section.DRAWER);
    }

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        
        drawer.add(createMenuLink("Film Listing", com.vaadin.flow.component.icon.VaadinIcon.MOVIE, FilmListingView.class));
        drawer.add(createMenuLink("Book Tickets", com.vaadin.flow.component.icon.VaadinIcon.TICKET, BookingView.class));
        drawer.add(createMenuLink("Cancel Booking", com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE, CancellationView.class));
        
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_STAFF"))) {
                drawer.add(createMenuLink("Staff View", com.vaadin.flow.component.icon.VaadinIcon.USERS, StaffView.class));
            }
            if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
                drawer.add(createMenuLink("Admin Dashboard", com.vaadin.flow.component.icon.VaadinIcon.DASHBOARD, AdminView.class));
            }
            if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
                drawer.add(createMenuLink("Manager View", com.vaadin.flow.component.icon.VaadinIcon.COG, ManagerView.class));
            }
        });

        addToDrawer(drawer);
    }

    private RouterLink createMenuLink(String text, com.vaadin.flow.component.icon.VaadinIcon icon, Class<? extends com.vaadin.flow.component.Component> view) {
        RouterLink link = new RouterLink(view);
        link.add(icon.create());
        link.add(new com.vaadin.flow.component.html.Span(text));
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("gap", "var(--lumo-space-m)");
        return link;
    }
}
