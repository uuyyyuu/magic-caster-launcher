import com.wbdl.elder.ui.HomeActivity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

public final class EnumWireTest {
    public static void main(String[] args) throws Exception {
        if (!HomeActivity.c.class.getName().equals("com.wbdl.elder.ui.HomeActivity$c")) {
            throw new AssertionError("Unexpected wire class name");
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(HomeActivity.c.Devices);
        }
        String wire = new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);
        if (!wire.contains("com.wbdl.elder.ui.HomeActivity$c") || !wire.contains("Devices")) {
            throw new AssertionError("Missing serialized destination names");
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            if (in.readObject() != HomeActivity.c.Devices) throw new AssertionError("Enum round-trip failed");
        }
        System.out.println("PASS: expected enum binary name, constant and Java serialization round-trip");
        System.out.println("This unit test covers only the wire format. See README for device-test scope.");
    }
}
