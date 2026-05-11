package com.hcbs.views;

import com.hcbs.entity.User;
import com.hcbs.repository.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.select.Select;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

@PageTitle("Register | Horizon Cinemas")
@Route("register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        H1 title = new H1("Create Account");
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        PasswordField confirmPassword = new PasswordField("Confirm Password");
        
        Select<String> roleSelect = new Select<>();
        roleSelect.setLabel("Position");
        roleSelect.setItems("Admin", "Manager", "Staff", "Customer");
        roleSelect.setValue("Customer");
        
        Button register = new Button("Register", e -> {
            if (username.isEmpty() || password.isEmpty()) {
                Notification.show("Please fill in all fields");
                return;
            }
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwords do not match");
                return;
            }
            if (userRepository.findByUsername(username.getValue()).isPresent()) {
                Notification.show("Username already exists");
                return;
            }

            String selectedRole = roleSelect.getValue();
            String dbRole = "USER";
            if ("Admin".equals(selectedRole)) dbRole = "ADMIN";
            else if ("Manager".equals(selectedRole)) dbRole = "MANAGER";
            else if ("Staff".equals(selectedRole)) dbRole = "STAFF";
            else dbRole = "CUSTOMER";

            User user = User.builder()
                    .username(username.getValue())
                    .password(passwordEncoder.encode(password.getValue()))
                    .roles(new HashSet<>(Collections.singletonList(dbRole)))
                    .build();
            
            userRepository.save(user);
            Notification.show("Registration successful! Please login.");
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        register.getStyle().set("background-color", "#2563eb").set("color", "white");
        
        Button backToLogin = new Button("Back to Login", e -> getUI().ifPresent(ui -> ui.navigate("login")));

        add(title, username, password, confirmPassword, roleSelect, register, backToLogin);
    }
}
