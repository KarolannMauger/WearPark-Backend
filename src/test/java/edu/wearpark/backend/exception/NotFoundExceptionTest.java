package edu.wearpark.backend.exception;

import edu.wearpark.backend.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void constructor_withTypeAndUri_shouldSetFieldsCorrectly() {
        String type = "Device";
        String uri = "/devices/123";

        NotFoundException ex = new NotFoundException(type, uri);

        assertEquals(type, ex.getType());
        assertEquals(uri, ex.getUri());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getCode());
    }

    @Test
    void constructor_withOnlyType_shouldSetUriToNull() {
        String type = "User";

        NotFoundException ex = new NotFoundException(type);

        assertEquals(type, ex.getType());
        assertNull(ex.getUri());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getCode());
    }

    @Test
    void fieldsShouldBeImmutable() {
        NotFoundException ex = new NotFoundException("Product", "/products/1");

        assertAll(
                () -> assertEquals("Product", ex.getType()),
                () -> assertEquals("/products/1", ex.getUri())
        );
    }
}