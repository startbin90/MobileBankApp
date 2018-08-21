import java.io.Serializable;

public class testobj implements Serializable {
    static final long serialVersionUID = 42L;
    int req;
    String name;

    testobj(int req, String name) {
        this.req = req;

    }
}
