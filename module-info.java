module ru.nsu.kudryavtsev.andrey.lab4snake {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.google.protobuf;

    requires org.slf4j;
    requires logback.classic;
    requires logback.core;

    opens ru.nsu.kudryavtsev.andrey.lab4 to javafx.fxml;
    exports ru.nsu.kudryavtsev.andrey.lab4;
}