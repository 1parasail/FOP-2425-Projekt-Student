package hProjekt.mocking;

public class StudentMethodCall {

    public Object invoked;
    public Invocation call;
    public Throwable exception;

    public StudentMethodCall(Object invoked, Invocation call, Throwable exception) {
        this.invoked = invoked;
        this.call = call;
        this.exception = exception;
    }

    public Object getInvoked() {
        return invoked;
    }

    public void setInvoked(Object invoked) {
        this.invoked = invoked;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Invocation getCall() {
        return call;
    }

    public void setCall(Invocation call) {
        this.call = call;
    }

    @Override
    public String toString() {
        return "StudentMethodCall{" +
            "invoked=" + invoked +
            ", call=" + call +
            ", exception=" + exception +
            '}';
    }
}
