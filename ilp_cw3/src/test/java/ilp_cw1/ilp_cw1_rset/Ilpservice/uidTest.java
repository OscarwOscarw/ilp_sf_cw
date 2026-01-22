package ilp_cw1.ilp_cw1_rset.Ilpservice;

import ilp_cw1.ilp_cw1_rset.ilpController;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class focusing on testing the uid() method
 */
public class uidTest {
    @Mock
    private ilpService ilpService;

    // Create an instance of the controller under test
    private final ilpController controller = new ilpController(ilpService);

    /**
     * Tests whether the uid endpoint returns the correct student ID
     * Verification points:
     * 1. Return value is not null
     * 2. Return value exactly matches the expected "s2488412"
     */
    @Test
    void testUidReturnsCorrectValue() {
        // Call the method under test
        String result = controller.uid();

        // Verify the results
        assertNotNull(result, "uid return value should not be null");
        assertEquals("s2488412", result, "uid return value does not match expected value");
    }

    /**
     * Tests whether the return value of the uid endpoint is an immutable string
     * Ensures the returned value is a fixed constant that cannot be accidentally modified
     */
    @Test
    void testUidIsImmutable() {
        String result = controller.uid();

        // Verify the returned value is a constant string (checks reference equality)
        assertSame("s2488412", result, "uid should return a constant string");
    }
}