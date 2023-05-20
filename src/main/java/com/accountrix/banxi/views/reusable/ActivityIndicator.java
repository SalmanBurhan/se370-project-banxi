package com.accountrix.banxi.views.reusable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

@JsModule("./src/lottie-player.js")
public class ActivityIndicator extends Div {

    private final Component parentComponent;
    public ActivityIndicator(Component parent) {

        this.parentComponent = parent;

        parent.getStyle().set("position", "relative");
        parent.getStyle().set("flex", "1 0 auto");

        //width:100%; position: absolute; top:0; left:0; z-index: 9;
        this.getStyle().set("position", "absolute");
        this.getStyle().set("top", "40%");
        this.getStyle().set("right", "0");
        this.getStyle().set("bottom", "0");
        this.getStyle().set("left", "0");
        this.getStyle().set("width", "100%");
        this.getStyle().set("z-index", "100");

        //Image image = new Image("icons/activity-indicator.gif", "Activity Indicator");
        Html lottiePlayer = new Html("<lottie-player src=\"https://assets8.lottiefiles.com/private_files/lf30_lccfyjj6.json\"  background=\"transparent\"  speed=\"1.0\"  loop  autoplay></lottie-player>");
        lottiePlayer.getStyle().set("display", "block");
        lottiePlayer.getStyle().set("width", "100px");
        lottiePlayer.getStyle().set("height", "100px");
        lottiePlayer.getStyle().set("margin-left", "auto");
        lottiePlayer.getStyle().set("margin-right", "auto");
        this.add(lottiePlayer);

        this.parentComponent.getElement().setChild(0, this.getElement());
        this.startAnimating();
    }

    public void startAnimating() {
        this.getStyle().set("display", "block");
        System.out.println("[Activity Spinner] Start Animating");
    }

    public void stopAnimating() {
        this.getStyle().set("display", "none");
        System.out.println("[Activity Spinner] Stop Animating");
    }
}
