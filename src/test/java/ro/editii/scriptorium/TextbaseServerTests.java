package ro.editii.scriptorium;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.editii.scriptorium.web.DivController;

@SpringBootTest
public class TextbaseServerTests {

    @Autowired
    DivController divController;

    @Test
    public void contextLoads() {
        assert divController != null;
    }

}
