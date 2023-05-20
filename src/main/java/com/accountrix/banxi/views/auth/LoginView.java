package com.accountrix.banxi.views.auth;

import com.accountrix.banxi.model.user.Role;
import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.ServiceRef;
import com.accountrix.banxi.service.user.UserRepository;
import com.accountrix.banxi.service.user.UserService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Login | Banxi")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    @Autowired
    private UserService userService;

    private final LoginForm loginForm = new LoginForm();
    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(new H1("Banxi"));
        configureLoginForm();
        createSignUpPrompt();
    }

    private void configureLoginForm() {
        LoginI18n loginI18n = LoginI18n.createDefault();

        LoginI18n.Form form = loginI18n.getForm();
        form.setUsername("Email Address");
        form.setSubmit("Log In");
        form.setTitle("Welcome Back!");
        loginI18n.setForm(form);

        LoginI18n.ErrorMessage formError = loginI18n.getErrorMessage();
        formError.setTitle("Incorrect Email Address or Password");
        formError.setMessage("Check that you have entered the correct email address and password and try again.");
        loginI18n.setErrorMessage(formError);

        loginForm.setI18n(loginI18n);
        loginForm.setAction("login");
        add(loginForm);
    }

    private void createSignUpPrompt() {
        HorizontalLayout signUpLayout = new HorizontalLayout();
        signUpLayout.setAlignItems(Alignment.BASELINE);

        Dialog signupDialog = createSignUpDialog();

        Button signUpButton = new Button("Sign Up Here", e -> signupDialog.open());
        signUpButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        signUpLayout.add(new H4("New To Banxi?"), signUpButton, signupDialog);

        add(signUpLayout);
    }

    private Dialog createSignUpDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        H3 title = new H3("Sign Up");
        Hr divider = new Hr();

        TextField firstName = new TextField("First Name");
        firstName.setAutocapitalize(Autocapitalize.WORDS);
        firstName.setRequired(true);

        TextField lastName = new TextField("Last Name");
        firstName.setAutocapitalize(Autocapitalize.WORDS);
        lastName.setRequired(false);

        EmailField email = new EmailField("Email Address");
        email.setRequired(true);

        PasswordField password = new PasswordField("Password");
        password.setRequired(true);

        PasswordField confirmPassword = new PasswordField("Confirm password");
        confirmPassword.setRequired(true);

        confirmPassword.setValueChangeMode(ValueChangeMode.EAGER);
        confirmPassword.addValueChangeListener(e -> {
            confirmPassword.setHelperText((!e.getValue().equals(password.getValue())) ? "Password Does Not Match" : "");
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        Button signupButton = new Button("Create Account", e -> {
            User newUser = new User(email.getValue(), firstName.getValue(), lastName.getValue(), password.getValue(), Role.ROLE_USER);
            userService.create(newUser);
            Notification creationNotification = Notification.show(String.format("Welcome to Banxi, %s!", newUser.getFirstName()));
            creationNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });
        signupButton.setDisableOnClick(true);
        signupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(formLayout);
        formLayout.add(title, divider, firstName, lastName, email, password, confirmPassword);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(email, 2);
        dialog.getFooter().add(cancelButton, signupButton);

        return dialog;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}