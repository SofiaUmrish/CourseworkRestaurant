package org.example.restaurant_management_system;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest extends ApplicationTest {

    private Main mainApp;
    private Stage testStage;

    @Override
    public void start(Stage stage) throws Exception {
        testStage = stage;
        mainApp = new Main();
        mainApp.start(stage);
    }

    @Test
    void testApplicationStartsSuccessfully() {
        Scene scene = testStage.getScene();

        assertNotNull(scene, "Scene не повинна бути null після запуску");
        assertEquals("Система Управління Рестораном", testStage.getTitle(),
                "Заголовок сцени має бути правильний");

        assertNotNull(scene.getStylesheets(), "Стилі не мають бути null");
        assertEquals(1, scene.getStylesheets().size(), "Має бути" +
                " один стиль завантажений");

            assertTrue(scene.getStylesheets().get(0).contains("/styles/LoginStyle.css"),
                    "LoginStyle.css має бути завантажений");
    }

    @Test
    void testStageIsShowingAfterStart() {
        assertTrue(testStage.isShowing(), "Stage повинен " +
                "відображатися після запуску програми");
    }
}