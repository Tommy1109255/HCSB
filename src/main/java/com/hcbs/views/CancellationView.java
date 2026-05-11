package com.hcbs.views;

import com.hcbs.service.CinemaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "cancellation", layout = MainLayout.class)
@PageTitle("Cancel Booking | Horizon Cinemas")
@PermitAll
public class CancellationView extends VerticalLayout {

    public CancellationView(CinemaService cinemaService) {
        add(new H2("Cancel Your Booking"));

        TextField referenceInput = new TextField("Booking Reference");
        Button cancelBtn = new Button("Cancel Booking");

        cancelBtn.addClickListener(e -> {
            try {
                String result = cinemaService.cancelBooking(referenceInput.getValue());
                Notification.show(result);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });

        add(referenceInput, cancelBtn);
    }
}
