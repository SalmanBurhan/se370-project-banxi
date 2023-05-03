package com.accountrix.banxi.views.error;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ErrorView extends VerticalLayout {

    public ErrorView(String errorMessage) {

        setAlignItems(Alignment.CENTER);

        Image image = new Image("icons/error-view-unexpected.png", "Unexpected Error Graphic");
        image.getElement().getStyle().set("width", "40%");
        add(image);

        H1 title = new H1("Uh Oh, That Wasn't Supposed To Happen!");
        title.getElement().getStyle().set("text-align", "center");
        add(title);

        H3 message = new H3(errorMessage);
        message.getElement().getStyle().set("text-align", "center");
        add(message);
    }
}
