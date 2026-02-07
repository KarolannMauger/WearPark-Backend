package edu.wearpark.backend.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonValidationUtilTest {
    @Nested
    class validatePassword {
        @Test
        void whenIsNull_shouldFail() {
            assertFalse(CommonValidationUtil.validatePassword(null));
        }
        @Test
        void whenLessThan8Char_shouldFail() {
            assertFalse(CommonValidationUtil.validatePassword("Qw@123"));
        }
        @Test
        void whenNoSmallCaps_shouldFail() {
            assertFalse(CommonValidationUtil.validatePassword("QW@1235678"));
        }
        @Test
        void whenNoCaps_shouldFail() {
            assertFalse(CommonValidationUtil.validatePassword("qw@1235678"));
        }
        @Test
        void whenNoNumeral_shouldFail() {
            assertFalse(CommonValidationUtil.validatePassword("Qw@abcdefghij"));
        }
        @Test
        void whenNoSpecialChar_shouldFail() {
            assertFalse(CommonValidationUtil.validatePassword("Qw123bcdefghij"));
        }
        @Test
        void happyPath() {
            assertTrue(CommonValidationUtil.validatePassword("Qw@123bcdefghij"));
        }
    }

}