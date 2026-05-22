package com.hcbs.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashSet;
import java.util.Set;

public class SeatSelector extends Composite<VerticalLayout> {

    private int rows;
    private int cols;
    private Set<String> reservedSeats = new HashSet<>();
    private Set<String> selectedSeats = new HashSet<>();
    private SeatSelectionListener listener;

    public interface SeatSelectionListener {
        void onSelectionChange(Set<String> selectedSeats);
    }

    public SeatSelector(int rows, int cols, Set<String> reservedSeats, SeatSelectionListener listener) {
        this.rows = rows;
        this.cols = cols;
        this.reservedSeats = reservedSeats != null ? reservedSeats : new HashSet<>();
        this.listener = listener;
        updateLayout();
    }

    public void setReservedSeats(Set<String> reservedSeats) {
        this.reservedSeats = reservedSeats != null ? reservedSeats : new HashSet<>();
        this.selectedSeats.clear();
        updateLayout();
    }

    private void updateLayout() {
        VerticalLayout layout = getContent();
        layout.removeAll();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);
        layout.setSpacing(false);

        if (rows == 0 || cols == 0) {
            layout.add(new Span("Select a showing to see seat map"));
            return;
        }

        // Screen
        Div screen = new Div();
        screen.setText("SCREEN");
        screen.getStyle().set("width", "300px");
        screen.getStyle().set("height", "10px");
        screen.getStyle().set("background", "#333");
        screen.getStyle().set("color", "white");
        screen.getStyle().set("text-align", "center");
        screen.getStyle().set("margin-bottom", "30px");
        screen.getStyle().set("border-radius", "0 0 20px 20px");
        screen.getStyle().set("font-size", "10px");
        screen.getStyle().set("line-height", "10px");
        layout.add(screen);

        for (int r = 0; r < rows; r++) {
            HorizontalLayout rowLayout = new HorizontalLayout();
            rowLayout.setSpacing(true);
            char rowChar = (char) ('A' + r);
            
            Span rowLabel = new Span(String.valueOf(rowChar));
            rowLabel.setWidth("20px");
            rowLabel.getStyle().set("font-weight", "bold");
            rowLayout.add(rowLabel);

            for (int c = 1; c <= cols; c++) {
                String seatId = rowChar + String.valueOf(c);
                Div seat = new Div();
                seat.setWidth("25px");
                seat.setHeight("25px");
                seat.getStyle().set("margin", "2px");
                seat.getStyle().set("border-radius", "4px");
                seat.getStyle().set("cursor", "pointer");
                seat.getStyle().set("display", "flex");
                seat.getStyle().set("align-items", "center");
                seat.getStyle().set("justify-content", "center");
                seat.getStyle().set("font-size", "9px");
                seat.getStyle().set("transition", "all 0.2s");
                seat.setText(String.valueOf(c));

                if (reservedSeats.contains(seatId)) {
                    seat.getStyle().set("background", "#e0e0e0"); // Light gray for reserved
                    seat.getStyle().set("color", "#999");
                    seat.getStyle().set("cursor", "not-allowed");
                } else if (selectedSeats.contains(seatId)) {
                    seat.getStyle().set("background", "#52c41a"); // Green for selected
                    seat.getStyle().set("color", "white");
                    seat.getStyle().set("box-shadow", "0 0 5px #52c41a");
                    seat.addClickListener(e -> toggleSeat(seatId));
                } else {
                    seat.getStyle().set("background", "#1890ff"); // Blue for available
                    seat.getStyle().set("color", "white");
                    seat.addClickListener(e -> toggleSeat(seatId));
                    seat.getElement().addEventListener("mouseenter", e -> {
                        seat.getStyle().set("transform", "scale(1.1)");
                    });
                    seat.getElement().addEventListener("mouseleave", e -> {
                        seat.getStyle().set("transform", "scale(1.0)");
                    });
                }
                rowLayout.add(seat);
            }
            layout.add(rowLayout);
        }
        
        // Legend
        HorizontalLayout legend = new HorizontalLayout();
        legend.getStyle().set("margin-top", "20px");
        legend.setSpacing(true);
        legend.add(createLegendItem("#1890ff", "Available"));
        legend.add(createLegendItem("#e0e0e0", "Reserved"));
        legend.add(createLegendItem("#52c41a", "Selected"));
        layout.add(legend);
    }

    private Component createLegendItem(String color, String text) {
        HorizontalLayout item = new HorizontalLayout();
        item.setSpacing(true);
        Div box = new Div();
        box.setWidth("15px");
        box.setHeight("15px");
        box.getStyle().set("background", color);
        box.getStyle().set("border-radius", "3px");
        Span label = new Span(text);
        label.getStyle().set("font-size", "12px");
        item.add(box, label);
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        return item;
    }

    private void toggleSeat(String seatId) {
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
        } else {
            selectedSeats.add(seatId);
        }
        updateLayout();
        if (listener != null) {
            listener.onSelectionChange(selectedSeats);
        }
    }

    public Set<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setDimensions(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.selectedSeats.clear();
        updateLayout();
    }
}
