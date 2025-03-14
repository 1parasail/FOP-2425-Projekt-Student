package hProjekt.mocking;

import java.util.Arrays;
import java.util.Objects;

public record Invocation(Object[] arguments, Object returnValue) {

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Invocation that = (Invocation) object;
        return Objects.deepEquals(arguments, that.arguments) && Objects.equals(returnValue, that.returnValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(arguments), returnValue);
    }
}
